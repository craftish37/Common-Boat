package net.craftish37.commonboat.mixin;

import net.craftish37.commonboat.ConfigAccess;
import net.craftish37.commonboat.CommonBoatConfig;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity.Location;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBoatEntity.class)
public abstract class AbstractBoatEntityAirDragMixin {
    @Unique
    private Vec3d commonboat$savedVelocity = Vec3d.ZERO;
    @Shadow private Location location;
    @Shadow private float yawVelocity;
    @Shadow private boolean pressingLeft;
    @Shadow private boolean pressingRight;
    @Shadow private boolean pressingForward;
    @Shadow private boolean pressingBack;
    @Inject(method = "updatePaddles", at = @At("HEAD"))
    private void preventPaddling(CallbackInfo ci) {
        CommonBoatConfig cfg = ConfigAccess.get();
        if (cfg.enabled && cfg.easterEggsEnabled && cfg.elytraBoatEnabled) {
            net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
            AbstractBoatEntity self = (AbstractBoatEntity) (Object) this;
            if (client.player != null && client.player.getVehicle() == self && client.options.jumpKey.isPressed()) {
                this.pressingLeft = false;
                this.pressingRight = false;
            }
        }
    }
    @Inject(method = "updateVelocity", at = @At("HEAD"), cancellable = true)
    private void saveVelocity(CallbackInfo ci) {
        AbstractBoatEntity self = (AbstractBoatEntity) (Object) this;
        this.commonboat$savedVelocity = self.getVelocity();
        CommonBoatConfig cfg = ConfigAccess.get();
        if (cfg.enabled && cfg.easterEggsEnabled && cfg.elytraBoatEnabled) {
            net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
            if (client.player != null && client.player.getVehicle() == self && client.options.jumpKey.isPressed()) {
                this.yawVelocity = 0.0F;
                this.pressingLeft = false;
                this.pressingRight = false;
                if (!self.isOnGround()) {
                    ci.cancel();
                }
            }
        }
    }
    @Inject(method = "updateVelocity", at = @At("TAIL"))
    private void applyCustomPhysics(CallbackInfo ci) {
        CommonBoatConfig cfg = ConfigAccess.get();
        AbstractBoatEntity self = (AbstractBoatEntity) (Object) this;
        if (!cfg.enabled) {
            return;
        }
        if (cfg.easterEggsEnabled && cfg.elytraBoatEnabled) {
            return;
        }
        Vec3d currentVelocity = self.getVelocity();
        double savedSpeed = this.commonboat$savedVelocity.horizontalLength();
        Vec3d savedHorizontalVector = new Vec3d(this.commonboat$savedVelocity.x, 0, this.commonboat$savedVelocity.z);
        if (this.location == Location.IN_AIR && cfg.removeAirDrag) {
            if (currentVelocity.y > 0) {
                Vec3d horizontalFacing = Vec3d.fromPolar(0.0F, self.getYaw());
                Vec3d newHorizontalVelocity = horizontalFacing.multiply(savedSpeed);
                self.setVelocity(new Vec3d(newHorizontalVelocity.x, currentVelocity.y, newHorizontalVelocity.z));
            } else {
                self.setVelocity(new Vec3d(savedHorizontalVector.x, currentVelocity.y, savedHorizontalVector.z));
            }
        } else if (this.location == Location.IN_WATER && cfg.velocityMultiplierEnabled) {
            boolean isPaddling = this.pressingForward || this.pressingBack;
            boolean isTurning = this.pressingLeft || this.pressingRight || this.yawVelocity != 0.0F;
            if (!isPaddling && !isTurning) {
                self.setVelocity(new Vec3d(savedHorizontalVector.x, currentVelocity.y, savedHorizontalVector.z));
            }
        }
    }
}