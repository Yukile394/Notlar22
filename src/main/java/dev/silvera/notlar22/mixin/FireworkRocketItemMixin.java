package dev.silvera.notlar22.mixin;

import dev.silvera.notlar22.Notlar22;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PaneBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
        if (notlar22$isPassThrough(state)) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void notlar22$blockRepeat(World world, PlayerEntity user, Hand hand,
                                      CallbackInfoReturnable<ActionResult> cir) {
        if (!Notlar22.isEnabled() || !user.isGliding()) {
            return;
        }
        if (user.getItemCooldownManager().isCoolingDown(user.getStackInHand(hand))) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    @Inject(method = "use", at = @At("RETURN"))
    private void notlar22$startCooldown(World world, PlayerEntity user, Hand hand,
                                        CallbackInfoReturnable<ActionResult> cir) {
        if (!Notlar22.isEnabled() || !user.isGliding()) {
            return;
        }
        ActionResult result = cir.getReturnValue();
        if (result != null && result.isAccepted()) {
            user.getItemCooldownManager().set(new ItemStack(Items.FIREWORK_ROCKET), 8);
        }
    }

    @Unique
    private static boolean notlar22$isPassThrough(BlockState state) {
        if (state.isOf(Blocks.COBWEB) || state.isOf(Blocks.OAK_PLANKS) || state.isOf(Blocks.BEDROCK)) {
            return true;
        }
        if (state.isIn(BlockTags.IMPERMEABLE)) {
            return true;
        }
        return state.getBlock() instanceof PaneBlock && !state.isOf(Blocks.IRON_BARS);
    }
}
