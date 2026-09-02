package daripher.autoleveling.integration.jade;

import daripher.autoleveling.AutoLevelingMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum LevelComponentProvider implements IEntityComponentProvider {
  INSTANCE;

  private static final Identifier ID = Identifier.fromNamespaceAndPath(AutoLevelingMod.MOD_ID, "level");

  @Override
  public Identifier getUid() {
    return ID;
  }

  @Override
  public void appendTooltip(
      ITooltip tooltip, EntityAccessor entityAccessor, IPluginConfig pluginConfig) {
    if (!entityAccessor.getServerData().contains(LevelDataProvider.DATA_KEY)) return;
    int level = entityAccessor.getServerData().getInt(LevelDataProvider.DATA_KEY);
    tooltip.add(Component.translatable("jade.autoleveling.tooltip", level));
  }
}
