package net.craftish37.commonboat.mixin;

import net.craftish37.commonboat.ConfigAccess;
import net.craftish37.commonboat.CommonBoatConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class BoatJumpMixin {

    @Shadow @Final protected MinecraftClient client;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        CommonBoatConfig cfg = ConfigAccess.get();

        if (cfg.enabled && cfg.easterEggsEnabled && cfg.flappyBirdEnabled) {
            if (player.getVehicle() instanceof AbstractBoatEntity boat) {
                boolean canJump = boat.getVelocity().y <= 0.05;

                if (canJump && this.client.options.jumpKey.isPressed()) {
                    Vec3d velocity = boat.getVelocity();
                    double horizontalSpeed = velocity.horizontalLength();
                    double jumpVelocity = horizontalSpeed * 1;
                    if (jumpVelocity > 0) {
                        boat.addVelocity(0.0, jumpVelocity, 0.0);
                    }
                }
            }
        }
    }
}