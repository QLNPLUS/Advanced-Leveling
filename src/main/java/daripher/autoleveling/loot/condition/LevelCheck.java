package daripher.autoleveling.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import daripher.autoleveling.event.MobsLevelingEvents;
import daripher.autoleveling.init.AutoLevelingLootItemConditions;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
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

  public static LootItemConditionType createType() {
    return new LootItemConditionType(CODEC);
  }

  public @NotNull LootItemConditionType getType() {
    return AutoLevelingLootItemConditions.LEVEL_CHECK.get();
  }

  public @NotNull Set<LootContextParam<?>> getReferencedContextParams() {
    return ImmutableSet.of(LootContextParams.THIS_ENTITY);
  }

  public boolean test(LootContext context) {
    if (!context.hasParam(LootContextParams.THIS_ENTITY)) return false;
    Entity entity = context.getParam(LootContextParams.THIS_ENTITY);
    if (!MobsLevelingEvents.hasLevel(entity)) return false;
    int level = MobsLevelingEvents.getLevel((LivingEntity) entity) + 1;
    return level >= min && level <= max;
  }

}
