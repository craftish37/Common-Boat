package net.craftish37.commonboat.mixin;

import net.craftish37.commonboat.CommonBoatConfig;
import net.craftish37.commonboat.ConfigAccess;
import net.craftish37.commonboat.EasterEggFishHighlighter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
    @Shadow @Final private Matrix3x2fStack matrices;
    @Inject(method = "drawStackOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("TAIL"))
    private void renderCapturedFishAsterisk(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
        if (stack.isEmpty() || stack.getItem() != Items.TROPICAL_FISH_BUCKET) return;
        CommonBoatConfig cfg = ConfigAccess.get();
        if (!cfg.enabled || !cfg.easterEggsEnabled || !cfg.leFischeAuChocolatEnabled) return;
        int variant = -1;
        NbtComponent bucketData = stack.get(DataComponentTypes.BUCKET_ENTITY_DATA);
        if (bucketData != null) {
            NbtCompound nbt = bucketData.copyNbt();
            if (nbt.contains("Variant")) {
                variant = nbt.getInt("Variant").orElse(-1);
            } else if (nbt.contains("BucketVariantTag")) {
                variant = nbt.getInt("BucketVariantTag").orElse(-1);
            }
        }
        if (variant == -1) {
            NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (customData != null) {
                NbtCompound nbt = customData.copyNbt();
                if (nbt.contains("BucketVariantTag")) {
                    variant = nbt.getInt("BucketVariantTag").orElse(-1);
                }
            }
        }
        if (variant != -1) {
            Integer color = EasterEggFishHighlighter.getFishVariantColor(variant);
            if (color != null) {
                Matrix3x2f current2D = new Matrix3x2f();
                this.matrices.get(current2D);
                Matrix4f zMatrix = new Matrix4f();
                zMatrix.m00(current2D.m00());
                zMatrix.m11(current2D.m11());
                zMatrix.m01(current2D.m01());
                zMatrix.m10(current2D.m10());
                zMatrix.m30(current2D.m20());
                zMatrix.m31(current2D.m21());
                zMatrix.translate(0.0F, 0.0F, 200.0F);
                VertexConsumerProvider.Immediate consumers = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
                renderer.draw("*", x + 10, y - 2, color, true, zMatrix, consumers, TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
            }
        }
    }
}