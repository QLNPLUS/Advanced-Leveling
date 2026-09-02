package daripher.autoleveling;

import daripher.autoleveling.config.AdvancedConfig;
import daripher.autoleveling.config.Config;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public final class FabricLevelingHooks {
  private static final Identifier LEVEL_MODIFIER_ID = Identifier.of(FabricAutoLevelingMod.MOD_ID, "level");

  private FabricLevelingHooks() {}

  public static void tick(LivingEntity entity) {
    if (!(entity.getWorld() instanceof ServerWorld world)
        || entity instanceof PlayerEntity
        || !entity.isAlive()
        || ((LevelHolder) entity).autoleveling$getLevel() >= 0) {
      return;
    }

    Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
    if (!AdvancedConfig.shouldLevel(entityId.toString())) return;

    double distance = Math.sqrt(entity.getBlockPos().getSquaredDistance(world.getSpawnPos()));
    double deepness = Math.max(0.0D, world.getSeaLevel() - entity.getY());
    int level = Config.STARTING_LEVEL.get();
    level += (int) Math.floor(distance * Config.LEVELS_PER_DISTANCE.get());
    level += (int) Math.floor(deepness * Config.LEVELS_PER_DEEPNESS.get());
    level += (int) Math.floor(world.getTimeOfDay() / 24000.0D * Config.LEVELS_PER_DAY.get());
    if (Config.RANDOM_LEVEL_BONUS.get() > 0) {
      level += world.random.nextInt(Config.RANDOM_LEVEL_BONUS.get() + 1);
    }
    if (Config.MAX_LEVEL.get() > 0) level = Math.min(level, Config.MAX_LEVEL.get());

    ((LevelHolder) entity).autoleveling$setLevel(Math.max(0, level));
    applyAttribute(entity, EntityAttributes.GENERIC_MOVEMENT_SPEED, level * 0.001D);
    applyAttribute(entity, EntityAttributes.GENERIC_ATTACK_DAMAGE, level * 0.1D);
    applyAttribute(entity, EntityAttributes.GENERIC_ARMOR, level * 0.1D);
    applyAttribute(entity, EntityAttributes.GENERIC_MAX_HEALTH, level * 0.1D);
  }

  private static void applyAttribute(
      LivingEntity entity, RegistryEntry<EntityAttribute> attribute, double amount) {
    EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
    if (instance == null) return;
    instance.removeModifier(LEVEL_MODIFIER_ID);
    instance.addPersistentModifier(
        new EntityAttributeModifier(LEVEL_MODIFIER_ID, amount, EntityAttributeModifier.Operation.ADD_VALUE));
  }
}
