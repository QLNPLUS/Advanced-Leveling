package daripher.autoleveling.integration.jade;

import daripher.autoleveling.AutoLevelingMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class AutoLevelingWailaPlugin implements IWailaPlugin {
  private static final IEntityComponentProvider PROVIDER = new IEntityComponentProvider() {
    @Override public ResourceLocation getUid() { return ResourceLocation.fromNamespaceAndPath(AutoLevelingMod.MOD_ID, "level"); }
    @Override public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
      if (accessor.getEntity() instanceof LivingEntity entity && AutoLevelingMod.hasLevel(entity))
        tooltip.add(Component.translatable("jade.autoleveling.tooltip", AutoLevelingMod.getLevel(entity) + 1));
    }
  };
  @Override public void registerClient(IWailaClientRegistration registration) { registration.registerEntityComponent(PROVIDER, LivingEntity.class); }
}
