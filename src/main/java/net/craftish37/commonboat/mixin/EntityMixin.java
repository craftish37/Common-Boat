package net.craftish37.commonboat.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.craftish37.commonboat.ConfigAccess;
import net.craftish37.commonboat.CommonBoatConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Redirect(method = "getVelocityMultiplier",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getVelocityMultiplier()F"))
    private float getVelocityMultiplier(Block target) {
        CommonBoatConfig cfg = ConfigAccess.get();
        if (!cfg.enabled || !cfg.velocityMultiplierEnabled) return target.getVelocityMultiplier();
        Entity entity = ((Entity)(Object)this);
        if (entity instanceof AbstractBoatEntity && target instanceof FluidBlock) {
            return (float) cfg.velocityMultiplier;
        } else {
            return target.getVelocityMultiplier();
        }
    }
}