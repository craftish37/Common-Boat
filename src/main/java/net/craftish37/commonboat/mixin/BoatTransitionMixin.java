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
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
            if (commonboat$tryStepUpAtPoint(boat, cfg, point, direction)) {
                return;
            }
        }
    }
    @Unique
    private boolean commonboat$tryStepUpAtPoint(AbstractBoatEntity boat, CommonBoatConfig cfg, Vec3d point, Vec3d direction) {
        World world = boat.getEntityWorld();
        BlockPos wallPos = BlockPos.ofFloored(point.x, boat.getY(), point.z);
        BlockState wallState = world.getBlockState(wallPos);
        VoxelShape wallShape = wallState.getCollisionShape(world, wallPos);
        if (wallShape.isEmpty() || !wallState.getFluidState().isEmpty()) {
            return false;
        }
        if (!commonboat$isStepValid(world, wallPos, wallState, wallShape)) {
            return false;
        }
        BlockPos posAbove = wallPos.up();
        if (!world.getBlockState(posAbove).getCollisionShape(world, posAbove).isEmpty()) {
            return false;
        }
        double targetY = wallPos.getY() + wallShape.getMax(Direction.Axis.Y);
        double liftDistance = targetY - boat.getY();
        if (liftDistance > 0.01 && liftDistance <= cfg.boatStepHeight) {
            boat.setPosition(
                    boat.getX() + direction.x * 0.25,
                    targetY + 0.25,
                    boat.getZ() + direction.z * 0.25
            );
            return true;
        }
        return false;
    }
    @Unique
    private boolean commonboat$isStepValid(World world, BlockPos wallPos, BlockState wallState, VoxelShape wallShape) {
        if (wallState.isSideSolidFullSquare(world, wallPos, Direction.UP)) {
            return true;
        }
        BlockPos centerSupportPos = wallPos.down();
        Box wallBox = wallShape.getBoundingBox();

        for (BlockPos supportPos : BlockPos.iterate(centerSupportPos.add(-1, 0, -1), centerSupportPos.add(1, 0, 1))) {
            BlockState supportState = world.getBlockState(supportPos);
            VoxelShape supportShape = supportState.getCollisionShape(world, supportPos);

            if (!supportShape.isEmpty() && supportState.getFluidState().isEmpty()) {
                Box supportBox = supportShape.getBoundingBox();
                if (supportBox.minX <= wallBox.minX && supportBox.maxX >= wallBox.maxX &&
                        supportBox.minZ <= wallBox.minZ && supportBox.maxZ >= wallBox.maxZ) {
                    return true;
                }
            }
        }
        return false;
    }
}