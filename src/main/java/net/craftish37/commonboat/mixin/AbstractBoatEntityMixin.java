package net.craftish37.commonboat.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.craftish37.commonboat.ConfigAccess;
import net.craftish37.commonboat.CommonBoatConfig;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractBoatEntity.class)
public class AbstractBoatEntityMixin {
    @ModifyReturnValue(method = "getNearbySlipperiness", at = @At("RETURN"))
    private float modifySlipperiness(float original) {
        CommonBoatConfig cfg = ConfigAccess.get();
        AbstractBoatEntity boat = (AbstractBoatEntity) (Object) this;
        if (!cfg.enabled) return original;
        if (!cfg.customBlockSlipperiness.isEmpty()) {
            BlockPos blockPos = boat.getSteppingPos();
            BlockState blockState = boat.getWorld().getBlockState(blockPos);
            Identifier blockId = Registries.BLOCK.getId(blockState.getBlock());
            Double customSlip = cfg.customBlockSlipperiness.get(blockId.toString());
            if (customSlip != null) {
                return customSlip.floatValue();
            }
        }
        if (cfg.slipperinessEnabled) {
            double minSlip = cfg.slipperiness;
            return MathHelper.clamp(original, (float) minSlip, 1.0F);
        }
        return original;
    }
}
