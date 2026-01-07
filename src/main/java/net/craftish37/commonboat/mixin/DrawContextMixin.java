package net.craftish37.commonboat.mixin;

import net.craftish37.commonboat.CommonBoatConfig;
import net.craftish37.commonboat.ConfigAccess;
import net.craftish37.commonboat.EasterEggFishHighlighter;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
    @Inject(method = "drawStackOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("TAIL"))
    private void renderCapturedFishAsterisk(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
        if (stack.isEmpty() || stack.getItem() != Items.TROPICAL_FISH_BUCKET) return;
        CommonBoatConfig cfg = ConfigAccess.get();
        if (!cfg.enabled || !cfg.easterEggsEnabled || !cfg.leFischeAuChocolatEnabled) return;
        Integer variant = EasterEggFishHighlighter.getVariantIdFromBucket(stack);
        if (variant != null) {
            Integer color = EasterEggFishHighlighter.getFishVariantColor(variant);
            if (color != null) {
                DrawContext context = (DrawContext) (Object) this;
                context.drawText(renderer, "*", x + 12, y , color, false);
            }
        }
    }
}