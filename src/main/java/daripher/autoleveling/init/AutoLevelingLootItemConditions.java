package daripher.autoleveling.init;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.loot.condition.LevelCheck;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class AutoLevelingLootItemConditions {
  public static final DeferredRegister<LootItemConditionType> REGISTRY =
      DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, AutoLevelingMod.MOD_ID);

  public static final DeferredHolder<LootItemConditionType, LootItemConditionType> LEVEL_CHECK =
      REGISTRY.register("level_check", LevelCheck::createType);
}
