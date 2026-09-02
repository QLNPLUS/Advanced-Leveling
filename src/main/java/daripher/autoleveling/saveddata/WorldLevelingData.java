package daripher.autoleveling.saveddata;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.data.DimensionsLevelingSettingsReloader;
import daripher.autoleveling.settings.LevelingSettings;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class WorldLevelingData extends SavedData {
  private static final Codec<WorldLevelingData> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.FLOAT.fieldOf("LevelBonus").forGetter(data -> data.levelBonus),
                      Codec.INT.fieldOf("TickCount").forGetter(data -> data.tickCount))
                  .apply(instance, WorldLevelingData::fromValues));
  private static final SavedDataType<WorldLevelingData> TYPE =
      new SavedDataType<>(
          Identifier.fromNamespaceAndPath(AutoLevelingMod.MOD_ID, "world_leveling"),
          WorldLevelingData::create,
          CODEC);
  private float levelBonus;
  public int tickCount;

  private static WorldLevelingData create() {
    return new WorldLevelingData();
  }

  @SubscribeEvent
  public static void tick(LevelTickEvent.Pre event) {
    if (event.getLevel().isClientSide()) return;
    ServerLevel level = (ServerLevel) event.getLevel();
    WorldLevelingData levelingData = WorldLevelingData.get(level);
    levelingData.tick(level);
  }

  private static WorldLevelingData fromValues(float levelBonus, int tickCount) {
    WorldLevelingData data = WorldLevelingData.create();
    data.levelBonus = levelBonus;
    data.tickCount = tickCount;
    return data;
  }

  public static WorldLevelingData get(ServerLevel level) {
    return level
        .getDataStorage()
        .computeIfAbsent(TYPE);
  }

  private void tick(Level world) {
    tickCount++;
    // 24_000 ticks is one Minecraft day
    if (tickCount >= 24_000) {
      ResourceKey<Level> dimension = world.dimension();
      LevelingSettings settings =
          DimensionsLevelingSettingsReloader.get(dimension);
      levelBonus += settings.levelsPerDay();
      tickCount -= 24_000;
    }
    setDirty();
  }

  public int getLevelBonus() {
    return (int) levelBonus;
  }

  public void setLevelBonus(float levelBonus) {
    this.levelBonus = levelBonus;
    setDirty();
  }

  public void addLevelBonus(float levelBonus) {
    setLevelBonus(this.levelBonus + levelBonus);
  }
}
