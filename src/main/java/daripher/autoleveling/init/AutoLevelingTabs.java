package daripher.autoleveling.init;

import daripher.autoleveling.AutoLevelingMod;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class AutoLevelingTabs {
  public static final CreativeModeTab TOOLS =
      new CreativeModeTab("tools") {
        @Override
        public ItemStack makeIcon() {
          return new ItemStack(AutoLevelingItems.BLACKLIST_TOOL.get());
        }
      };
}
