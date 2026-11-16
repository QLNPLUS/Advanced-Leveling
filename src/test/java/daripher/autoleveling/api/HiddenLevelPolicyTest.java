package daripher.autoleveling.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Locks the {@code mobs.hidden_levels} policy to two rules only: an exact entity ID hides that one
 * type, and a {@code namespace:*} entry hides every type of that namespace. The rule is exercised
 * directly, without a client and without the entity registry.
 */
class HiddenLevelPolicyTest {
  private static final ResourceLocation ZOMBIE = new ResourceLocation("minecraft", "zombie");
  private static final ResourceLocation ZOMBIE_VILLAGER =
      new ResourceLocation("minecraft", "zombie_villager");
  private static final ResourceLocation MODDED =
      new ResourceLocation("examplemod", "custom_mob");

  @Test
  @DisplayName("an empty hidden list hides nothing")
  void emptyListHidesNothing() {
    assertFalse(LevelingApi.isHiddenId(ZOMBIE, List.of()));
  }

  @Test
  @DisplayName("an exact entity ID hides that entity only")
  void exactIdHidesThatEntityOnly() {
    List<String> hidden = List.of("minecraft:zombie");
    assertTrue(LevelingApi.isHiddenId(ZOMBIE, hidden));
    assertFalse(LevelingApi.isHiddenId(ZOMBIE_VILLAGER, hidden));
    assertFalse(LevelingApi.isHiddenId(MODDED, hidden));
  }

  @Test
  @DisplayName("a namespace wildcard hides every entity of that namespace")
  void namespaceWildcardHidesTheWholeNamespace() {
    List<String> hidden = List.of("minecraft:*");
    assertTrue(LevelingApi.isHiddenId(ZOMBIE, hidden));
    assertTrue(LevelingApi.isHiddenId(ZOMBIE_VILLAGER, hidden));
    assertFalse(LevelingApi.isHiddenId(MODDED, hidden));
  }

  @Test
  @DisplayName("a wildcard for another namespace does not hide this one")
  void wildcardIsNamespaceScoped() {
    assertFalse(LevelingApi.isHiddenId(ZOMBIE, List.of("examplemod:*")));
    assertTrue(LevelingApi.isHiddenId(MODDED, List.of("examplemod:*")));
  }

  @Test
  @DisplayName("a bare path or a partial ID is not a rule")
  void partialIdsAreNotRules() {
    assertFalse(LevelingApi.isHiddenId(ZOMBIE, List.of("zombie")));
    assertFalse(LevelingApi.isHiddenId(ZOMBIE, List.of("*")));
    assertFalse(LevelingApi.isHiddenId(ZOMBIE, List.of("minecraft:zombie_")));
  }

  @Test
  @DisplayName("an unknown entity id is never hidden")
  void nullIdIsNeverHidden() {
    assertFalse(LevelingApi.isHiddenId(null, List.of("minecraft:*")));
  }

  @Test
  @DisplayName("exact and wildcard entries compose without widening each other")
  void exactAndWildcardCompose() {
    List<String> hidden = List.of("examplemod:custom_mob", "minecraft:*");
    assertTrue(LevelingApi.isHiddenId(ZOMBIE, hidden));
    assertTrue(LevelingApi.isHiddenId(MODDED, hidden));
    assertFalse(LevelingApi.isHiddenId(new ResourceLocation("other", "custom_mob"), hidden));
  }
}
