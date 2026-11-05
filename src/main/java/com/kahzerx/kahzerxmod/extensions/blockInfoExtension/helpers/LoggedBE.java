package com.kahzerx.kahzerxmod.extensions.blockInfoExtension.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public record LoggedBE(BlockPos pos, Level world) {
    public BlockPos getPos() {
        return pos;
    }

    public Level getWorld() {
        return world;
    }
}
