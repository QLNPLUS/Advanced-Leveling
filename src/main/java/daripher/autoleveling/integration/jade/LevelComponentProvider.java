package daripher.autoleveling.integration.jade;

import daripher.autoleveling.AutoLevelingMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum LevelComponentProvider implements IEntityComponentProvider {
  INSTANCE;

  private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(AutoLevelingMod.MOD_ID, "level");

  @Override
  public ResourceLocation getUid() {
    return ID;
  }

  @Override
  public void appendTooltip(
      ITooltip tooltip, EntityAccessor entityAccessor, IPluginConfig pluginConfig) {
    if (!entityAccessor.getServerData().contains(LevelDataProvider.LEVEL_TAG)) return;
    int level = entityAccessor.getServerData().getInt(LevelDataProvider.LEVEL_TAG);
    tooltip.add(Component.translatable("jade.autoleveling.tooltip", level));
  }
}
