package daripher.autoleveling.config;

import com.electronwill.nightconfig.toml.TomlWriter;
import daripher.autoleveling.AutoLevelingMod;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public final class ConfigFormatter {
  private ConfigFormatter() {}

  public static void formatCommonConfig(ModConfigEvent event) {
    ModConfig config = event.getConfig();
    if (!AutoLevelingMod.MOD_ID.equals(config.getModId())) return;
    if (config.getType() != ModConfig.Type.COMMON) return;

    try {
      TomlWriter writer = new TomlWriter();
      writer.setIndentArrayElementsPredicate(ConfigFormatter::shouldWriteMultiline);
      StringWriter output = new StringWriter();
      writer.write(config.getConfigData(), output);

      Path configPath = config.getFullPath();
      String formatted = output.toString();
      if (Files.readString(configPath, StandardCharsets.UTF_8).equals(formatted)) return;

      Path temporaryPath = configPath.resolveSibling(configPath.getFileName() + ".tmp");
      Files.writeString(temporaryPath, formatted, StandardCharsets.UTF_8);
      try {
        Files.move(
            temporaryPath,
            configPath,
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException exception) {
        Files.move(temporaryPath, configPath, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (Exception exception) {
      AutoLevelingMod.LOGGER.error("Could not format common config", exception);
    }
  }

  private static boolean shouldWriteMultiline(List<?> list) {
    if (list.isEmpty()) return false;
    return list.stream().allMatch(List.class::isInstance)
        || list.stream().allMatch(String.class::isInstance);
  }
}
