package net.craftish37.commonboat.mixin;

import net.craftish37.commonboat.EasterEggFishHighlighter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public class ItemRendererMixin {
    @Inject(method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;III)V", at = @At("HEAD"))
    private void renderFishHighlight(LivingEntity entity, ItemStack stack, int x, int y, int seed, CallbackInfo ci) {
        if (stack.isEmpty()) return;
        float[] color = EasterEggFishHighlighter.getItemHighlightColor(stack);
        if (color != null) {
            DrawContext context = (DrawContext) (Object) this;
            int r = (int) (color[0] * 255);
            int g = (int) (color[1] * 255);
            int b = (int) (color[2] * 255);
            int colorInt = (255 << 24) | (r << 16) | (g << 8) | b;

            context.fillGradient(x, y, x + 16, y + 1, colorInt, colorInt);
            context.fillGradient(x, y + 15, x + 16, y + 16, colorInt, colorInt);
            context.fillGradient(x, y + 1, x + 1, y + 15, colorInt, colorInt);
            context.fillGradient(x + 15, y + 1, x + 16, y + 15, colorInt, colorInt);
        }
    }
}