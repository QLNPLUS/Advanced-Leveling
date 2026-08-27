package daripher.autoleveling.init;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.item.*;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class AutoLevelingItems {
  public static final DeferredRegister<Item> REGISTRY =
      DeferredRegister.create(BuiltInRegistries.ITEMS, AutoLevelingMod.MOD_ID);

  public static final DeferredHolder<Item, Item> BLACKLIST_TOOL =
      REGISTRY.register("blacklist_tool", BlacklistToolItem::new);
  public static final DeferredHolder<Item, Item> WHITELIST_TOOL =
      REGISTRY.register("whitelist_tool", WhitelistToolItem::new);
}
