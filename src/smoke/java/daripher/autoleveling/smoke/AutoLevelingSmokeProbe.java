package daripher.autoleveling.smoke;

import daripher.autoleveling.api.LevelingApi;
import daripher.autoleveling.settings.AttributeBonus;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

/**
 * Test-only dedicated-server probe. It reads the public API, records what it observed, writes a
 * machine-readable result next to the run directory and stops the server it was started with.
 *
 * <p>This class is compiled in the {@code smoke} sourceset only and is never part of the mod jar.
 */
@Mod(AutoLevelingSmokeProbe.MOD_ID)
@Mod.EventBusSubscriber(modid = AutoLevelingSmokeProbe.MOD_ID)
public final class AutoLevelingSmokeProbe {
  static final String MOD_ID = "autoleveling_smoke";
  private static final List<String[]> CHECKS = new ArrayList<>();

  public AutoLevelingSmokeProbe() {}

  @SubscribeEvent
  public static void onServerStarted(ServerStartedEvent event) {
    int failures;
    try {
      failures = runChecks(event.getServer().getLevel(Level.OVERWORLD));
    } catch (Throwable throwable) {
      add("probe_completed", false, throwable.getClass().getName() + ": " + throwable.getMessage());
      failures = countFailures();
    }
    writeResult(failures);
    System.out.println("AUTOLEVELING_SMOKE_RESULT=" + (failures == 0 ? "PASS" : "FAIL"));
    event.getServer().halt(false);
  }

  private static int runChecks(ServerLevel level) {
    add("dedicated_server_started", level != null, "overworld=" + (level != null));
    add("library_loaded", ModList.get().isLoaded("yiranexpressionlib"),
        "yiranexpressionlib mod state=" + ModList.get().isLoaded("yiranexpressionlib"));
    add("level_tag_shared", "LEVEL".equals(LevelingApi.LEVEL_TAG), "LEVEL_TAG=" + LevelingApi.LEVEL_TAG);
    add("null_entity_empty", LevelingApi.displayLevel(null).isEmpty(), "null entity");
    if (level == null) return countFailures();

    LivingEntity entity = EntityType.ZOMBIE.create(level);
    if (entity == null) {
      add("entity_created", false, "EntityType.ZOMBIE.create returned null");
      return countFailures();
    }
    add("entity_created", true, "entity=" + entity.getType().getDescriptionId());
    add("entity_not_spawned", level.getEntity(entity.getUUID()) == null,
        "level.getEntity(uuid)=" + level.getEntity(entity.getUUID()));

    CompoundTag data = entity.getPersistentData();
    String before = data.toString();
    add("missing_level_empty", LevelingApi.displayLevel(entity).isEmpty(), "no LEVEL tag");
    add("read_does_not_mutate", before.equals(data.toString()), "persistent data unchanged");

    data.putInt(LevelingApi.LEVEL_TAG, 0);
    add("zero_stored_is_one", equalsLevel(LevelingApi.displayLevel(entity), 1), describe(entity));

    data.putInt(LevelingApi.LEVEL_TAG, 4);
    String beforeRead = data.toString();
    OptionalInt displayed = LevelingApi.displayLevel(entity);
    add("stored_four_is_five", equalsLevel(displayed, 5), describe(entity));
    add("leveled_read_does_not_mutate", beforeRead.equals(data.toString()), "persistent data unchanged");

    data.putInt(LevelingApi.LEVEL_TAG, -1);
    add("negative_is_empty", LevelingApi.displayLevel(entity).isEmpty(), describe(entity));

    data.putString(LevelingApi.LEVEL_TAG, "4");
    add("wrong_type_is_empty", LevelingApi.displayLevel(entity).isEmpty(), "LEVEL is a string");

    data.putInt(LevelingApi.LEVEL_TAG, Integer.MAX_VALUE);
    add("overflow_is_empty", LevelingApi.displayLevel(entity).isEmpty(), "LEVEL=Integer.MAX_VALUE");

    data.remove(LevelingApi.LEVEL_TAG);
    add("removed_level_empty", LevelingApi.displayLevel(entity).isEmpty(), "LEVEL removed");

    add("hidden_policy_readable", !LevelingApi.isHidden(EntityType.ZOMBIE),
        "isHidden(minecraft:zombie)=" + LevelingApi.isHidden(EntityType.ZOMBIE));

    AttributeBonus bonus = AttributeBonus.expression("base + (level - 1) * 10");
    AttributeModifier modifier = bonus.createModifier(20.0D, 4);
    add("expression_library_usable", Math.abs(modifier.getAmount() - 40.0D) < 1.0E-9D,
        "amount=" + modifier.getAmount());
    return countFailures();
  }

  private static boolean equalsLevel(OptionalInt value, int expected) {
    return value.isPresent() && value.getAsInt() == expected;
  }

  private static String describe(LivingEntity entity) {
    return "displayLevel=" + LevelingApi.displayLevel(entity)
        + " stored=" + entity.getPersistentData().get(LevelingApi.LEVEL_TAG);
  }

  private static void add(String id, boolean ok, String detail) {
    CHECKS.add(new String[] {id, Boolean.toString(ok), detail});
  }

  private static int countFailures() {
    int failures = 0;
    for (String[] check : CHECKS) {
      if (!"true".equals(check[1])) failures++;
    }
    return failures;
  }

  private static String escape(String value) {
    if (value == null) return "";
    return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
  }

  private static void writeResult(int failures) {
    StringBuilder json = new StringBuilder();
    json.append('{');
    json.append("\"probe\":\"autoleveling-smoke\",");
    json.append("\"loader\":\"forge\",");
    json.append("\"minecraft\":\"1.19.2\",");
    json.append("\"java\":\"").append(escape(System.getProperty("java.version"))).append("\",");
    json.append("\"result\":\"").append(failures == 0 ? "PASS" : "FAIL").append("\",");
    json.append("\"failures\":").append(failures).append(',');
    json.append("\"checks\":[");
    for (int index = 0; index < CHECKS.size(); index++) {
      String[] check = CHECKS.get(index);
      if (index > 0) json.append(',');
      json.append("{\"id\":\"").append(escape(check[0])).append("\",");
      json.append("\"ok\":").append(check[1]).append(',');
      json.append("\"detail\":\"").append(escape(check[2])).append("\"}");
    }
    json.append("]}");
    try {
      Files.writeString(Path.of("smoke-result.json"), json + System.lineSeparator(),
          StandardCharsets.UTF_8);
    } catch (Exception exception) {
      System.out.println("AUTOLEVELING_SMOKE_RESULT_WRITE_ERROR=" + exception);
    }
  }
}
