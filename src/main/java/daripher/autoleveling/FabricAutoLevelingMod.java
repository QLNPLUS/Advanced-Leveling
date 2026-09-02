package daripher.autoleveling;

import daripher.autoleveling.config.AdvancedConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class FabricAutoLevelingMod implements ModInitializer {
  public static final String MOD_ID = "autoleveling";
  public static Item BLACKLIST_TOOL;
  public static Item WHITELIST_TOOL;

  @Override
  public void onInitialize() {
    BLACKLIST_TOOL = registerTool("blacklist_tool", false);
    WHITELIST_TOOL = registerTool("whitelist_tool", true);
    AdvancedConfig.load();
  }

  private static Item registerTool(String name, boolean whitelist) {
    return Registry.register(
        Registries.ITEM,
        Identifier.of(MOD_ID, name),
        new MobListToolItem(new Item.Settings().maxCount(1), whitelist));
  }
}
