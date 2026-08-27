package daripher.autoleveling.integration.jade;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.event.MobsLevelingEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

public enum LevelDataProvider implements IServerDataProvider<EntityAccessor> {
  INSTANCE;

  static final String LEVEL_TAG = "Level";
  private static final ResourceLocation ID =
      ResourceLocation.fromNamespaceAndPath(AutoLevelingMod.MOD_ID, "level");

  @Override
  public ResourceLocation getUid() {
    return ID;
  }

  @Override
  public void appendServerData(CompoundTag data, EntityAccessor entityAccessor) {
    if (!(entityAccessor.getEntity() instanceof LivingEntity livingEntity)
        || !MobsLevelingEvents.hasLevel(livingEntity)
        || !MobsLevelingEvents.shouldShowLevel(livingEntity)) {
      return;
    }
    data.putInt(LEVEL_TAG, MobsLevelingEvents.getLevel(livingEntity) + 1);
  }

  @Override
  public boolean shouldRequestData(EntityAccessor entityAccessor) {
    return entityAccessor.getEntity() instanceof LivingEntity;
  }
}
