package com.kahzerx.kahzerxmod.extensions.slabExtension.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

public class SlabUtils {
    public static boolean isSlab(Item item) {
        return Block.byItem(item) instanceof SlabBlock;
    }

    public static void flipSlab(Level world, BlockPos pos, BlockState state) {
        world.setBlockAndUpdate(pos, state.setValue(SlabBlock.TYPE, world.getBlockState(pos).getValue(SlabBlock.TYPE) == SlabType.DOUBLE ? SlabType.DOUBLE : SlabType.TOP));
    }
}
