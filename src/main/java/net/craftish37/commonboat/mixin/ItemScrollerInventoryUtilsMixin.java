package net.craftish37.commonboat.mixin;

import net.craftish37.commonboat.CommonBoatConfig;
import net.craftish37.commonboat.ConfigAccess;
import net.craftish37.commonboat.EasterEggFishHighlighter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.BlockItem;
import net.minecraft.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;
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

        CommonBoatConfig cfg = ConfigAccess.get();
        if (cfg == null || !cfg.enabled || !cfg.customItemScrolling) {
            return;
        }

        if (stack1.isEmpty() && stack2.isEmpty()) return;

        boolean s1IsShulker = (stack1.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock);
        boolean s2IsShulker = (stack2.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock);

        boolean s1IsFish = (stack1.getItem() == Items.TROPICAL_FISH_BUCKET);
        boolean s2IsFish = (stack2.getItem() == Items.TROPICAL_FISH_BUCKET);

        boolean s1IsHead = (stack1.getItem() == Items.PLAYER_HEAD);
        boolean s2IsHead = (stack2.getItem() == Items.PLAYER_HEAD);

        if ((s1IsShulker && s2IsShulker) || (s1IsFish && s2IsFish) || (s1IsHead && s2IsHead)) {
            int id1 = EasterEggFishHighlighter.getCustomSortId(stack1);
            int id2 = EasterEggFishHighlighter.getCustomSortId(stack2);

            if (id1 != Integer.MAX_VALUE && id2 != Integer.MAX_VALUE) {
                cir.setReturnValue(Integer.compare(id1, id2));
            }
        }
    }
}