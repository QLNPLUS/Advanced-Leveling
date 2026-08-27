package daripher.autoleveling.config;

import daripher.autoleveling.client.LevelPlatePos;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class Config {
  public static final Config.Common COMMON;
  public static final ModConfigSpec COMMON_SPEC;
  public static final Config.Client CLIENT;
  public static final ModConfigSpec CLIENT_SPEC;

  static {
    Pair<Config.Common, ModConfigSpec> commonSpec =
        new ModConfigSpec.Builder().configure(Config.Common::new);
    COMMON_SPEC = commonSpec.getRight();
    COMMON = commonSpec.getLeft();
    Pair<Config.Client, ModConfigSpec> clientSpec =
        new ModConfigSpec.Builder().configure(Config.Client::new);
    CLIENT_SPEC = clientSpec.getRight();
    CLIENT = clientSpec.getLeft();
  }

  public static void register(ModContainer container) {
    container.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC, "advancedleveling-common.toml");
    container.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC, "advancedleveling-client.toml");
  }

  public static class Common {
    public final ConfigValue<Integer> startingLevel;
    public final ConfigValue<Integer> maxLevel;
    public final ConfigValue<Integer> randomLevelBonus;
    public final ConfigValue<Double> expBonus;
    public final ConfigValue<Double> levelsPerDistance;
    public final ConfigValue<Double> levelsPerDeepness;
    public final ConfigValue<Double> levelsPerDay;
    public final ConfigValue<Double> levelPowerPerDistance;
    public final ConfigValue<Double> levelPowerPerDeepness;
    public final ConfigValue<Boolean> alwaysShowLevel;
    public final ConfigValue<Boolean> showLevelWhenLookingAt;

    public Common(ModConfigSpec.Builder builder) {
      builder.push("Mobs");
      alwaysShowLevel = builder.define("Always show mobs levels", false);
      showLevelWhenLookingAt = builder.define("Only show levels when you look at the mob", true);
      expBonus = builder.define("Bonus experience per level", 0.1D);
      builder.pop();
      builder.push("Default levelling settings");
      startingLevel = builder.define("Starting level", 1);
      builder.comment("If this is equal to 0, there will be no maximum level");
      maxLevel = builder.define("Maximum level", 0);
      levelsPerDistance = builder.define("Level increase per one block distance from spawn", 0.01D);
      levelsPerDeepness =
          builder.define("Level increase per one block deepness below sea level", 0.0D);
      builder.comment(
          "If this is higher than 0, the level of monsters will be randomly increased by value between 0 and this value");
      randomLevelBonus = builder.define("Random level bonus", 0);
      builder.comment(
          "If this is higher than 0, mobs level will increase every day by specified amount");
      levelsPerDay = builder.define("Level bonus per day", 0d);
      builder.comment("Exponential level increase with distance");
      levelPowerPerDistance = builder.define("level_power_per_distance", 0d);
      builder.comment("Exponential level increase with deepness");
      levelPowerPerDeepness = builder.define("level_power_per_deepness", 0d);
      builder.pop();
    }
  }

  public static class Client {
    private static int level_text_color = -1;
    public final ConfigValue<String> levelTextColor;
    public final ConfigValue<LevelPlatePos> levelTextPosition;
    public final ConfigValue<Integer> levelTextShiftX;
    public final ConfigValue<Integer> levelTextShiftY;

    public Client(ModConfigSpec.Builder builder) {
      builder.push("Visuals");
      levelTextColor = builder.define("Level text color", "#1cff27", Config.Client::isColorString);
      levelTextPosition = builder.defineEnum("Level text position", LevelPlatePos.LEFT);
      levelTextShiftX = builder.define("Level text shift x", 0);
      levelTextShiftY = builder.define("Level text shift y", 0);
      builder.pop();
    }

    private static boolean isColorString(Object object) {
      if (!(object instanceof String string)) return false;
      if (!string.startsWith("#")) return false;
      string = string.substring(1);
      try {
        Integer.parseInt(string, 16);
        return true;
      } catch (NumberFormatException exception) {
        return false;
      }
    }

    public static int getLevelTextColor() {
      if (level_text_color == -1) {
        String color = CLIENT.levelTextColor.get().substring(1);
        level_text_color = Integer.parseInt(color, 16);
      }
      return level_text_color;
    }
  }
}
