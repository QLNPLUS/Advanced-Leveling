package daripher.autoleveling.saveddata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class GlobalLevelingData extends SavedData {
  private int levelBonus;

  private static GlobalLevelingData create() {
    return new GlobalLevelingData();
  }

  private static GlobalLevelingData load(CompoundTag tag, HolderLookup.Provider registries) {
    GlobalLevelingData data = GlobalLevelingData.create();
    data.levelBonus = tag.getInt("LevelBonus");
    return data;
  }

  public static GlobalLevelingData get(MinecraftServer server) {
    return server
        .overworld()
        .getDataStorage()
        .computeIfAbsent(
            new SavedData.Factory<>(GlobalLevelingData::create, GlobalLevelingData::load),
            "global_leveling");
  }

  @Override
  public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
    tag.putInt("LevelBonus", levelBonus);
    return tag;
  }

  public void setLevel(int level) {
    this.levelBonus = level;
    setDirty();
  }

  public int getLevelBonus() {
    return levelBonus;
  }
}
