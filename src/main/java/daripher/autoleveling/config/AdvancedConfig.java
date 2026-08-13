package daripher.autoleveling.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.settings.AttributeBonus;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

public final class AdvancedConfig {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Path CONFIG_PATH =
      FMLPaths.CONFIGDIR.get().resolve("advancedleveling.json");

  private static Map<String, JsonElement> configuredAttributes = defaultAttributes();
  private static List<String> mobBlacklist = new ArrayList<>();
  private static List<String> mobWhitelist = new ArrayList<>();
  private static List<String> hiddenLevels = new ArrayList<>();
  private static Map<Attribute, AttributeBonus> resolvedAttributes;

  private AdvancedConfig() {}

  public static synchronized void load() {
    if (Files.notExists(CONFIG_PATH)) {
      configuredAttributes = defaultAttributes();
      mobBlacklist = new ArrayList<>();
      mobWhitelist = new ArrayList<>();
      hiddenLevels = new ArrayList<>();
      resolvedAttributes = null;
      save();
      return;
    }

    try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
      JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
      Map<String, JsonElement> attributes = readAttributes(root);
      JsonObject mobs = requireObject(root, "mobs");
      List<String> blacklist = readStringList(mobs, "blacklist");
      List<String> whitelist = readStringList(mobs, "whitelist");
      List<String> hidden = readStringList(mobs, "hidden_levels");

      configuredAttributes = attributes;
      mobBlacklist = blacklist;
      mobWhitelist = whitelist;
      hiddenLevels = hidden;
      resolvedAttributes = null;
      AutoLevelingMod.LOGGER.info("Loaded advanced configuration from {}", CONFIG_PATH);
    } catch (Exception exception) {
      AutoLevelingMod.LOGGER.error(
          "Could not load {}. Default advanced settings will be used; the file was not changed.",
          CONFIG_PATH,
          exception);
      configuredAttributes = defaultAttributes();
      mobBlacklist = new ArrayList<>();
      mobWhitelist = new ArrayList<>();
      hiddenLevels = new ArrayList<>();
      resolvedAttributes = null;
    }
  }

  public static synchronized void save() {
    JsonObject root = new JsonObject();
    JsonObject attributes = new JsonObject();
    configuredAttributes.forEach(attributes::add);
    root.add("attributes", attributes);

    JsonObject mobs = new JsonObject();
    mobs.add("blacklist", GSON.toJsonTree(mobBlacklist));
    mobs.add("whitelist", GSON.toJsonTree(mobWhitelist));
    mobs.add("hidden_levels", GSON.toJsonTree(hiddenLevels));
    root.add("mobs", mobs);

    try {
      Files.createDirectories(CONFIG_PATH.getParent());
      Path temporary = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".tmp");
      try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
        GSON.toJson(root, writer);
      }
      Files.move(temporary, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException exception) {
      AutoLevelingMod.LOGGER.error("Could not save advanced configuration to {}", CONFIG_PATH, exception);
    }
  }

  public static synchronized Map<Attribute, AttributeBonus> getAttributeBonuses() {
    if (resolvedAttributes != null) return resolvedAttributes;
    Map<Attribute, AttributeBonus> bonuses = new LinkedHashMap<>();
    configuredAttributes.forEach(
        (attributeId, value) -> {
          Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attributeId));
          if (attribute == null) {
            AutoLevelingMod.LOGGER.error("Attribute '{}' could not be found", attributeId);
            return;
          }
          AttributeBonus bonus =
              value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()
                  ? AttributeBonus.numeric(
                      value.getAsDouble(), AttributeModifier.Operation.MULTIPLY_BASE)
                  : AttributeBonus.expression(value.getAsString());
          bonuses.put(attribute, bonus);
        });
    resolvedAttributes = Collections.unmodifiableMap(bonuses);
    return resolvedAttributes;
  }

  public static synchronized List<String> getMobBlacklist() {
    return List.copyOf(mobBlacklist);
  }

  public static synchronized List<String> getMobWhitelist() {
    return List.copyOf(mobWhitelist);
  }

  public static synchronized List<String> getHiddenLevels() {
    return List.copyOf(hiddenLevels);
  }

  public static synchronized boolean toggleMobBlacklist(String entityId) {
    boolean added = toggle(mobBlacklist, entityId);
    save();
    return added;
  }

  public static synchronized boolean toggleMobWhitelist(String entityId) {
    boolean added = toggle(mobWhitelist, entityId);
    save();
    return added;
  }

  private static boolean toggle(List<String> values, String value) {
    if (values.remove(value)) return false;
    values.add(value);
    return true;
  }

  private static Map<String, JsonElement> readAttributes(JsonObject root) {
    JsonObject object = requireObject(root, "attributes");
    Map<String, JsonElement> attributes = new LinkedHashMap<>();
    object.entrySet()
        .forEach(
            entry -> {
              String id = entry.getKey();
              new ResourceLocation(id);
              JsonElement value = entry.getValue();
              boolean number = value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber();
              boolean expression = value.isJsonPrimitive() && value.getAsJsonPrimitive().isString();
              if (!number && !expression) {
                throw new IllegalArgumentException(
                    "Attribute '" + id + "' must have a numeric or string value");
              }
              if (expression) AttributeBonus.expression(value.getAsString());
              attributes.put(id, value.deepCopy());
            });
    return attributes;
  }

  private static JsonObject requireObject(JsonObject parent, String name) {
    JsonElement element = parent.get(name);
    if (element == null || !element.isJsonObject()) {
      throw new IllegalArgumentException("Missing object '" + name + "'");
    }
    return element.getAsJsonObject();
  }

  private static List<String> readStringList(JsonObject parent, String name) {
    JsonElement element = parent.get(name);
    if (element == null || !element.isJsonArray()) {
      throw new IllegalArgumentException("Missing array 'mobs." + name + "'");
    }
    List<String> values = new ArrayList<>();
    element
        .getAsJsonArray()
        .forEach(
            value -> {
              if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException("All values in 'mobs." + name + "' must be strings");
              }
              values.add(value.getAsString());
            });
    return values;
  }

  private static Map<String, JsonElement> defaultAttributes() {
    Map<String, JsonElement> attributes = new LinkedHashMap<>();
    attributes.put("minecraft:generic.movement_speed", GSON.toJsonTree(0.001));
    attributes.put("minecraft:generic.flying_speed", GSON.toJsonTree(0.001));
    attributes.put("minecraft:generic.attack_damage", GSON.toJsonTree(0.1));
    attributes.put("minecraft:generic.armor", GSON.toJsonTree(0.1));
    attributes.put("minecraft:generic.max_health", GSON.toJsonTree(0.1));
    attributes.put("autoleveling:monster.projectile_damage_bonus", GSON.toJsonTree(0.1));
    attributes.put("autoleveling:monster.explosion_damage_bonus", GSON.toJsonTree(0.1));
    return attributes;
  }
}
