package daripher.autoleveling.init;

import daripher.autoleveling.AutoLevelingMod;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class AutoLevelingAttributes {
  public static final DeferredRegister<Attribute> REGISTRY =
      DeferredRegister.create(BuiltInRegistries.ATTRIBUTES, AutoLevelingMod.MOD_ID);

  public static final DeferredHolder<Attribute, Attribute> PROJECTILE_DAMAGE_MULTIPLIER =
      rangedAttribute("monster", "projectile_damage_bonus", 1, 1, 1000);
  public static final DeferredHolder<Attribute, Attribute> EXPLOSION_DAMAGE_MULTIPLIER =
      rangedAttribute("monster", "explosion_damage_bonus", 1, 1, 1000);

  private static DeferredHolder<Attribute, Attribute> rangedAttribute(
      String category, String name, double defaultValue, double minValue, double maxValue) {
    return REGISTRY.register(
        category + "." + name,
        () ->
            new RangedAttribute(category + "." + name, defaultValue, minValue, maxValue)
                .setSyncable(true));
  }
}
