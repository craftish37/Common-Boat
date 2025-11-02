package net.craftish37.commonboat.mixin;

import net.craftish37.commonboat.ConfigAccess;
import net.craftish37.commonboat.CommonBoatConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity.Location;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class BoatJumpMixin {
    @Shadow @Final protected MinecraftClient client;
    @Unique
    private double commonboat$lastJumpPeakY = 0.0;
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        CommonBoatConfig cfg = ConfigAccess.get();
        if (cfg.enabled && cfg.easterEggsEnabled && cfg.flappyBirdEnabled) {
            if (player.getVehicle() instanceof AbstractBoatEntity boat) {
                boolean canInitiateJump = boat.getVelocity().y <= 0.01;
                double gravity = 0.04D;
                if (boat.getVelocity().y > 0) {
                    commonboat$lastJumpPeakY = boat.getY();
                } else if (boat.isOnGround() || ((AbstractBoatEntityAccessor) boat).getLocationField() == Location.IN_WATER) {
                    commonboat$lastJumpPeakY = boat.getY();
                }
                if (this.client.options.jumpKey.isPressed()) {
                    Location currentLocation = ((AbstractBoatEntityAccessor) boat).getLocationField();
                    boolean onSurface = boat.isOnGround() || currentLocation == Location.IN_WATER;
                    if (onSurface && canInitiateJump) {
                        double jumpVelocity = boat.getVelocity().horizontalLength() * 1;
                        if (cfg.maxJumpHeight != -1.0) {
                            double maxVerticalVelocity = Math.sqrt(2 * gravity * cfg.maxJumpHeight);
                            if (jumpVelocity > maxVerticalVelocity) {
                                jumpVelocity = maxVerticalVelocity;
                            }
                        }
                        if (jumpVelocity > 0) {
                            boat.addVelocity(0.0, jumpVelocity, 0.0);
                            commonboat$lastJumpPeakY = boat.getY();
                        }
                    } else if (!onSurface) {
                        double jumpHeight = 1.0;
                        if (cfg.flappyBirdPitchControl) {
                            float pitch = player.getPitch();
                            double fallHeight = commonboat$lastJumpPeakY - boat.getY();
                            if (fallHeight < 0.0) fallHeight = 0.0;
                            if (pitch >= 0) {
                                double pitchPercent = pitch / 90.0;
                                jumpHeight = (1.0 - pitchPercent) * fallHeight;
                            } else {
                                double pitchPercent = pitch / -90.0;
                                jumpHeight = (pitchPercent * (5.0 - fallHeight)) + fallHeight;
                            }
                        }

                        double airJumpVelocity = Math.sqrt(2 * gravity * jumpHeight);
                        Vec3d currentVel = boat.getVelocity();
                        if (currentVel.y <= 0.0 && airJumpVelocity > 0) {
                            boat.setVelocity(currentVel.x, airJumpVelocity, currentVel.z);
                        }
                    }
                }
            }
        }
    }
}