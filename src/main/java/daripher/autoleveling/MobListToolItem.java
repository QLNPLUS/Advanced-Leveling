package daripher.autoleveling;

import daripher.autoleveling.config.AdvancedConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public final class MobListToolItem extends Item {
  private final boolean whitelist;

  public MobListToolItem(Settings settings, boolean whitelist) {
    super(settings);
    this.whitelist = whitelist;
  }

  @Override
  public ActionResult useOnEntity(
      ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
    if (!user.getWorld().isClient) {
      String entityId = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
      boolean added = whitelist
          ? AdvancedConfig.toggleMobWhitelist(entityId)
          : AdvancedConfig.toggleMobBlacklist(entityId);
      String key = whitelist
          ? "item.autoleveling.whitelist_tool.%s"
          : "item.autoleveling.blacklist_tool.%s";
      user.sendMessage(Text.translatable(key.formatted(added ? "added" : "removed"), entity.getDisplayName()), true);
    }
    return ActionResult.SUCCESS;
  }
}
