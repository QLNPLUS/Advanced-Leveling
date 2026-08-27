package daripher.autoleveling.datagen;

import daripher.autoleveling.AutoLevelingMod;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID, bus = Bus.MOD)
public class AutoLevelingDataGenerator {
  @SubscribeEvent
  public static void onGatherData(GatherDataEvent event) {
    DataGenerator dataGenerator = event.getGenerator();
    ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
    PackOutput packOutput = dataGenerator.getPackOutput();
    dataGenerator.addProvider(event.includeClient(), new AutoLevelingLanguageProvider(packOutput));
    dataGenerator.addProvider(
        event.includeClient(), new AutoLevelingItemModelProvider(packOutput, existingFileHelper));
  }
}
