package daripher.autoleveling.client;

import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.core.registries.BuiltInRegistries;
import org.slf4j.Logger;

@EventBusSubscriber(value = Dist.CLIENT)
public enum LeveledMobsTextures implements ResourceManagerReloadListener {
  INSTANCE;

  private static final Map<EntityType<?>, Map<Integer, Identifier>> CACHED_TEXTURES =
      new HashMap<>();
  private static final String TEXTURES_FOLDER = "textures/leveled_mobs";
  private static final String TEXTURE_FILE_NAME_FORMAT = "^[a-z|_]+_[1-9]+$";
  private static final String PNG_FILE_SUFFIX = ".png";
  private static final Logger LOGGER = LogUtils.getLogger();

  @Nullable
  public static Identifier get(EntityType<?> entityType, int level) {
    if (!hasTexturesFor(entityType)) {
      return null;
    }
    for (int i = level; i > 0; i--) {
      Identifier textureLocation = getTextureFor(entityType, i);
      if (textureLocation != null) {
        return textureLocation;
      }
    }
    return null;
  }

  @Nullable
  private static Identifier getTextureFor(EntityType<?> entityType, int level) {
    return CACHED_TEXTURES.get(entityType).get(level);
  }

  private static boolean hasTexturesFor(EntityType<?> entityType) {
    return CACHED_TEXTURES.containsKey(entityType) && !CACHED_TEXTURES.get(entityType).isEmpty();
  }

  @SubscribeEvent
  public static void addListener(AddClientReloadListenersEvent event) {
    event.addListener(
        Identifier.fromNamespaceAndPath("autoleveling", "leveled_mob_textures"), INSTANCE);
  }

  @Override
  public void onResourceManagerReload(ResourceManager resourceManager) {
    CACHED_TEXTURES.clear();
    Set<Identifier> textures =
        resourceManager.listResources(TEXTURES_FOLDER, this::isPngImage).keySet();
    Predicate<String> textureNamePredicate =
        Pattern.compile(TEXTURE_FILE_NAME_FORMAT).asPredicate();
    Stream<Identifier> validTextures =
        textures.stream()
            .filter(textureLocation -> textureNamePredicate.test(textureLocation.toString()));
    validTextures.forEach(this::saveTexture);
  }

  private void saveTexture(Identifier textureLocation) {
    String textureName = getTextureFileName(textureLocation);
    String[] splitTextureName = textureName.split("_");
    String entityTypeName = splitTextureName[0];
    Identifier entityTypeId =
        Identifier.fromNamespaceAndPath(textureLocation.getNamespace(), entityTypeName);
    EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(entityTypeId);
    if (entityType == null) {
      LOGGER.warn(
          "Can't read texture {}, unknown entity type {} specified", textureLocation, entityTypeId);
      return;
    }
    CACHED_TEXTURES.computeIfAbsent(entityType, k -> new HashMap<>());
    int entityLevel = Integer.parseInt(splitTextureName[1]);
    CACHED_TEXTURES.get(entityType).put(entityLevel, textureLocation);
  }

  public String getTextureFileName(Identifier resourceLocation) {
    return resourceLocation.getPath().replace(TEXTURES_FOLDER, "").replace(PNG_FILE_SUFFIX, "");
  }

  private boolean isPngImage(Identifier resourceLocation) {
    return resourceLocation.getPath().endsWith(PNG_FILE_SUFFIX);
  }
}
