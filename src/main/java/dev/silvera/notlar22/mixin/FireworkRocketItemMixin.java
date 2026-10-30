package dev.silvera.notlar22.mixin;

import dev.silvera.notlar22.Notlar22;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public abstract class FireworkRocketItemMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void notlar22$passThrough(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (!Notlar22.isEnabled()) {
            return;
        }
        BlockState state = context.getWorld().getBlockState(context.getBlockPos());
        if (state.isOf(Blocks.COBWEB) || state.isOf(Blocks.OAK_PLANKS)) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }
}
