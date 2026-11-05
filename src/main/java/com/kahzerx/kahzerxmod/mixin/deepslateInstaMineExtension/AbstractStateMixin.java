package com.kahzerx.kahzerxmod.mixin.deepslateInstaMineExtension;

import com.kahzerx.kahzerxmod.extensions.deepslateInstaMineExtension.DeepslateInstaMineExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class AbstractStateMixin {
    @Shadow public abstract Block getBlock();
    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    public void onGetHardness(BlockGetter world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (DeepslateInstaMineExtension.isExtensionEnabled && (this.getBlock() == Blocks.DEEPSLATE || this.getBlock() == Blocks.COBBLED_DEEPSLATE)) {
            cir.setReturnValue(1.5F);
        }
    }
}
