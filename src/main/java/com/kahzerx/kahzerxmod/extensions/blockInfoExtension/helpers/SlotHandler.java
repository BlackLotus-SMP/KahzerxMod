package com.kahzerx.kahzerxmod.extensions.blockInfoExtension.helpers;

import net.minecraft.world.inventory.AbstractContainerMenu;

public interface SlotHandler {
    AbstractContainerMenu getHandler();
    void setHandler(AbstractContainerMenu handler);
}
