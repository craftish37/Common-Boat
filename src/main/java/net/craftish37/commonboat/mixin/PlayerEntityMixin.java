package net.craftish37.commonboat.mixin;

import net.craftish37.commonboat.CommonBoatConfig;
import net.craftish37.commonboat.ConfigAccess;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void commonboat$removeBoatBreakSpeedPenalty(BlockState block, CallbackInfoReturnable<Float> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        CommonBoatConfig cfg = ConfigAccess.get();
        if (cfg.enabled && cfg.disableBlockBreakingPenalty && player.getVehicle() instanceof AbstractBoatEntity) {
            if (!player.isOnGround()) {
                cir.setReturnValue(cir.getReturnValue() * 5.0F);
            }
        }
    }
}