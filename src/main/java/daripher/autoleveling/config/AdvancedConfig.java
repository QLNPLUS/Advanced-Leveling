package daripher.autoleveling.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class AdvancedConfig {
  private static final Path CONFIG_PATH =
      FabricLoader.getInstance().getConfigDir().resolve("advancedleveling.json");
  private static final List<String> MOB_BLACKLIST = new ArrayList<>();
  private static final List<String> MOB_WHITELIST = new ArrayList<>();
  private static final List<String> HIDDEN_LEVELS = new ArrayList<>();

  private AdvancedConfig() {}

  public static synchronized void load() {
    if (Files.notExists(CONFIG_PATH)) return;
    try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
      JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
      JsonObject mobs = root.has("mobs") ? root.getAsJsonObject("mobs") : new JsonObject();
      replace(MOB_BLACKLIST, readList(mobs.get("blacklist")));
      replace(MOB_WHITELIST, readList(mobs.get("whitelist")));
      replace(HIDDEN_LEVELS, readList(mobs.get("hidden_levels")));
    } catch (Exception ignored) {
      MOB_BLACKLIST.clear();
      MOB_WHITELIST.clear();
      HIDDEN_LEVELS.clear();
    }
  }

  public static boolean shouldLevel(String entityId) {
    if (MOB_BLACKLIST.contains(entityId)) return false;
    return MOB_WHITELIST.isEmpty() || MOB_WHITELIST.contains(entityId);
  }

  public static List<String> getMobBlacklist() {
    return List.copyOf(MOB_BLACKLIST);
  }

  public static List<String> getMobWhitelist() {
    return List.copyOf(MOB_WHITELIST);
  }

  public static List<String> getHiddenLevels() {
    return List.copyOf(HIDDEN_LEVELS);
  }

  public static synchronized boolean toggleMobBlacklist(String entityId) {
    boolean added = toggle(MOB_BLACKLIST, entityId);
    save();
    return added;
  }

  public static synchronized boolean toggleMobWhitelist(String entityId) {
    boolean added = toggle(MOB_WHITELIST, entityId);
    save();
    return added;
  }

  private static boolean toggle(List<String> values, String value) {
    if (values.remove(value)) return false;
    values.add(value);
    return true;
  }

  private static void replace(List<String> target, List<String> values) {
    target.clear();
    target.addAll(values);
  }

  private static List<String> readList(JsonElement element) {
    if (element == null || !element.isJsonArray()) return new ArrayList<>();
    List<String> values = new ArrayList<>();
    for (JsonElement value : element.getAsJsonArray()) {
      if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
        values.add(value.getAsString());
      }
    }
    return values;
  }

  private static void save() {
    JsonObject root = new JsonObject();
    JsonObject mobs = new JsonObject();
    mobs.add("blacklist", arrayOf(MOB_BLACKLIST));
    mobs.add("whitelist", arrayOf(MOB_WHITELIST));
    mobs.add("hidden_levels", arrayOf(HIDDEN_LEVELS));
    root.add("mobs", mobs);
    try {
      Files.createDirectories(CONFIG_PATH.getParent());
      try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
        writer.write(root.toString());
      }
    } catch (IOException ignored) {
      // Configuration changes remain active in memory for the current session.
    }
  }

  private static JsonArray arrayOf(List<String> values) {
    JsonArray array = new JsonArray();
    values.forEach(array::add);
    return array;
  }
}
