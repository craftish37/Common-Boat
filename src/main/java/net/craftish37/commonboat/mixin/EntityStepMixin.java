package net.craftish37.commonboat.mixin;

import net.craftish37.commonboat.ConfigAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityStepMixin {
    @Redirect(
            method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getStepHeight()F")
    )
    private float modifyBoatStepHeight(Entity instance) {
        if (instance instanceof AbstractBoatEntity) {
            if (ConfigAccess.get().enabled) {
                return (float) ConfigAccess.get().boatStepHeight;
            }
        }
        return instance.getStepHeight();
    }
}