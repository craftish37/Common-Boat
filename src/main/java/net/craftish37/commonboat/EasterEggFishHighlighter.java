package net.craftish37.commonboat;

import org.lwjgl.opengl.GL11;
import net.craftish37.commonboat.mixin.TropicalFishEntityAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.List;
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
        List<TropicalFishEntity> fishToHighlight = new ArrayList<>();
        for (Entity entity : client.world.getOtherEntities(client.player, client.player.getBoundingBox().expand(48))) {
            if (!(entity instanceof TropicalFishEntity fish)) continue;
            int variant = fish.getDataTracker().get(TropicalFishEntityAccessor.getVariantTrackedData());
            if (UNIQUE_VARIANT_IDS.contains(variant)) continue;
            fishToHighlight.add(fish);
        }
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        for (TropicalFishEntity fish : fishToHighlight) {
            drawBoxOutline(matrices, consumer, cameraPos, fish.getBoundingBox());
        }
        if (fishToHighlight.size() > 1) {
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            for (int i = 0; i < fishToHighlight.size() - 1; i++) {
                TropicalFishEntity fish1 = fishToHighlight.get(i);
                TropicalFishEntity fish2 = fishToHighlight.get(i + 1);

                Vec3d center1 = fish1.getBoundingBox().getCenter();
                Vec3d center2 = fish2.getBoundingBox().getCenter();

                double x1 = center1.x - cameraPos.x;
                double y1 = center1.y - cameraPos.y;
                double z1 = center1.z - cameraPos.z;
                double x2 = center2.x - cameraPos.x;
                double y2 = center2.y - cameraPos.y;
                double z2 = center2.z - cameraPos.z;

                line(consumer, matrix, x1, y1, z1, x2, y2, z2);
            }
        }

        provider.draw();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glLineWidth(1.0f);
    }
    private static void drawBoxOutline(MatrixStack matrices, VertexConsumer consumer, Vec3d cameraPos, Box box) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        double minX = box.minX - cameraPos.x;
        double minY = box.minY - cameraPos.y;
        double minZ = box.minZ - cameraPos.z;
        double maxX = box.maxX - cameraPos.x;
        double maxY = box.maxY - cameraPos.y;
        double maxZ = box.maxZ - cameraPos.z;

        line(consumer, matrix, minX, minY, minZ, maxX, minY, minZ);
        line(consumer, matrix, maxX, minY, minZ, maxX, minY, maxZ);
        line(consumer, matrix, maxX, minY, maxZ, minX, minY, maxZ);
        line(consumer, matrix, minX, minY, maxZ, minX, minY, minZ);

        line(consumer, matrix, minX, maxY, minZ, maxX, maxY, minZ);
        line(consumer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ);
        line(consumer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ);
        line(consumer, matrix, minX, maxY, maxZ, minX, maxY, minZ);

        line(consumer, matrix, minX, minY, minZ, minX, maxY, minZ);
        line(consumer, matrix, maxX, minY, minZ, maxX, maxY, minZ);
        line(consumer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ);
        line(consumer, matrix, minX, minY, maxZ, minX, maxY, maxZ);
    }
    private static void line(VertexConsumer consumer, Matrix4f matrix, double x1, double y1, double z1, double x2, double y2, double z2) {
        consumer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(1.0F, 1.0F, 1.0F, 1.0F).normal(1.0F, 1.0F, 1.0F);
        consumer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(1.0F, 1.0F, 1.0F, 1.0F).normal(1.0F, 1.0F, 1.0F);
    }
}