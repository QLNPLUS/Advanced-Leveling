package daripher.autoleveling.smoke;

import daripher.autoleveling.api.LevelingApi;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * Test-only server probe for the public {@link LevelingApi}. It is packaged as its own mod jar from
 * the {@code smoke} source set, is loaded only through the development run's {@code mods} folder, and
 * is never part of the distributable artifact.
 *
 * <p>It creates a real, unspawned {@link Zombie} inside the running dedicated server and calls the
 * public API on it, checking the reported level and that the persistent data the API reads is
 * unchanged by the read.
 */
@EventBusSubscriber(modid = ApiSmokeProbe.MOD_ID)
public final class ApiSmokeProbe {
  public static final String MOD_ID = "autoleveling_smoke";

  private ApiSmokeProbe() {}

  @SubscribeEvent
  public static void onServerStarted(ServerStartedEvent event) {
    run(event.getServer());
  }

  private static void run(MinecraftServer server) {
    List<String> cases = new ArrayList<>();
    boolean ok = true;
    try {
      Zombie zombie = EntityType.ZOMBIE.create(server.overworld(), EntitySpawnReason.COMMAND);
      if (zombie == null) throw new IllegalStateException("could not create a zombie");

      boolean noLevelEmpty = LevelingApi.displayLevel(zombie).isEmpty();
      cases.add("no_level{stored=absent,empty=" + noLevelEmpty + "}");
      ok &= noLevelEmpty;

      ok &= checkLevel(zombie, 0, 1, cases);
      ok &= checkLevel(zombie, 4, 5, cases);

      System.out.println(
          "AL_SMOKE {\"probe\":\"neoforge-26.1.2\",\"mod_source\":\""
              + LevelingApi.class.getProtectionDomain().getCodeSource().getLocation()
              + "\",\"entity\":\"minecraft:zombie\",\"cases\":["
              + String.join(",", cases.stream().map(c -> "\"" + c + "\"").toList())
              + "],\"ok\":" + ok + "}");
    } catch (Throwable failure) {
      System.out.println(
          "AL_SMOKE {\"probe\":\"neoforge-26.1.2\",\"ok\":false,\"error\":\"" + failure + "\"}");
      failure.printStackTrace();
    }
  }

  private static boolean checkLevel(Zombie zombie, int stored, int expected, List<String> cases) {
    CompoundTag data = zombie.getPersistentData();
    data.putInt(LevelingApi.LEVEL_TAG, stored);
    CompoundTag before = data.copy();
    var reported = LevelingApi.displayLevel(zombie);
    CompoundTag after = data.copy();
    boolean dataUnchanged = before.equals(after);
    boolean storedUnchanged = after.getIntOr(LevelingApi.LEVEL_TAG, Integer.MIN_VALUE) == stored;
    boolean reportedExpected = reported.isPresent() && reported.getAsInt() == expected;
    cases.add(
        "stored_"
            + stored
            + "{reported="
            + reported.orElse(Integer.MIN_VALUE)
            + ",expected="
            + expected
            + ",persistent_data_unchanged="
            + dataUnchanged
            + ",stored_level_after="
            + after.getIntOr(LevelingApi.LEVEL_TAG, Integer.MIN_VALUE)
            + "}");
    return dataUnchanged && storedUnchanged && reportedExpected;
  }
}
