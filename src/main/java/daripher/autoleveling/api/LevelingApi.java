package daripher.autoleveling.api;

import daripher.autoleveling.LevelHolder;
import daripher.autoleveling.config.AdvancedConfig;
import java.util.List;
import java.util.OptionalInt;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Read-only, server-safe access to the level Advanced Leveling stores for an entity.
 *
 * <p>This is the Fabric port of the NeoForge API added in 1.0.3. It follows this branch instead of
 * the NeoForge storage: the level lives in the {@code autoleveling$level} field of every {@link
 * LivingEntity} (see {@code LivingEntityMixin}) and is persisted as the integer NBT key {@link
 * #LEVEL_TAG}. The stored value is already the level shown to players and is one-based, with
 * {@link #NO_LEVEL} meaning "this entity has no Advanced Leveling level"; nothing here adds one or
 * converts an existing world to another base.
 *
 * <p>Advanced Leveling never levels players, so {@link #displayLevel(LivingEntity)} always returns
 * an empty result for them. A type listed in {@code mobs.hidden_levels} (an exact entity ID or a
 * {@code namespace:*} entry) also has no visible level.
 *
 * <p>Every call is a pure read. It does not write NBT, does not set the level, does not refresh
 * attributes, equipment or drops, and does not create or modify saved data, so it is safe to call
 * from a server tick or a packet handler. Nothing here is cached: a caller that needs a stable
 * snapshot for one tick should read the value once per entity per tick itself.
 *
 * <p>This class has no client-only references and can be called on a dedicated server. The Fabric
 * artifact does not compile a level plate or a Jade provider, so this API is the only display-level
 * reader on this branch and does not claim those renderers exist.
 *
 * @since 1.0.3
 */
public final class LevelingApi {
  /** Persisted NBT key holding the one-based level; {@link #NO_LEVEL} means "no level". */
  public static final String LEVEL_TAG = "AdvancedLeveling";

  /** Stored value used by this branch for "this entity has no Advanced Leveling level". */
  public static final int NO_LEVEL = -1;

  private LevelingApi() {}

  /**
   * Returns the level Advanced Leveling stores for this entity, or an empty result when it has
   * none.
   *
   * <p>The result is empty when the entity is {@code null} or a player, when the entity type is
   * hidden through {@code mobs.hidden_levels}, and when the stored value is {@link #NO_LEVEL} (or
   * any other negative value read from a damaged save). A stored value of zero is a real level and
   * is reported as {@code 0}; it is never treated as "missing" and never incremented.
   *
   * @param entity the entity to read, possibly {@code null}
   * @return the stored level, or an empty result when no level exists
   */
  public static OptionalInt displayLevel(LivingEntity entity) {
    if (entity == null) return OptionalInt.empty();
    boolean player = entity instanceof PlayerEntity || entity.getType() == EntityType.PLAYER;
    return displayLevel(player, isHidden(entity.getType()), (LevelHolder) entity);
  }

  /**
   * Tells whether Advanced Leveling hides the level of this entity type through {@code
   * mobs.hidden_levels}. Both an exact entity ID and a {@code namespace:*} entry hide the level.
   *
   * <p>The configuration is read on every call and never cached here, so a reload of {@code
   * advancedleveling.json} takes effect immediately. Reading the configuration requires the Fabric
   * loader, which is why the pure rule lives in {@link #isHidden(List, String)} and is what the
   * tests exercise.
   *
   * @param type the entity type to test, possibly {@code null}
   * @return {@code true} when the level must not be displayed for this type
   */
  public static boolean isHidden(EntityType<?> type) {
    if (type == null) return false;
    Identifier entityId = Registries.ENTITY_TYPE.getId(type);
    if (entityId == null) return false;
    return isHidden(AdvancedConfig.getHiddenLevels(), entityId.toString());
  }

  /**
   * Applies the exclusion rules to an already-read level.
   *
   * <p>Kept separate from {@link #displayLevel(LivingEntity)} so the rules can be exercised without
   * a running game: {@code player} is the player check, {@code hidden} is {@link
   * #isHidden(EntityType)} and {@code storedLevel} is the raw field value.
   */
  static OptionalInt resolve(boolean player, boolean hidden, int storedLevel) {
    if (player || hidden) return OptionalInt.empty();
    if (storedLevel < 0) return OptionalInt.empty();
    return OptionalInt.of(storedLevel);
  }

  /**
   * The player and hidden checks run before the level is read, and the level is only ever read, so
   * an excluded entity is not even inspected and no caller can write through this path.
   */
  static OptionalInt displayLevel(boolean player, boolean hidden, LevelHolder holder) {
    if (player || hidden || holder == null) return OptionalInt.empty();
    return resolve(false, false, holder.autoleveling$getLevel());
  }

  /**
   * The shared {@code mobs.hidden_levels} rule: an exact {@code namespace:path} entry or a {@code
   * namespace:*} entry hides every entity of that namespace.
   */
  static boolean isHidden(List<String> hiddenLevels, String entityId) {
    if (hiddenLevels == null || entityId == null) return false;
    if (hiddenLevels.contains(entityId)) return true;
    // A namespace wildcard only applies to a real `namespace:path` id: a string without a colon is
    // not a namespace, so it must not match `namespace:*` entries.
    int separator = entityId.indexOf(':');
    if (separator < 0) return false;
    return hiddenLevels.contains(entityId.substring(0, separator) + ":*");
  }
}
