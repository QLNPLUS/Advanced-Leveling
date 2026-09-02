package daripher.autoleveling.event;

import daripher.autoleveling.AutoLevelingMod;
import daripher.autoleveling.config.AdvancedConfig;
import daripher.autoleveling.config.Config;
import daripher.autoleveling.data.DimensionsLevelingSettingsReloader;
import daripher.autoleveling.data.EntitiesLevelingSettingsReloader;
import daripher.autoleveling.init.AutoLevelingAttributes;
import daripher.autoleveling.saveddata.GlobalLevelingData;
import daripher.autoleveling.saveddata.WorldLevelingData;
import daripher.autoleveling.settings.DimensionLevelingSettings;
import daripher.autoleveling.settings.AttributeBonus;
import daripher.autoleveling.settings.LevelingSettings;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = AutoLevelingMod.MOD_ID)
public class MobsLevelingEvents {
  private static final String LEVEL_TAG = "LEVEL";
  private static final ContextKeySet ADDITIONAL_LOOT_PARAMS =
      new ContextKeySet.Builder()
          .required(LootContextParams.THIS_ENTITY)
          .required(LootContextParams.ORIGIN)
          .required(LootContextParams.DAMAGE_SOURCE)
          .optional(LootContextParams.ATTACKING_ENTITY)
          .optional(LootContextParams.DIRECT_ATTACKING_ENTITY)
          .optional(LootContextParams.LAST_DAMAGE_PLAYER)
          .build();

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public static void applyLevelBonuses(EntityJoinLevelEvent event) {
    if (!shouldSetLevel(event.getEntity())) return;
    LivingEntity entity = (LivingEntity) event.getEntity();
    if (hasLevel(entity)) {
      applyAttributeBonuses(entity, false);
      return;
    }
    BlockPos spawnPos = getSpawnPosition(entity);
    double distanceToSpawn = Math.sqrt(spawnPos.distSqr(entity.blockPosition()));
    int level = createLevelForEntity(entity, distanceToSpawn);
    setLevel(entity, level);
    applyAttributeBonuses(entity, true);
    addEquipment(entity);
  }

  private static BlockPos getSpawnPosition(LivingEntity entity) {
    ResourceKey<Level> dimension = entity.level().dimension();
    DimensionLevelingSettings settings = DimensionsLevelingSettingsReloader.get(dimension);
    if (settings.spawnPosOverride() == null) {
      return ((ServerLevel) entity.level()).getRespawnData().pos();
    }
    return settings.spawnPosOverride();
  }

  @SubscribeEvent
  public static void adjustExperienceDrop(LivingExperienceDropEvent event) {
    if (!hasLevel(event.getEntity())) return;
    int level = getLevel(event.getEntity()) + 1;
    int originalExp = event.getDroppedExperience();
    double expBonus = Config.COMMON.expBonus.get() * level;
    event.setDroppedExperience((int) (originalExp + originalExp * expBonus));
  }

  @SubscribeEvent
  public static void dropAdditionalLoot(LivingDropsEvent event) {
    if (!Config.COMMON.enableAdditionalLoot.get()) return;
    LivingEntity entity = event.getEntity();
    if (!hasLevel(entity)) return;
    ResourceKey<LootTable> lootTableId =
        ResourceKey.create(
            net.minecraft.core.registries.Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(AutoLevelingMod.MOD_ID, "gameplay/leveled_mobs"));
    MinecraftServer server = entity.level().getServer();
    if (server == null) return;
    LootTable lootTable = server.reloadableRegistries().getLootTable(lootTableId);
    if (lootTable == LootTable.EMPTY) return;
    LootParams lootParams = createLootParams(entity, event.getSource());
    lootTable.getRandomItems(
        lootParams,
        itemStack -> entity.spawnAtLocation((ServerLevel) entity.level(), itemStack));
  }

  @SubscribeEvent
  public static void reloadSettings(AddServerReloadListenersEvent event) {
    AdvancedConfig.load();
    event.addListener(
        Identifier.fromNamespaceAndPath(AutoLevelingMod.MOD_ID, "dimensions_leveling_settings"),
        new DimensionsLevelingSettingsReloader());
    event.addListener(
        Identifier.fromNamespaceAndPath(AutoLevelingMod.MOD_ID, "entities_leveling_settings"),
        new EntitiesLevelingSettingsReloader());
  }

  @SubscribeEvent
  public static void applyAttributesDamageBonus(LivingIncomingDamageEvent event) {
    DamageSource damage = event.getSource();
    if (!(damage.getEntity() instanceof LivingEntity attacker)) return;
    float multiplier = getDamageMultiplier(damage, attacker);
    if (multiplier > 1F) event.setAmount(event.getAmount() * multiplier);
  }

  public static float getDamageMultiplier(DamageSource damage, LivingEntity attacker) {
    if (damage.is(DamageTypeTags.IS_PROJECTILE)) {
      return getAttributeValue(attacker, AutoLevelingAttributes.PROJECTILE_DAMAGE_MULTIPLIER);
    }
    if (damage.is(DamageTypeTags.IS_EXPLOSION)) {
      return getAttributeValue(attacker, AutoLevelingAttributes.EXPLOSION_DAMAGE_MULTIPLIER);
    }
    return 1F;
  }

  private static float getAttributeValue(
      LivingEntity entity, net.minecraft.core.Holder<Attribute> damageBonusAttribute) {
    AttributeInstance instance = entity.getAttribute(damageBonusAttribute);
    return instance == null ? 0F : (float) instance.getValue();
  }

  private static boolean shouldSetLevel(Entity entity) {
    if (entity.level().isClientSide()) return false;
    return canHaveLevel(entity);
  }

  private static int createLevelForEntity(LivingEntity entity, double distance) {
    MinecraftServer server = ((ServerLevel) entity.level()).getServer();
    if (server == null) return 0;
    LevelingSettings settings = getLevelingSettings(entity);
    int monsterLevel = settings.startingLevel() - 1;
    int maxLevel = settings.maxLevel();
    monsterLevel += (int) (settings.levelsPerDistance() * distance);
    monsterLevel += (int) Math.pow(distance, distance * settings.levelPowerPerDistance()) - 1;
    if (entity.getY() < 64) {
      double deepness = 64 - entity.getY();
      monsterLevel += (int) (settings.levelsPerDeepness() * deepness);
      monsterLevel += (int) Math.pow(deepness, deepness * settings.levelPowerPerDeepness()) - 1;
    }
    int levelBonus = settings.randomLevelBonus() + 1;
    if (levelBonus > 0) monsterLevel += entity.getRandom().nextInt(levelBonus);
    monsterLevel = Math.abs(monsterLevel);
    monsterLevel += WorldLevelingData.get((ServerLevel) entity.level()).getLevelBonus();
    monsterLevel += Config.COMMON.levelBonus.get();
    if (maxLevel > 0) monsterLevel = Math.min(monsterLevel, maxLevel - 1);
    GlobalLevelingData globalLevelingData = GlobalLevelingData.get(server);
    monsterLevel += globalLevelingData.getLevelBonus();
    return monsterLevel;
  }

  @Nonnull
  private static LevelingSettings getLevelingSettings(LivingEntity entity) {
    LevelingSettings settings = EntitiesLevelingSettingsReloader.get(entity.getType());
    if (settings == null) {
      ResourceKey<Level> dimension = entity.level().dimension();
      return DimensionsLevelingSettingsReloader.get(dimension);
    }
    return settings;
  }

  public static void applyAttributeBonuses(LivingEntity entity) {
    applyAttributeBonuses(entity, true);
  }

  private static void applyAttributeBonuses(LivingEntity entity, boolean restoreHealth) {
    getAttributeBonuses(entity)
        .forEach(
            (attribute, bonus) -> applyAttributeBonus(entity, attribute, bonus, restoreHealth));
  }

  private static Map<Attribute, AttributeBonus> getAttributeBonuses(LivingEntity entity) {
    LevelingSettings settings = getLevelingSettings(entity);
    if (settings.attributeModifiers().isEmpty()) {
      return AdvancedConfig.getAttributeBonuses();
    }
    return settings.attributeModifiers();
  }

  private static void applyAttributeBonus(
      LivingEntity entity, Attribute attribute, AttributeBonus bonus, boolean restoreHealth) {
    AttributeInstance attributeInstance =
        entity.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute));
    if (attributeInstance == null) {
      return;
    }
    AttributeModifier oldModifier = attributeInstance.getModifier(AttributeBonus.MODIFIER_ID);
    if (oldModifier != null) attributeInstance.removeModifier(oldModifier);
    int level = getLevel(entity);
    AttributeModifier modifier = bonus.createModifier(attributeInstance.getBaseValue(), level);
    attributeInstance.addPermanentModifier(modifier);
    if (restoreHealth && attribute == Attributes.MAX_HEALTH.value()) {
      entity.setHealth(entity.getMaxHealth());
    }
  }

  public static void addEquipment(LivingEntity entity) {
    MinecraftServer server = entity.level().getServer();
    if (server == null) return;
    for (EquipmentSlot slot : EquipmentSlot.values()) {
      LootTable equipmentTable = getEquipmentLootTableForSlot(server, entity, slot);
      if (equipmentTable == LootTable.EMPTY) continue;
      LootParams lootParams = createEquipmentLootParams(entity);
      equipmentTable.getRandomItems(lootParams, itemStack -> entity.setItemSlot(slot, itemStack));
    }
  }

  private static LootTable getEquipmentLootTableForSlot(
      MinecraftServer server, LivingEntity entity, EquipmentSlot slot) {
    Identifier entityId = EntityType.getKey(entity.getType());
    Identifier lootTableId = getEquipmentTableId(slot, entityId);
    ResourceKey<LootTable> key =
        ResourceKey.create(Registries.LOOT_TABLE, lootTableId);
    return server.reloadableRegistries().getLootTable(key);
  }

  @Nonnull
  private static Identifier getEquipmentTableId(
      EquipmentSlot slot, Identifier entityId) {
    String path = "equipment/" + entityId.getPath() + "_" + slot.getName();
    return Identifier.fromNamespaceAndPath(entityId.getNamespace(), path);
  }

  private static LootParams createLootParams(LivingEntity entity, DamageSource damageSource) {
    ServerLevel level = (ServerLevel) entity.level();
    LootParams.Builder builder =
        new LootParams.Builder(level)
            .withParameter(LootContextParams.THIS_ENTITY, entity)
            .withParameter(LootContextParams.ORIGIN, entity.position())
            .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
            .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, damageSource.getEntity())
            .withOptionalParameter(
                LootContextParams.DIRECT_ATTACKING_ENTITY, damageSource.getDirectEntity());
    int lastHurtByPlayerTime = entity.getLastHurtByPlayerMemoryTime();
    Player lastHurtByPlayer = entity.getLastHurtByPlayer();
    if (lastHurtByPlayerTime > 0 && lastHurtByPlayer != null) {
      builder =
          builder
              .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, lastHurtByPlayer)
              .withLuck(lastHurtByPlayer.getLuck());
    }
    return builder.create(ADDITIONAL_LOOT_PARAMS);
  }

  private static LootParams createEquipmentLootParams(LivingEntity entity) {
    return new LootParams.Builder((ServerLevel) entity.level())
        .withParameter(LootContextParams.THIS_ENTITY, entity)
        .withParameter(LootContextParams.ORIGIN, entity.position())
        .create(LootContextParamSets.SELECTOR);
  }

  private static boolean canHaveLevel(Entity entity) {
    if (!(entity instanceof LivingEntity)) return false;
    if (entity.getType() == EntityType.PLAYER) return false;
    Identifier entityId = EntityType.getKey(entity.getType());
    String entityNamespace = entityId.getNamespace();
    List<String> blacklist = AdvancedConfig.getMobBlacklist();
    if (blacklist.contains(entityNamespace + ":*")) return false;
    List<String> whitelist = AdvancedConfig.getMobWhitelist();
    if (whitelist.contains(entityNamespace + ":*")) return true;
    if (blacklist.contains(entityId.toString())) return false;
    if (!whitelist.isEmpty()) return whitelist.contains(entityId.toString());
    return true;
  }

  public static boolean shouldShowLevel(Entity entity) {
    Identifier entityId = EntityType.getKey(entity.getType());
    List<String> blacklist = AdvancedConfig.getHiddenLevels();
    if (blacklist.contains(entityId.toString())) return false;
    String namespace = entityId.getNamespace();
    return !blacklist.contains(namespace + ":*");
  }

  public static boolean hasLevel(Entity entity) {
    return entity.getPersistentData().contains(LEVEL_TAG);
  }

  public static int getLevel(LivingEntity entity) {
    return entity.getPersistentData().getIntOr(LEVEL_TAG, 0);
  }

  public static void setLevel(LivingEntity entity, int level) {
    entity.getPersistentData().putInt(LEVEL_TAG, level);
  }
}
