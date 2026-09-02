package daripher.autoleveling.saveddata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import daripher.autoleveling.AutoLevelingMod;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class GlobalLevelingData extends SavedData {
  private static final Codec<GlobalLevelingData> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(Codec.INT.fieldOf("LevelBonus").forGetter(GlobalLevelingData::getLevelBonus))
                  .apply(instance, GlobalLevelingData::fromLevelBonus));
  private static final SavedDataType<GlobalLevelingData> TYPE =
      new SavedDataType<>(
          Identifier.fromNamespaceAndPath(AutoLevelingMod.MOD_ID, "global_leveling"),
          GlobalLevelingData::create,
          CODEC);
  private int levelBonus;

  private static GlobalLevelingData create() {
    return new GlobalLevelingData();
  }

  private static GlobalLevelingData fromLevelBonus(int levelBonus) {
    GlobalLevelingData data = GlobalLevelingData.create();
    data.levelBonus = levelBonus;
    return data;
  }

  public static GlobalLevelingData get(MinecraftServer server) {
    return server
        .overworld()
        .getDataStorage()
        .computeIfAbsent(TYPE);
  }

  public void setLevel(int level) {
    this.levelBonus = level;
    setDirty();
  }

  public int getLevelBonus() {
    return levelBonus;
  }
}
