package daripher.autoleveling.smoke;

import daripher.autoleveling.LevelHolder;
import daripher.autoleveling.api.LevelingApi;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;

/**
 * Test-only server probe for the public {@link LevelingApi}. It is packaged as its own mod jar from
 * the {@code smoke} source set, is loaded only through the development run's {@code mods} folder, and
 * is never part of the distributable artifact.
 *
 * <p>It creates a real, unspawned {@link ZombieEntity} inside the running dedicated server and calls the
 * public API on it, checking that the stored level is reported and that neither the holder field nor
 * the serialized entity data changes because of the read.
 */
public final class ApiSmokeProbe implements ModInitializer {
  public static final String MOD_ID = "autoleveling_smoke";

  @Override
  public void onInitialize() {
    ServerLifecycleEvents.SERVER_STARTED.register(ApiSmokeProbe::run);
  }

  private static void run(MinecraftServer server) {
    List<String> cases = new ArrayList<>();
    boolean ok = true;
    try {
      ZombieEntity zombie = EntityType.ZOMBIE.create(server.getOverworld());
      if (zombie == null) throw new IllegalStateException("could not create a zombie");
      LevelHolder holder = (LevelHolder) zombie;

      // A freshly created entity has no level: the holder sentinel is -1 and the API must say so.
      int sentinel = holder.autoleveling$getLevel();
      boolean noLevelEmpty = LevelingApi.displayLevel(zombie).isEmpty();
      cases.add("no_level{sentinel=-1,reported=" + sentinel + ",empty=" + noLevelEmpty + "}");
      ok &= sentinel == -1 && noLevelEmpty;

      ok &= checkLevel(zombie, holder, 0, cases);
      ok &= checkLevel(zombie, holder, 4, cases);

      System.out.println(
          "AL_SMOKE {\"probe\":\"fabric\",\"mod_source\":\""
              + LevelingApi.class.getProtectionDomain().getCodeSource().getLocation()
              + "\",\"entity\":\"minecraft:zombie\",\"cases\":["
              + String.join(",", cases.stream().map(c -> "\"" + c + "\"").toList())
              + "],\"ok\":" + ok + "}");
    } catch (Throwable failure) {
      System.out.println(
          "AL_SMOKE {\"probe\":\"fabric\",\"ok\":false,\"error\":\"" + failure + "\"}");
      failure.printStackTrace();
    }
  }

  private static boolean checkLevel(
      ZombieEntity zombie, LevelHolder holder, int stored, List<String> cases) {
    holder.autoleveling$setLevel(stored);
    NbtCompound before = zombie.writeNbt(new NbtCompound());
    int holderBefore = holder.autoleveling$getLevel();
    var reported = LevelingApi.displayLevel(zombie);
    int holderAfter = holder.autoleveling$getLevel();
    NbtCompound after = zombie.writeNbt(new NbtCompound());
    boolean serializedUnchanged = before.equals(after);
    boolean holderUnchanged = holderBefore == stored && holderAfter == stored;
    boolean reportedStored = reported.isPresent() && reported.getAsInt() == stored;
    cases.add("stored_" + stored + "{reported=" + reported.orElse(Integer.MIN_VALUE)
        + ",holder_before=" + holderBefore + ",holder_after=" + holderAfter
        + ",serialized_unchanged=" + serializedUnchanged
        + ",saved_level=" + after.getInt(LevelingApi.LEVEL_TAG) + "}");
    return serializedUnchanged && holderUnchanged && reportedStored;
  }
}
