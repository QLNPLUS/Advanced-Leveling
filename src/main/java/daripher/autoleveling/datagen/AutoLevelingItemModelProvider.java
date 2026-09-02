package daripher.autoleveling.datagen;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.init.AutoLevelingItems;
import java.util.Objects;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.minecraft.core.registries.BuiltInRegistries;

public class AutoLevelingItemModelProvider extends ItemModelProvider {
  public AutoLevelingItemModelProvider(
      PackOutput packOutput, ExistingFileHelper existingFileHelper) {
    super(packOutput, AutoLevelingMod.MOD_ID, existingFileHelper);
  }

  @Override
  protected void registerModels() {
    handheld(AutoLevelingItems.BLACKLIST_TOOL.get());
    handheld(AutoLevelingItems.WHITELIST_TOOL.get());
  }

  private void handheld(Item item) {
    Identifier itemId = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item));
    withExistingParent(itemId.toString(), mcLoc("handheld"))
        .texture("layer0", modLoc("item/" + itemId.getPath()));
  }
}
