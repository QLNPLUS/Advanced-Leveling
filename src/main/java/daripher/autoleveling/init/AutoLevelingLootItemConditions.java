package daripher.autoleveling.init;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.loot.condition.LevelCheck;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class AutoLevelingLootItemConditions {
  public static final DeferredRegister<MapCodec<? extends LootItemCondition>> REGISTRY =
      DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, AutoLevelingMod.MOD_ID);

  public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<LevelCheck>>
      LEVEL_CHECK = REGISTRY.register("level_check", () -> LevelCheck.CODEC);
}
