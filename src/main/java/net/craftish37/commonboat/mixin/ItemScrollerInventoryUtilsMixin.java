package net.craftish37.commonboat.mixin;

import net.craftish37.commonboat.EasterEggFishHighlighter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "fi.dy.masa.itemscroller.util.InventoryUtils", remap = false)
public class ItemScrollerInventoryUtilsMixin {
    @Inject(method = "compareStacks", at = @At("HEAD"), cancellable = true)
    private static void onCompareStacks(@Coerce Object stack1Obj, @Coerce Object stack2Obj, CallbackInfoReturnable<Integer> cir) {
        ItemStack stack1 = (ItemStack) stack1Obj;
        ItemStack stack2 = (ItemStack) stack2Obj;

        if (stack1.isEmpty() || stack2.isEmpty()) return;

        if (isCustomSortable(stack1) && isCustomSortable(stack2)) {
            int id1 = EasterEggFishHighlighter.getCustomSortId(stack1);
            int id2 = EasterEggFishHighlighter.getCustomSortId(stack2);

            if (id1 != Integer.MAX_VALUE && id2 != Integer.MAX_VALUE) {
                cir.setReturnValue(Integer.compare(id1, id2));
            }
        }
    }
    @Unique
    private static boolean isCustomSortable(ItemStack stack) {
        return stack.isIn(net.minecraft.registry.tag.ItemTags.SHULKER_BOXES) ||
                stack.isIn(net.minecraft.registry.tag.ItemTags.BUNDLES) ||
                stack.getItem() == Items.TROPICAL_FISH_BUCKET ||
                stack.getItem() == Items.PLAYER_HEAD;
    }
}