package net.craftish37.commonboat.mixin;

import net.craftish37.commonboat.ConfigAccess;
import net.craftish37.commonboat.CommonBoatConfig;
import net.minecraft.block.BlockState;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBoatEntity.class)
public abstract class BoatTransitionMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void assistStepUp(CallbackInfo ci) {
        AbstractBoatEntity boat = (AbstractBoatEntity) (Object) this;
        CommonBoatConfig cfg = ConfigAccess.get();
        if (!cfg.enabled || !cfg.boatStepHeightEnabled) return;
        Vec3d velocity = boat.getVelocity();
        double speed = velocity.horizontalLength();
        if (speed <= 0) return;
        Vec3d direction = velocity.normalize();
        Vec3d boatPos = boat.getEntityPos();
        Vec3d rightVec = new Vec3d(-direction.z, 0, direction.x);
        Vec3d[] checkPoints = {
                boatPos.add(direction.multiply(0.9)),
                boatPos.add(direction.multiply(0.8)).add(rightVec.multiply(0.7)),
                boatPos.add(direction.multiply(0.8)).add(rightVec.multiply(-0.7))
        };
        for (Vec3d point : checkPoints) {
            BlockPos wallPos = BlockPos.ofFloored(point.x, boat.getY(), point.z);
            BlockState wallState = boat.getEntityWorld().getBlockState(wallPos);
            VoxelShape wallShape = wallState.getCollisionShape(boat.getEntityWorld(), wallPos);
            if (!wallShape.isEmpty() && wallState.getFluidState().isEmpty()) {
                boolean isStepValid = wallState.isSideSolidFullSquare(boat.getEntityWorld(), wallPos, Direction.UP);
                if (!isStepValid) {
                    BlockPos centerSupportPos = wallPos.down();
                    for (BlockPos supportPos : BlockPos.iterate(centerSupportPos.add(-1, 0, -1), centerSupportPos.add(1, 0, 1))) {
                        BlockState supportState = boat.getEntityWorld().getBlockState(supportPos);
                        VoxelShape supportShape = supportState.getCollisionShape(boat.getEntityWorld(), supportPos);
                        if (!supportShape.isEmpty() && supportState.getFluidState().isEmpty()) {
                            Box wallBox = wallShape.getBoundingBox();
                            Box supportBox = supportShape.getBoundingBox();
                            if (supportBox.minX <= wallBox.minX && supportBox.maxX >= wallBox.maxX &&
                                    supportBox.minZ <= wallBox.minZ && supportBox.maxZ >= wallBox.maxZ) {
                                isStepValid = true;
                                break;
                            }
                        }
                    }
                }
                if (!isStepValid) continue;
                BlockPos posAbove = wallPos.up();
                if (!boat.getEntityWorld().getBlockState(posAbove).getCollisionShape(boat.getEntityWorld(), posAbove).isEmpty()) {
                    continue;
                }
                double targetY = wallPos.getY() + wallShape.getMax(Direction.Axis.Y);
                double liftDistance = targetY - boat.getY();
                if (liftDistance > 0.01 && liftDistance <= cfg.boatStepHeight) {
                    boat.setPosition(
                            boat.getX() + direction.x * 0.25,
                            targetY + 0.25,
                            boat.getZ() + direction.z * 0.25
                    );
                    return;
                }
            }
        }
    }
}