package daripher.autoleveling.item;

import daripher.autoleveling.config.AdvancedConfig;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;

public class BlacklistToolItem extends Item {
  public BlacklistToolItem(Properties properties) {
    super(properties);
  }

  @Override
  public @NotNull InteractionResult interactLivingEntity(
      @NotNull ItemStack itemStack,
      Player player,
      @NotNull LivingEntity entity,
      @NotNull InteractionHand hand) {
    if (!player.level().isClientSide()) blacklistEntity(player, entity);
    return InteractionResult.SUCCESS;
  }

  protected void blacklistEntity(Player player, LivingEntity entity) {
    String id =
        Objects.requireNonNull(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType())).toString();
    if (!AdvancedConfig.toggleMobBlacklist(id)) {
      player.sendSystemMessage(Component.translatable(getDescriptionId() + ".removed", id));
    } else {
      player.sendSystemMessage(Component.translatable(getDescriptionId() + ".added", id));
    }
  }

  @Override
  public void appendHoverText(
      @NotNull ItemStack itemStack,
      Item.TooltipContext context,
      TooltipDisplay tooltipDisplay,
      Consumer<Component> components,
      @NotNull TooltipFlag tooltipFlag) {
    components.accept(Component.translatable(getDescriptionId() + ".tooltip"));
  }
}
