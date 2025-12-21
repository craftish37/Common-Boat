package net.craftish37.commonboat.mixin;

import net.craftish37.commonboat.CommonBoatConfig;
import net.craftish37.commonboat.ConfigAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientConnection.class)
public class ClientPlayNetworkHandlerMixin {

    @ModifyVariable(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), argsOnly = true)
    private Packet<?> commonboat$spoofBoatPosition(Packet<?> packet) {
        if (packet instanceof VehicleMoveC2SPacket) {
            CommonBoatConfig cfg = ConfigAccess.get();
            if (cfg.enabled && cfg.preventUnderwaterEject) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null && client.world != null && client.player.getVehicle() instanceof AbstractBoatEntity boat) {
                    if (boat.isSubmergedInWater()) {
                        BlockPos pos = boat.getBlockPos();
                        double waterHeight = boat.getEntityWorld().getFluidState(pos).getHeight(boat.getEntityWorld(), pos);
                        if (waterHeight > 0) {
                            double surfaceY = (double) pos.getY() + waterHeight + 0.1;
                            return new VehicleMoveC2SPacket(
                                    new Vec3d(boat.getX(), surfaceY, boat.getZ()),
                                    boat.getYaw(),
                                    boat.getPitch(),
                                    boat.isSubmergedInWater()
                            );
                        }
                    }
                }
            }
        }
        return packet;
    }
}