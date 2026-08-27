package daripher.autoleveling.item;

import daripher.autoleveling.config.AdvancedConfig;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;

public class WhitelistToolItem extends Item {
  public WhitelistToolItem() {
    super(new Properties().stacksTo(1));
  }

  @Override
  public @NotNull InteractionResult interactLivingEntity(
      @NotNull ItemStack itemStack,
      Player player,
      @NotNull LivingEntity entity,
      @NotNull InteractionHand hand) {
    if (!player.level().isClientSide) whitelistEntity(player, entity);
    return InteractionResult.SUCCESS;
  }

  protected void whitelistEntity(Player player, LivingEntity entity) {
    String id =
        Objects.requireNonNull(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType())).toString();
    if (!AdvancedConfig.toggleMobWhitelist(id)) {
      player.sendSystemMessage(Component.translatable(getDescriptionId() + ".removed", id));
    } else {
      player.sendSystemMessage(Component.translatable(getDescriptionId() + ".added", id));
    }
  }

  @Override
  public void appendHoverText(
      @NotNull ItemStack itemStack,
      Item.TooltipContext context,
      List<Component> components,
      @NotNull TooltipFlag tooltipFlag) {
    components.add(Component.translatable(getDescriptionId() + ".tooltip"));
  }
}
