package daripher.autoleveling.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import daripher.autoleveling.settings.EntityLevelingSettings;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class EntitiesLevelingSettingsReloader
    extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> {
  private static final Logger LOGGER = LogUtils.getLogger();
  private static final Gson GSON = new GsonBuilder().create();
  private static final Map<Identifier, EntityLevelingSettings> SETTINGS = new HashMap<>();

  public EntitiesLevelingSettingsReloader() {
  }

  @Nullable
  public static EntityLevelingSettings get(EntityType<?> entityType) {
    return SETTINGS.get(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
  }

  @Override
  protected Map<Identifier, JsonElement> prepare(
      @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
    Map<Identifier, JsonElement> jsonElements = new HashMap<>();
    FileToIdConverter converter = FileToIdConverter.json("leveling_settings/entities");
    for (Map.Entry<Identifier, Resource> entry :
        resourceManager
            .listResources("leveling_settings/entities", id -> id.getPath().endsWith(".json"))
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
      EntityLevelingSettings settings = EntityLevelingSettings.load(jsonObject);
      SETTINGS.put(fileId, settings);
      LOGGER.info("Loaded leveling settings {}", fileId);
    } catch (Exception exception) {
      LOGGER.error("Couldn't load leveling settings {}", fileId);
      exception.printStackTrace();
    }
  }
}
