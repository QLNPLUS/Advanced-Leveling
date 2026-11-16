package daripher.autoleveling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import net.yiran.expressionlib.expr.Expression;
import net.yiran.expressionlib.expr.ExpressionBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Locks the behaviour of the canonical YiRanExpressionLib binary this artifact bundles.
 *
 * <p>The Fabric artifact compiles only the numeric leveling path ({@code FabricLevelingHooks}); it
 * does not compile the JSON {@code attributes} expression settings that the NeoForge branch has.
 * These tests therefore prove what the bundled library does with the {@code base} / {@code level}
 * variables an expression would use, and deliberately do not pretend that a mod-side expression
 * configuration is active here.
 */
class ExpressionLibraryTest {
  private static final double EPSILON = 1.0E-9D;

  private static Expression expression(String source) {
    return new ExpressionBuilder(source).variables("base", "level").build();
  }

  @Test
  @DisplayName("an expression evaluates the base and level variables it was built with")
  void evaluatesDeclaredVariables() {
    Expression scaled = expression("base + (level - 1) * 10");
    assertEquals(20.0D, scaled.evaluate(Map.of("base", 20.0D, "level", 1.0D)), EPSILON);
    assertEquals(60.0D, scaled.evaluate(Map.of("base", 20.0D, "level", 5.0D)), EPSILON);
    assertEquals(5.0D, expression("level").evaluate(Map.of("base", 20.0D, "level", 5.0D)), EPSILON);
  }

  @Test
  @DisplayName("evaluating twice with different variables does not leak state between calls")
  void evaluationIsStateless() {
    Expression expression = expression("base * 2");
    assertEquals(40.0D, expression.evaluate(Map.of("base", 20.0D, "level", 0.0D)), EPSILON);
    assertEquals(6.0D, expression.evaluate(Map.of("base", 3.0D, "level", 0.0D)), EPSILON);
    assertEquals("base * 2", expression.getExpressionString());
  }

  @Test
  @DisplayName("the library reports every variable an expression mentions")
  void reportsMentionedVariables() {
    // This is what a caller needs to reject names it does not offer; the rejection itself is the
    // caller's job, and on this branch no compiled class performs it.
    Set<String> names = expression("foo + base * level").getVariableNames();
    assertTrue(names.contains("foo"));
    assertTrue(names.contains("base"));
    assertTrue(names.contains("level"));
    assertEquals(Set.of("base", "level"), expression("base + level").getVariableNames());
  }

  @Test
  @DisplayName("a non-finite result is observable, so a caller can refuse to apply it")
  void nonFiniteResultsAreObservable() {
    double infinite = expression("base / 0").evaluate(Map.of("base", 20.0D, "level", 1.0D));
    assertFalse(Double.isFinite(infinite));
    double notANumber = expression("0 * base / 0").evaluate(Map.of("base", 20.0D, "level", 1.0D));
    assertFalse(Double.isFinite(notANumber));
    assertTrue(Double.isFinite(expression("base + level").evaluate(Map.of("base", 20.0D, "level", 1.0D))));
  }
}
