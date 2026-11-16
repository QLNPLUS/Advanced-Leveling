package daripher.autoleveling.api;

import daripher.autoleveling.config.AdvancedConfig;
import daripher.autoleveling.event.MobsLevelingEvents;
import java.util.List;
import java.util.OptionalInt;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Read-only, server-safe access to the level Advanced Leveling stores for an entity.
 *
 * <p>Advanced Leveling only ever levels non-player living entities. The value returned here is the
 * number a player sees for the entity: the persisted, zero-based {@code LEVEL} value plus one. This
 * branch computes the same {@code LEVEL + 1} when it would render a level, so an integration that
 * reads this API stays consistent with the mod's own meaning of a level.
 *
 * <p>Advanced Leveling never levels players, so {@link #displayLevel(LivingEntity)} always returns an
 * empty result for them. A type listed in {@code mobs.hidden_levels} also has no visible level.
 *
 * <p>Every call is a pure read. It does not write NBT, does not set the level, does not refresh
 * attributes, equipment or drops, and does not create or modify saved data, so it is safe to call
 * from a server tick or a packet handler. Nothing here is cached: a caller that needs a stable
 * snapshot for one tick should read the value once per entity per tick itself.
 *
 * <p>This class has no client-only references and can be called on a dedicated server. This branch
 * does not compile a level plate renderer or a Jade provider, so this API is the only display-level
 * reader here and does not claim those renderers exist.
 *
 * @since 1.0.3
 */
public final class LevelingApi {
  /** Persisted NBT key that stores the zero-based level; the displayed level is this value + 1. */
  public static final String LEVEL_TAG = MobsLevelingEvents.LEVEL_TAG;

  private LevelingApi() {}

  /**
   * Returns the level Advanced Leveling shows for this entity, or an empty result when Advanced
   * Leveling has no visible level for it.
   *
   * <p>The result is empty when the entity is a player, when {@code LEVEL} is absent or is not an
   * integer, when the stored value is negative or cannot be incremented without overflowing, and
   * when the entity type is hidden through {@code mobs.hidden_levels}. A stored value of zero is a
   * real level and is reported as {@code 1}; it is never treated as "missing".
   *
   * @param entity the entity to read, possibly {@code null}
   * @return the displayed level, or an empty result when no visible level exists
   */
  public static OptionalInt displayLevel(@Nullable LivingEntity entity) {
    if (entity == null) return OptionalInt.empty();
    if (entity instanceof Player || entity.getType() == EntityType.PLAYER) {
      return OptionalInt.empty();
    }
    Tag stored = entity.getPersistentData().get(LEVEL_TAG);
    return resolve(false, stored, isHidden(entity.getType()));
  }

  /**
   * Tells whether Advanced Leveling hides the level of this entity type through {@code
   * mobs.hidden_levels}. Both an exact entity ID and a {@code namespace:*} entry hide the level.
   *
   * <p>The configuration is read on every call and never cached here, so a reload of {@code
   * advancedleveling.json} takes effect immediately.
   *
   * @param type the entity type to test, possibly {@code null}
   * @return {@code true} when the level must not be displayed for this type
   */
  public static boolean isHidden(@Nullable EntityType<?> type) {
    if (type == null) return false;
    var entityId = EntityType.getKey(type);
    if (entityId == null) return false;
    return isHidden(AdvancedConfig.getHiddenLevels(), entityId.toString());
  }

  /**
   * Applies the display rules to an already-read {@code LEVEL} tag.
   *
   * <p>Kept separate from {@link #displayLevel(LivingEntity)} so the rules can be exercised without
   * a running game. {@code player} is the player check, {@code stored} is the raw tag read from
   * {@code LEVEL} (possibly {@code null}) and {@code hidden} is {@link #isHidden(EntityType)}.
   */
  static OptionalInt resolve(boolean player, @Nullable Tag stored, boolean hidden) {
    if (player || hidden) return OptionalInt.empty();
    if (!(stored instanceof IntTag intTag)) return OptionalInt.empty();
    int storedLevel = intTag.value();
    if (storedLevel < 0 || storedLevel == Integer.MAX_VALUE) return OptionalInt.empty();
    return OptionalInt.of(storedLevel + 1);
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
