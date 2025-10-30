package net.craftish37.commonboat.mixin;

import net.craftish37.commonboat.ConfigAccess;
import net.craftish37.commonboat.CommonBoatConfig;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBoatEntity.class)
public abstract class AbstractBoatEntityTickMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void applySpeedCap(CallbackInfo ci) {
        AbstractBoatEntity boat = (AbstractBoatEntity) (Object) this;
        CommonBoatConfig cfg = ConfigAccess.get();
        if (cfg.enabled && cfg.maxSpeed != -1.0) {
            double internalMaxSpeed = cfg.maxSpeed / 20.0;
            Vec3d currentVelocity = boat.getVelocity();
            double horizontalSpeed = currentVelocity.horizontalLength();
            if (horizontalSpeed > internalMaxSpeed) {
                double scale = internalMaxSpeed / horizontalSpeed;
                boat.setVelocity(new Vec3d(currentVelocity.x * scale, currentVelocity.y, currentVelocity.z * scale)
                );
            }
        }
    }
}