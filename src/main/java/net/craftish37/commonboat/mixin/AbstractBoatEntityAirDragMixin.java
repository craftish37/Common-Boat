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
public abstract class AbstractBoatEntityAirDragMixin {
    @Inject(method = "updateVelocity", at = @At("TAIL"))
    private void removeAirDrag(CallbackInfo ci) {
        AbstractBoatEntity boat = (AbstractBoatEntity) (Object) this;
        CommonBoatConfig cfg = ConfigAccess.get();
        boolean isInAir = !boat.isOnGround() && !boat.isSubmergedInWater();
        if (cfg.enabled && cfg.removeAirDrag && isInAir) {
            Vec3d currentVelocity = boat.getVelocity();
            boat.setVelocity(currentVelocity.multiply(1.0 / 0.99F));
        }
    }
}