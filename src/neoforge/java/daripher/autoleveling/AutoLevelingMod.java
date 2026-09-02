package daripher.autoleveling;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import org.slf4j.Logger;

@Mod(AutoLevelingMod.MOD_ID)
public final class AutoLevelingMod {
  public static final String MOD_ID = "autoleveling";
  public static final Logger LOGGER = LogUtils.getLogger();
  private static final Identifier LEVEL_MODIFIER = Identifier.fromNamespaceAndPath(MOD_ID, "level");
  private static final ResourceKey<LootTable> ADDITIONAL_LOOT = ResourceKey.create(Registries.LOOT_TABLE,
      Identifier.fromNamespaceAndPath(MOD_ID, "gameplay/leveled_mobs"));
  public static final ModConfigSpec COMMON_SPEC;
  public static final ModConfigSpec.BooleanValue ENABLE_ADDITIONAL_LOOT;
  public static final ModConfigSpec.IntValue STARTING_LEVEL;
  public static final ModConfigSpec.IntValue LEVEL_BONUS;
  static {
    ModConfigSpec.Builder b = new ModConfigSpec.Builder();
    b.push("Leveling");
    STARTING_LEVEL = b.comment("Initial level for newly spawned mobs").defineInRange("starting_level", 1, 0, 100000);
    LEVEL_BONUS = b.comment("Global level bonus").defineInRange("level_bonus", 0, -100000, 100000);
    ENABLE_ADDITIONAL_LOOT = b.comment("Generate the separate leveled_mobs loot table after normal drops").define("enable_additional_loot", true);
    b.pop();
    COMMON_SPEC = b.build();
  }

  public AutoLevelingMod(ModContainer container) {
    container.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC, "advancedleveling-common.toml");
  }

  @SubscribeEvent
  public static void onJoin(EntityJoinLevelEvent event) {
    if (event.getLevel().isClientSide() || !(event.getEntity() instanceof LivingEntity living) || living instanceof net.minecraft.world.entity.player.Player) return;
    if (!living.getPersistentData().contains("LEVEL")) {
      living.getPersistentData().putInt("LEVEL", Math.max(0, STARTING_LEVEL.get() - 1 + LEVEL_BONUS.get()));
    }
  }

  @SubscribeEvent
  public static void onDrops(LivingDropsEvent event) {
    if (!ENABLE_ADDITIONAL_LOOT.get() || !(event.getEntity().level() instanceof ServerLevel level)) return;
    if (!event.getEntity().getPersistentData().contains("LEVEL")) return;
    LootTable table = level.getServer().reloadableRegistries().getLootTable(ADDITIONAL_LOOT);
    LootParams params = new LootParams.Builder(level)
        .withParameter(LootContextParams.THIS_ENTITY, event.getEntity())
        .withParameter(LootContextParams.ORIGIN, event.getEntity().position())
        .withParameter(LootContextParams.DAMAGE_SOURCE, event.getSource())
        .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY);
    table.getRandomItems(params, stack -> event.getEntity().spawnAtLocation(stack));
  }

  public static int getLevel(Entity entity) { return entity.getPersistentData().getInt("LEVEL"); }
  public static boolean hasLevel(Entity entity) { return entity.getPersistentData().contains("LEVEL"); }
}
