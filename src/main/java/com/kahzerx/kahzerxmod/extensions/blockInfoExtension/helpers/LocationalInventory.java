package com.kahzerx.kahzerxmod.extensions.blockInfoExtension.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface LocationalInventory {
    BlockPos getLoc();
    Level getInvWorld();
}
