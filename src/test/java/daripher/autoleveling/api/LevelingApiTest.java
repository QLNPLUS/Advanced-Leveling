package daripher.autoleveling.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import daripher.autoleveling.LevelHolder;
import java.util.List;
import java.util.OptionalInt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Exercises the rules behind {@link LevelingApi#displayLevel} on this branch.
 *
 * <p>This branch stores the level in the {@link LevelHolder} field, so there is no NBT resolver to
 * test here: the checks are the player exclusion, the hidden policy, the {@link LevelingApi#NO_LEVEL}
 * sentinel and the fact that a level is only ever read.
 */
class LevelingApiTest {
  private static final String TAG = LevelingApi.LEVEL_TAG;

  /** Records whether the API touched the holder, so "read only" is an assertion, not a comment. */
  private static final class RecordingHolder implements LevelHolder {
    private final int level;
    private boolean read;
    private boolean written;

    private RecordingHolder(int level) {
      this.level = level;
    }

    @Override
    public int autoleveling$getLevel() {
      read = true;
      return level;
    }

    @Override
    public void autoleveling$setLevel(int level) {
      written = true;
    }
  }

  @Test
  @DisplayName("the persisted key and the no-level sentinel are the ones this branch already writes")
  void persistedKeyAndSentinelMatchTheExistingSaveFormat() {
    // The mixin writes the level under this key and uses -1 for "no level"; renaming either would
    // silently report "no level" for every leveled mob or make every mob look leveled.
    assertEquals("AdvancedLeveling", TAG);
    assertEquals(-1, LevelingApi.NO_LEVEL);
  }

  @Test
  @DisplayName("a holder without a level has no displayed level")
  void noLevelSentinelIsEmpty() {
    assertEquals(OptionalInt.empty(), LevelingApi.resolve(false, false, LevelingApi.NO_LEVEL));
  }

  @Test
  @DisplayName("a stored zero is a real level and stays zero")
  void zeroIsARealLevel() {
    assertEquals(OptionalInt.of(0), LevelingApi.resolve(false, false, 0));
    assertEquals(OptionalInt.of(0), LevelingApi.displayLevel(false, false, new RecordingHolder(0)));
  }

  @Test
  @DisplayName("stored levels are returned unchanged, without inventing an offset")
  void storedLevelsAreReturnedUnchanged() {
    assertEquals(OptionalInt.of(1), LevelingApi.resolve(false, false, 1));
    assertEquals(OptionalInt.of(5), LevelingApi.resolve(false, false, 5));
    assertEquals(OptionalInt.of(1000), LevelingApi.resolve(false, false, 1000));
    assertEquals(OptionalInt.of(7), LevelingApi.displayLevel(false, false, new RecordingHolder(7)));
  }

  @Test
  @DisplayName("players never have an Advanced Leveling level")
  void playerIsAlwaysEmpty() {
    assertEquals(OptionalInt.empty(), LevelingApi.resolve(true, false, LevelingApi.NO_LEVEL));
    assertEquals(OptionalInt.empty(), LevelingApi.resolve(true, false, 4));
    assertEquals(OptionalInt.empty(), LevelingApi.resolve(true, true, 4));
  }

  @Test
  @DisplayName("an excluded entity is never even read")
  void playerAndHiddenShortCircuitBeforeReading() {
    RecordingHolder player = new RecordingHolder(4);
    assertEquals(OptionalInt.empty(), LevelingApi.displayLevel(true, false, player));
    assertFalse(player.read, "the holder must not be read for a player");

    RecordingHolder hidden = new RecordingHolder(4);
    assertEquals(OptionalInt.empty(), LevelingApi.displayLevel(false, true, hidden));
    assertFalse(hidden.read, "the holder must not be read for a hidden type");
  }

  @Test
  @DisplayName("a negative stored value is rejected instead of being displayed")
  void negativeLevelIsEmpty() {
    assertEquals(OptionalInt.empty(), LevelingApi.resolve(false, false, -1));
    assertEquals(OptionalInt.empty(), LevelingApi.resolve(false, false, -2));
    assertEquals(OptionalInt.empty(), LevelingApi.resolve(false, false, Integer.MIN_VALUE));
  }

  @Test
  @DisplayName("the largest stored level is reported as-is, without overflow")
  void largestStoredLevelIsNotIncremented() {
    assertEquals(OptionalInt.of(Integer.MAX_VALUE), LevelingApi.resolve(false, false, Integer.MAX_VALUE));
    assertEquals(OptionalInt.of(Integer.MAX_VALUE - 1),
        LevelingApi.resolve(false, false, Integer.MAX_VALUE - 1));
  }

  @Test
  @DisplayName("hidden entity types have no visible level")
  void hiddenTypeIsEmpty() {
    assertEquals(OptionalInt.empty(), LevelingApi.resolve(false, true, 0));
    assertEquals(OptionalInt.empty(), LevelingApi.resolve(false, true, 4));
    assertEquals(OptionalInt.empty(), LevelingApi.displayLevel(false, true, new RecordingHolder(4)));
  }

  @Test
  @DisplayName("reading a level never writes one")
  void readingNeverWrites() {
    RecordingHolder holder = new RecordingHolder(4);
    assertEquals(OptionalInt.of(4), LevelingApi.displayLevel(false, false, holder));
    assertTrue(holder.read);
    assertFalse(holder.written, "displayLevel must not set the level");
    assertEquals(OptionalInt.of(4), LevelingApi.displayLevel(false, false, holder));
    assertFalse(holder.written);
  }

  @Test
  @DisplayName("a missing holder is not a level")
  void missingHolderIsEmpty() {
    assertEquals(OptionalInt.empty(), LevelingApi.displayLevel(false, false, null));
  }

  @Test
  @DisplayName("an exact hidden entry hides exactly that entity type")
  void hiddenExactEntry() {
    List<String> hidden = List.of("minecraft:zombie", "minecraft:skeleton");
    assertTrue(LevelingApi.isHidden(hidden, "minecraft:zombie"));
    assertTrue(LevelingApi.isHidden(hidden, "minecraft:skeleton"));
    assertFalse(LevelingApi.isHidden(hidden, "minecraft:creeper"));
    assertFalse(LevelingApi.isHidden(hidden, "minecraft:zombie_nautilus"));
  }

  @Test
  @DisplayName("a namespace wildcard hides the whole namespace and nothing else")
  void hiddenNamespaceWildcard() {
    List<String> hidden = List.of("minecraft:*");
    assertTrue(LevelingApi.isHidden(hidden, "minecraft:zombie"));
    assertTrue(LevelingApi.isHidden(hidden, "minecraft:skeleton"));
    assertFalse(LevelingApi.isHidden(hidden, "somemod:zombie"));

    List<String> oneNamespace = List.of("somemod:*", "minecraft:zombie");
    assertTrue(LevelingApi.isHidden(oneNamespace, "somemod:golem"));
    assertTrue(LevelingApi.isHidden(oneNamespace, "minecraft:zombie"));
    assertFalse(LevelingApi.isHidden(oneNamespace, "minecraft:skeleton"));
  }

  @Test
  @DisplayName("an empty hidden list hides nothing and there is no global wildcard")
  void hiddenEmptyAndNoGlobalWildcard() {
    assertFalse(LevelingApi.isHidden(List.of(), "minecraft:zombie"));
    assertFalse(LevelingApi.isHidden(List.of("*"), "minecraft:zombie"));
    assertFalse(LevelingApi.isHidden(List.of("minecraft:*"), "minecraft"));
    assertFalse(LevelingApi.isHidden(null, "minecraft:zombie"));
    assertFalse(LevelingApi.isHidden(List.of("minecraft:*"), null));
  }
}
