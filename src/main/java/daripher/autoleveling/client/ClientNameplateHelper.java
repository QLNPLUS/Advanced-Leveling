package daripher.autoleveling.client;

import daripher.autoleveling.config.Config;
import daripher.autoleveling.event.MobsLevelingEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;

public final class ClientNameplateHelper {
  private ClientNameplateHelper() {}

  public static boolean shouldShowName(LivingEntity entity) {
    if (!Minecraft.renderNames()) return false;
    if (entity.isVehicle()) return false;
    Minecraft minecraft = Minecraft.getInstance();
    if (entity == minecraft.getCameraEntity()) return false;
    LocalPlayer clientPlayer = minecraft.player;
    if (clientPlayer == null) return false;
    if (!clientPlayer.hasLineOfSight(entity) || entity.isInvisibleTo(clientPlayer)) return false;
    if (!MobsLevelingEvents.hasLevel(entity)) return false;
    if (!MobsLevelingEvents.shouldShowLevel(entity)) return false;
    if (Config.COMMON.alwaysShowLevel.get()) return true;
    return Config.COMMON.showLevelWhenLookingAt.get() && minecraft.crosshairPickEntity == entity;
  }
}
