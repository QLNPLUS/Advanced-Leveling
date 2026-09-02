package daripher.autoleveling.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import daripher.autoleveling.config.Config;
import daripher.autoleveling.settings.DimensionLevelingSettings;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class DimensionsLevelingSettingsReloader
    extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> {
  private static final Logger LOGGER = LogUtils.getLogger();
  private static final Gson GSON = new GsonBuilder().create();
  private static final Map<Identifier, DimensionLevelingSettings> SETTINGS = new HashMap<>();

  public DimensionsLevelingSettingsReloader() {
  }

  @Nonnull
  public static DimensionLevelingSettings get(ResourceKey<Level> dimension) {
    return SETTINGS.getOrDefault(dimension.identifier(), createDefaultSettings());
  }

  private static DimensionLevelingSettings createDefaultSettings() {
    return new DimensionLevelingSettings(
        Config.COMMON.startingLevel.get(),
        Config.COMMON.maxLevel.get(),
        Config.COMMON.levelsPerDistance.get().floatValue(),
        Config.COMMON.levelsPerDeepness.get().floatValue(),
        Config.COMMON.randomLevelBonus.get(),
        null,
        Config.COMMON.levelsPerDay.get().floatValue(),
        Config.COMMON.levelPowerPerDistance.get().floatValue(),
        Config.COMMON.levelPowerPerDeepness.get().floatValue(),
        Collections.emptyMap());
  }

  @Override
  protected Map<Identifier, JsonElement> prepare(
      @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
    Map<Identifier, JsonElement> jsonElements = new HashMap<>();
    FileToIdConverter converter = FileToIdConverter.json("leveling_settings/dimensions");
    for (Map.Entry<Identifier, Resource> entry :
        resourceManager
            .listResources("leveling_settings/dimensions", id -> id.getPath().endsWith(".json"))
            .entrySet()) {
      try (var reader = entry.getValue().openAsReader()) {
        jsonElements.put(converter.fileToId(entry.getKey()), JsonParser.parseReader(reader));
      } catch (Exception exception) {
        LOGGER.error("Couldn't read leveling settings {}", entry.getKey(), exception);
      }
    }
    return jsonElements;
  }

  @Override
  protected void apply(
      Map<Identifier, JsonElement> jsonElements,
      @NotNull ResourceManager resourceManager,
      @NotNull ProfilerFiller profilerFiller) {
    SETTINGS.clear();
    jsonElements.forEach(this::loadSettings);
  }

  private void loadSettings(Identifier fileId, JsonElement jsonElement) {
    try {
      JsonObject jsonObject = jsonElement.getAsJsonObject();
      DimensionLevelingSettings settings = DimensionLevelingSettings.load(jsonObject);
      SETTINGS.put(fileId, settings);
      LOGGER.info("Loaded leveling settings {}", fileId);
    } catch (Exception exception) {
      LOGGER.error("Couldn't load leveling settings {}", fileId);
      exception.printStackTrace();
    }
  }
}
