package daripher.autoleveling.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalInt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Exercises the display rules behind {@link LevelingApi#displayLevel}. {@link LevelingApi#resolve}
 * is the branch-free half of that method: the entity overload only adds the player check and the
 * two reads (the raw {@code LEVEL} tag and the hidden-level policy).
 */
class LevelingApiTest {
  private static final String TAG = LevelingApi.LEVEL_TAG;

  @Test
  @DisplayName("the persisted key is the one the leveling events write")
  void persistedKeyIsSharedWithTheLevelingEvents() {
    // The API reads the same NBT key the rest of the mod writes; a rename on either side would
    // silently report "no level" for every leveled mob.
    assertEquals("LEVEL", TAG);
  }

  @Test
  @DisplayName("a stored zero is a real level and displays as 1")
  void zeroBaseDisplaysAsOne() {
    assertEquals(OptionalInt.of(1), LevelingApi.resolve(false, IntTag.valueOf(0), false));
  }

  @Test
  @DisplayName("stored levels above zero display one higher")
  void storedLevelsAboveZeroDisplayAlignedValues() {
    assertEquals(OptionalInt.of(2), LevelingApi.resolve(false, IntTag.valueOf(1), false));
    assertEquals(OptionalInt.of(5), LevelingApi.resolve(false, IntTag.valueOf(4), false));
    assertEquals(OptionalInt.of(1000), LevelingApi.resolve(false, IntTag.valueOf(999), false));
  }

  @Test
  @DisplayName("players never have an Advanced Leveling level")
  void playerIsAlwaysEmpty() {
    assertEquals(OptionalInt.empty(), LevelingApi.resolve(true, null, false));
    assertEquals(OptionalInt.empty(), LevelingApi.resolve(true, IntTag.valueOf(4), false));
    assertEquals(OptionalInt.empty(), LevelingApi.resolve(true, IntTag.valueOf(4), true));
  }

  @Test
  @DisplayName("a missing LEVEL tag is not a level")
  void missingTagIsEmpty() {
    assertEquals(OptionalInt.empty(), LevelingApi.resolve(false, null, false));
  }

  @Test
  @DisplayName("a LEVEL tag of the wrong type is not a level")
  void wrongTagTypeIsEmpty() {
    assertEquals(OptionalInt.empty(), LevelingApi.resolve(false, StringTag.valueOf("5"), false));
    assertEquals(OptionalInt.empty(), LevelingApi.resolve(false, LongTag.valueOf(5L), false));
    assertEquals(OptionalInt.empty(), LevelingApi.resolve(false, DoubleTag.valueOf(5.0D), false));
  }

  @Test
  @DisplayName("a negative stored level is rejected instead of being displayed")
  void negativeLevelIsEmpty() {
    assertEquals(OptionalInt.empty(), LevelingApi.resolve(false, IntTag.valueOf(-1), false));
    assertEquals(
        OptionalInt.empty(), LevelingApi.resolve(false, IntTag.valueOf(Integer.MIN_VALUE), false));
  }

  @Test
  @DisplayName("a stored level that overflows when incremented is rejected")
  void overflowingLevelIsEmpty() {
    assertEquals(
        OptionalInt.empty(),
        LevelingApi.resolve(false, IntTag.valueOf(Integer.MAX_VALUE), false));
    assertEquals(
        OptionalInt.of(Integer.MAX_VALUE),
        LevelingApi.resolve(false, IntTag.valueOf(Integer.MAX_VALUE - 1), false));
  }

  @Test
  @DisplayName("hidden entity types have no visible level")
  void hiddenTypeIsEmpty() {
    assertEquals(OptionalInt.empty(), LevelingApi.resolve(false, IntTag.valueOf(0), true));
    assertEquals(OptionalInt.empty(), LevelingApi.resolve(false, IntTag.valueOf(4), true));
  }

  @Test
  @DisplayName("reading a level does not change the entity data")
  void resolveDoesNotMutateTheTagSource() {
    CompoundTag data = new CompoundTag();
    data.putInt(TAG, 4);
    Tag before = data.get(TAG).copy();
    LevelingApi.resolve(false, data.get(TAG), false);
    assertEquals(before, data.get(TAG));
    assertEquals(4, data.getInt(TAG));
    assertTrue(data.contains(TAG));
  }

  @Test
  @DisplayName("an absent or non-integer level reads the same as an empty result")
  void absentAndWrongTypesAreIndistinguishableFromNoLevel() {
    CompoundTag data = new CompoundTag();
    assertFalse(data.contains(TAG));
    assertTrue(LevelingApi.resolve(false, data.get(TAG), false).isEmpty());
    data.putString(TAG, "4");
    assertTrue(LevelingApi.resolve(false, data.get(TAG), false).isEmpty());
    data.putInt(TAG, 4);
    assertEquals(OptionalInt.of(5), LevelingApi.resolve(false, data.get(TAG), false));
  }
}
