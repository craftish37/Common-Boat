package net.craftish37.commonboat.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.craftish37.commonboat.ConfigAccess;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractBoatEntity.class)
public class AbstractBoatEntityMixin {
    @ModifyReturnValue(method = "getLandSlipperiness", at = @At("RETURN"))
    private float modifySlipperiness(float original) {
        if (!ConfigAccess.get().enabled) return original;

        double minSlip = ConfigAccess.get().slipperiness;
        return MathHelper.clamp(original, (float) minSlip, 1.0F);
    }
}