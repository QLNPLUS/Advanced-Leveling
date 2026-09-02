package daripher.autoleveling.loot.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import daripher.autoleveling.event.MobsLevelingEvents;
import java.util.Set;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

public record LevelCheck(int min, int max) implements LootItemCondition {
  public static final MapCodec<LevelCheck> CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      Codec.INT.optionalFieldOf("min", 0).forGetter(LevelCheck::min),
                      Codec.INT.optionalFieldOf("max", 0).forGetter(LevelCheck::max))
                  .apply(instance, LevelCheck::new));

  public @NotNull MapCodec<LevelCheck> codec() {
    return CODEC;
  }

  public @NotNull Set<ContextKey<?>> getReferencedContextParams() {
    return Set.of(LootContextParams.THIS_ENTITY);
  }

  public boolean test(LootContext context) {
    if (!context.hasParameter(LootContextParams.THIS_ENTITY)) return false;
    Entity entity = context.getParameter(LootContextParams.THIS_ENTITY);
    if (!MobsLevelingEvents.hasLevel(entity)) return false;
    int level = MobsLevelingEvents.getLevel((LivingEntity) entity) + 1;
    return level >= min && level <= max;
  }

}
