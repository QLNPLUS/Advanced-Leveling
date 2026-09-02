package daripher.autoleveling.mixin;

import daripher.autoleveling.FabricLevelingHooks;
import daripher.autoleveling.LevelHolder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements LevelHolder {
  @Unique private int autoleveling$level = -1;

  @Inject(method = "tick", at = @At("TAIL"))
  private void autoleveling$tick(CallbackInfo callbackInfo) {
    FabricLevelingHooks.tick((LivingEntity) (Object) this);
  }

  @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
  private void autoleveling$writeLevel(NbtCompound nbt, CallbackInfo callbackInfo) {
    if (autoleveling$level >= 0) nbt.putInt("AdvancedLeveling", autoleveling$level);
  }

  @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
  private void autoleveling$readLevel(NbtCompound nbt, CallbackInfo callbackInfo) {
    autoleveling$level = nbt.contains("AdvancedLeveling") ? nbt.getInt("AdvancedLeveling") : -1;
  }

  @Override
  public int autoleveling$getLevel() {
    return autoleveling$level;
  }

  @Override
  public void autoleveling$setLevel(int level) {
    autoleveling$level = level;
  }
}
