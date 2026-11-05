package com.kahzerx.kahzerxmod.mixin.blockInfoExtension;

import com.kahzerx.kahzerxmod.extensions.blockInfoExtension.helpers.ActionTypes;
import com.kahzerx.kahzerxmod.extensions.blockInfoExtension.helpers.BlockActionLog;
import com.kahzerx.kahzerxmod.extensions.blockInfoExtension.BlockInfoExtension;
import com.kahzerx.kahzerxmod.extensions.blockInfoExtension.utils.BlockInfoUtils;
import com.kahzerx.kahzerxmod.utils.DateUtils;
import com.kahzerx.kahzerxmod.utils.DimUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class BlockInteractionLoggingMixin {
    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSecondaryUseActive()Z"))
    private void onRightClick(ServerPlayer player, Level world, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (BlockInfoUtils.shouldRegisterBlock(world.getBlockState(hitResult.getBlockPos()).getBlock(), player)) {
            BlockActionLog actionLog = new BlockActionLog(
                    player.getName().getString(),
                    1,
                    world.getBlockState(hitResult.getBlockPos()).getBlock().getName().getString(),
                    hitResult.getBlockPos().getX(),
                    hitResult.getBlockPos().getY(),
                    hitResult.getBlockPos().getZ(),
                    DimUtils.getWorldID(DimUtils.getDim(player.level())),
                    ActionTypes.INTERACT.getId(),
                    DateUtils.getDate()
            );
            BlockInfoExtension.enqueue(actionLog);
        } else if (BlockInfoUtils.shouldRegisterItem(player, stack)) {
            BlockActionLog actionLog = new BlockActionLog(
                    player.getName().getString(),
                    1,
                    stack.getItem().getName(stack).getString(),
                    hitResult.getBlockPos().getX(),
                    hitResult.getBlockPos().getY(),
                    hitResult.getBlockPos().getZ(),
                    DimUtils.getWorldID(DimUtils.getDim(player.level())),
                    ActionTypes.INTERACT.getId(),
                    DateUtils.getDate()
            );
            BlockInfoExtension.enqueue(actionLog);
        }
    }
}
