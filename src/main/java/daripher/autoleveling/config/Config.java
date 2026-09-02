package daripher.autoleveling.config;

public final class Config {
  public static final Value<Integer> STARTING_LEVEL = new Value<>(1);
  public static final Value<Integer> MAX_LEVEL = new Value<>(0);
  public static final Value<Integer> RANDOM_LEVEL_BONUS = new Value<>(0);
  public static final Value<Double> LEVELS_PER_DISTANCE = new Value<>(0.01D);
  public static final Value<Double> LEVELS_PER_DEEPNESS = new Value<>(0.0D);
  public static final Value<Double> LEVELS_PER_DAY = new Value<>(0.0D);
  public static final Value<Double> LEVEL_POWER_PER_DISTANCE = new Value<>(0.0D);
  public static final Value<Double> LEVEL_POWER_PER_DEEPNESS = new Value<>(0.0D);
  public static final Value<Boolean> ALWAYS_SHOW_LEVEL = new Value<>(false);
  public static final Value<Boolean> SHOW_LEVEL_WHEN_LOOKING_AT = new Value<>(true);

  private Config() {}

  public static final class Value<T> {
    private T value;

    private Value(T value) {
      this.value = value;
    }

    public T get() {
      return value;
    }

    public void set(T value) {
      this.value = value;
    }
  }
}
