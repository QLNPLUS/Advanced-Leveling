package daripher.autoleveling.init;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class AutoLevelingItems {
  public static final DeferredRegister.Items REGISTRY =
      DeferredRegister.createItems(AutoLevelingMod.MOD_ID);

  public static final DeferredHolder<Item, Item> BLACKLIST_TOOL =
      REGISTRY.register(
          "blacklist_tool",
          key ->
              new BlacklistToolItem(
                  new Item.Properties()
                      .setId(ResourceKey.create(Registries.ITEM, key))
                      .stacksTo(1)));
  public static final DeferredHolder<Item, Item> WHITELIST_TOOL =
      REGISTRY.register(
          "whitelist_tool",
          key ->
              new WhitelistToolItem(
                  new Item.Properties()
                      .setId(ResourceKey.create(Registries.ITEM, key))
                      .stacksTo(1)));
}
