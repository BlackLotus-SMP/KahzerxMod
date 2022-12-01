package com.kahzerx.kahzerxmod.mixin.farmlandMyceliumExtension;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.HoeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HoeItem.class)
public class HoeItemMixin {
    @Redirect(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private BlockState onGetBlock(World instance, BlockPos pos) {
        Block b = instance.getBlockState(pos).getBlock();
        if (b == Blocks.MYCELIUM) {
            return Blocks.DIRT.getDefaultState();
        }
        return instance.getBlockState(pos);
    }
}
