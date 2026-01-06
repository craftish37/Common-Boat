package net.craftish37.commonboat.mixin;

import net.craftish37.commonboat.EasterEggFishHighlighter;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(DrawContext.class)
public class DrawContextMixin {
    // Targets 'drawStackOverlay' in DrawContext, which is responsible for item counts, durability bars, etc.
    @Inject(method = "drawStackOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("TAIL"))
    private void commonboat$renderFishOverlay(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
        if (stack.isOf(Items.TROPICAL_FISH_BUCKET)) {
            // Use the helper to get the variant from the BUCKET_ENTITY_DATA component
            int variant = (int) EasterEggFishHighlighter.getTropicalFishVariant(stack);

            if (variant != 0) {
                // Get the highlight color
                int color = EasterEggFishHighlighter.getFishOverlayColor(Optional.of(variant));

                if (color != 0) {
                    // Draw the asterisk
                    // We cast 'this' to DrawContext to call drawText
                    ((DrawContext)(Object)this).drawText(renderer, Text.literal("*"), x + 10, y, color, false);
                }
            }
        }
    }
}
