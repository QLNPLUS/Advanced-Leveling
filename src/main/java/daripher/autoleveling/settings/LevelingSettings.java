package daripher.autoleveling.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import daripher.autoleveling.config.Config;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;

public interface LevelingSettings {

  int startingLevel();

  int maxLevel();

  float levelsPerDistance();

  float levelsPerDeepness();

  int randomLevelBonus();

  float levelsPerDay();

  float levelPowerPerDistance();

  float levelPowerPerDeepness();

  Map<Attribute, AttributeBonus> attributeModifiers();

  static Map<Attribute, AttributeBonus> readAttributeModifiers(JsonObject jsonObject) {
    if (!jsonObject.has("attribute_modifiers")) {
      return Map.of();
    }
    Map<Attribute, AttributeBonus> modifiers = new HashMap<>();
    JsonArray jsonPairs = jsonObject.get("attribute_modifiers").getAsJsonArray();
    jsonPairs.forEach(
        jsonElement -> {
          JsonObject elementJson = jsonElement.getAsJsonObject();
          Attribute attribute = readAttribute(elementJson);
          AttributeBonus modifier = readAttributeModifier(elementJson);
          modifiers.put(attribute, modifier);
        });
    return modifiers;
  }

  static Attribute readAttribute(JsonObject jsonObject) {
    Identifier attributeId = Identifier.parse(jsonObject.get("attribute").getAsString());
    return BuiltInRegistries.ATTRIBUTE.getValue(attributeId);
  }

  static AttributeBonus readAttributeModifier(JsonObject jsonObject) {
    if (jsonObject.has("expression")) {
      return AttributeBonus.expression(jsonObject.get("expression").getAsString());
    }
    if (jsonObject.get("amount").isJsonPrimitive()
        && jsonObject.getAsJsonPrimitive("amount").isString()) {
      return AttributeBonus.expression(jsonObject.get("amount").getAsString());
    }
    double amount = jsonObject.get("amount").getAsDouble();
    AttributeModifier.Operation operation =
        AttributeModifier.Operation.BY_ID.apply(jsonObject.get("operation").getAsInt());
    return AttributeBonus.numeric(amount, operation);
  }

  static @Nullable BlockPos readSpawnPosOverride(JsonObject jsonObject) {
    if (!jsonObject.has("spawn_pos_override")) return null;
    JsonObject posJson = jsonObject.get("spawn_pos_override").getAsJsonObject();
    int x = posJson.get("x").getAsInt();
    int y = posJson.get("y").getAsInt();
    int z = posJson.get("z").getAsInt();
    return new BlockPos(x, y, z);
  }

  static float readOptionalFloat(
      JsonObject jsonObject, String name, ModConfigSpec.ConfigValue<Double> alternative) {
    if (!jsonObject.has(name)) {
      return alternative.get().floatValue();
    }
    return jsonObject.get(name).getAsFloat();
  }

  static float readLevelsPerDay(JsonObject jsonObject) {
    return readOptionalFloat(jsonObject, "levels_per_day", Config.COMMON.levelsPerDay);
  }

  static float readLevelPowerPerDistance(JsonObject jsonObject) {
    return readOptionalFloat(
        jsonObject, "level_power_per_distance", Config.COMMON.levelPowerPerDistance);
  }

  static float readLevelPowerPerDeepness(JsonObject jsonObject) {
    return readOptionalFloat(
        jsonObject, "level_power_per_deepness", Config.COMMON.levelPowerPerDeepness);
  }
}
