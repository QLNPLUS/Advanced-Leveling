package daripher.autoleveling.settings;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.yiran.expressionlib.expr.Expression;
import net.yiran.expressionlib.expr.ExpressionBuilder;

public final class AttributeBonus {
  public static final UUID MODIFIER_ID =
      UUID.fromString("6a102cb4-d735-4cb7-8ab2-3d383219a44e");
  private static final Set<String> VARIABLES = Set.of("base", "level");

  private final double amountPerLevel;
  private final AttributeModifier.Operation operation;
  private final Expression expression;

  private AttributeBonus(
      double amountPerLevel, AttributeModifier.Operation operation, Expression expression) {
    this.amountPerLevel = amountPerLevel;
    this.operation = operation;
    this.expression = expression;
  }

  public static AttributeBonus numeric(double amount, AttributeModifier.Operation operation) {
    return new AttributeBonus(amount, operation, null);
  }

  public static AttributeBonus expression(String source) {
    Expression expression = new ExpressionBuilder(source).variables("base", "level").build();
    if (!VARIABLES.containsAll(expression.getVariableNames())) {
      throw new IllegalArgumentException(
          "Attribute expression may only use variables 'base' and 'level': " + source);
    }
    return new AttributeBonus(0, AttributeModifier.Operation.ADDITION, expression);
  }

  public AttributeModifier createModifier(double base, int storedLevel) {
    double amount;
    if (expression == null) {
      amount = amountPerLevel * storedLevel;
    } else {
      double target =
          expression.evaluate(Map.of("base", base, "level", (double) storedLevel + 1));
      if (!Double.isFinite(target)) {
        throw new IllegalArgumentException(
            "Attribute expression returned a non-finite value: " + expression.getExpressionString());
      }
      amount = target - base;
    }
    return new AttributeModifier(MODIFIER_ID, "AutoLeveling", amount, operation);
  }
}
