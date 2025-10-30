package net.craftish37.commonboat;

import net.craftish37.commonboat.mixin.TropicalFishEntityAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import java.util.Set;

public class EasterEggFishHighlighter {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Set<Integer> UNIQUE_VARIANT_IDS = Set.of(
            65536,      // Clownfish
            459008,     // Triggerfish
            917504,     // Tomato Clownfish
            918273,     // Red Snapper
            918529,     // Red Cichlid
            16778497,   // Ornate Butterflyfish
            50660352,   // Queen Angelfish
            50726144,   // Cotton Candy Betta
            67108865,   // Threadfin
            67110144,   // Goatfish
            67371009,   // Yellow Tang
            67699456,   // Yellowtail Parrotfish
            67764993,   // Dottyback
            101253888,  // Parrotfish
            117441025,  // Moorish Idol
            117441793,  // Butterflyfish
            117506305,  // Anemone
            117899265,  // Black Tang
            118161664,  // Cichlid
            185008129,  // Blue Tang
            234882305,  // Emperor Red Snapper
            235340288   // Red Lipped Blenny
    );
    public static void onWorldRender(@org.jetbrains.annotations.Nullable MatrixStack matrices) {
        var cfg = ConfigAccess.get();
        if (!cfg.enabled || !cfg.easterEggsEnabled || !cfg.leFischeAuChocolatEnabled) return;
        if (client.world == null || client.player == null || matrices == null) return;
        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
        VertexConsumerProvider.Immediate provider = client.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer consumer = provider.getBuffer(RenderLayer.getLines());
        final float normalValue = (float) cfg.leFischeAuChocolatThiccness;
        for (Entity entity : client.world.getOtherEntities(client.player, client.player.getBoundingBox().expand(48))) {
            if (!(entity instanceof TropicalFishEntity fish)) continue;
            int variant = fish.getDataTracker().get(TropicalFishEntityAccessor.getVariantTrackedData());
            if (UNIQUE_VARIANT_IDS.contains(variant)) continue;
            drawBoxOutline(matrices, consumer, cameraPos, fish.getBoundingBox(), normalValue);
        }
        provider.draw();
    }
    private static void drawBoxOutline(MatrixStack matrices, VertexConsumer consumer, Vec3d cameraPos, Box box, float normalValue) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        double minX = box.minX - cameraPos.x;
        double minY = box.minY - cameraPos.y;
        double minZ = box.minZ - cameraPos.z;
        double maxX = box.maxX - cameraPos.x;
        double maxY = box.maxY - cameraPos.y;
        double maxZ = box.maxZ - cameraPos.z;

        line(consumer, matrix, minX, minY, minZ, maxX, minY, minZ, normalValue);
        line(consumer, matrix, maxX, minY, minZ, maxX, minY, maxZ, normalValue);
        line(consumer, matrix, maxX, minY, maxZ, minX, minY, maxZ, normalValue);
        line(consumer, matrix, minX, minY, maxZ, minX, minY, minZ, normalValue);

        line(consumer, matrix, minX, maxY, minZ, maxX, maxY, minZ, normalValue);
        line(consumer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, normalValue);
        line(consumer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, normalValue);
        line(consumer, matrix, minX, maxY, maxZ, minX, maxY, minZ, normalValue);

        line(consumer, matrix, minX, minY, minZ, minX, maxY, minZ, normalValue);
        line(consumer, matrix, maxX, minY, minZ, maxX, maxY, minZ, normalValue);
        line(consumer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, normalValue);
        line(consumer, matrix, minX, minY, maxZ, minX, maxY, maxZ, normalValue);
    }
    private static void line(VertexConsumer consumer, Matrix4f matrix, double x1, double y1, double z1, double x2, double y2, double z2, float normalValue) {
        consumer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(1.0F, 1.0F, 1.0F, 1.0F).normal(normalValue, normalValue, normalValue);
        consumer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(1.0F, 1.0F, 1.0F, 1.0F).normal(normalValue, normalValue, normalValue);
    }
}