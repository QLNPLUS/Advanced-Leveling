package daripher.autoleveling.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Locks the two attribute value modes to the shape the published configuration documents, using
 * the pinned YiRanExpressionLib binary: a number is an amount per stored level, an expression is
 * the final target value and sees the displayed level (stored level + 1).
 */
class AttributeBonusTest {
  private static final double EPSILON = 1.0E-9D;

  @Test
  @DisplayName("numeric value mode keeps multiplying by the stored level")
  void numericModeMultipliesByStoredLevel() {
    AttributeBonus bonus =
        AttributeBonus.numeric(0.1D, AttributeModifier.Operation.MULTIPLY_BASE);
    assertEquals(0.0D, bonus.createModifier(20.0D, 0).getAmount(), EPSILON);
    assertEquals(0.3D, bonus.createModifier(20.0D, 3).getAmount(), EPSILON);
    // A numeric mode value is an amount per level, so a negative stored level stays a negative
    // amount; only the expression mode has a notion of an absolute target.
    assertEquals(-0.1D, bonus.createModifier(20.0D, -1).getAmount(), EPSILON);
    AttributeModifier modifier = bonus.createModifier(20.0D, 3);
    assertEquals(AttributeBonus.MODIFIER_ID, modifier.getId());
    assertEquals(AttributeModifier.Operation.MULTIPLY_BASE, modifier.getOperation());
  }

  @Test
  @DisplayName("an expression is the final target value, not an amount")
  void expressionModeReturnsTheFinalTargetValue() {
    AttributeBonus bonus = AttributeBonus.expression("base + (level - 1) * 10");
    // Stored level 0 is displayed as level 1, so the target is the untouched base value.
    assertEquals(0.0D, bonus.createModifier(20.0D, 0).getAmount(), EPSILON);
    // Stored level 4 is displayed as level 5: 20 + (5 - 1) * 10 = 60, i.e. +40 over base.
    assertEquals(40.0D, bonus.createModifier(20.0D, 4).getAmount(), EPSILON);
    assertEquals(20.0D, bonus.createModifier(20.0D, 2).getAmount(), EPSILON);
    assertEquals(
        AttributeModifier.Operation.ADDITION, bonus.createModifier(20.0D, 0).getOperation());
    assertEquals(AttributeBonus.MODIFIER_ID, bonus.createModifier(20.0D, 0).getId());
  }

  @Test
  @DisplayName("an expression sees the displayed level, so level 1 means stored 0")
  void expressionVariableLevelIsTheDisplayedLevel() {
    AttributeBonus bonus = AttributeBonus.expression("level");
    assertEquals(1.0D - 20.0D, bonus.createModifier(20.0D, 0).getAmount(), EPSILON);
    assertEquals(6.0D - 20.0D, bonus.createModifier(20.0D, 5).getAmount(), EPSILON);
  }

  @Test
  @DisplayName("base is the attribute base value the modifier is created for")
  void expressionVariableBaseIsTheAttributeBaseValue() {
    AttributeBonus bonus = AttributeBonus.expression("base * 2");
    // The modifier carries the delta from base to the expression target.
    assertEquals(20.0D, bonus.createModifier(20.0D, 0).getAmount(), EPSILON);
    assertEquals(20.0D, bonus.createModifier(20.0D, 7).getAmount(), EPSILON);
    assertEquals(3.0D, bonus.createModifier(3.0D, 0).getAmount(), EPSILON);
  }

  @Test
  @DisplayName("an expression may only use base and level")
  void unknownVariableIsRejected() {
    IllegalArgumentException failure =
        assertThrows(
            IllegalArgumentException.class, () -> AttributeBonus.expression("foo + base * level"));
    assertTrue(
        failure.getMessage().contains("base") && failure.getMessage().contains("level"),
        () -> "unexpected message: " + failure.getMessage());
  }

  @Test
  @DisplayName("an expression that evaluates to a non-finite value is rejected")
  void nonFiniteResultsAreRejected() {
    IllegalArgumentException infinite =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              AttributeBonus bonus = AttributeBonus.expression("base / 0");
              bonus.createModifier(20.0D, 0);
            });
    assertTrue(
        infinite.getMessage().contains("non-finite"),
        () -> "unexpected message: " + infinite.getMessage());

    IllegalArgumentException notANumber =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              AttributeBonus bonus = AttributeBonus.expression("sqrt(0 - 1)");
              bonus.createModifier(20.0D, 0);
            });
    assertTrue(
        notANumber.getMessage().contains("non-finite"),
        () -> "unexpected message: " + notANumber.getMessage());
  }

  @Test
  @DisplayName("a finite overflowing double is not reachable but a huge value stays finite")
  void largeFiniteValuesAreAccepted() {
    AttributeBonus bonus = AttributeBonus.expression("base + level * 1e10");
    double amount = bonus.createModifier(20.0D, 4).getAmount();
    assertTrue(Double.isFinite(amount));
    assertEquals(5.0E10D, amount, EPSILON);
  }
}
