package com.kahzerx.kahzerxmod.mixin.slabExtension;

import com.kahzerx.kahzerxmod.extensions.slabExtension.SlabExtension;
import com.kahzerx.kahzerxmod.extensions.slabExtension.utils.SlabUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SlabBlock.class)
public class SlabBlockExtension extends Block {
    public SlabBlockExtension(Properties settings) {
        super(settings);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (SlabExtension.isExtensionEnabled && SlabUtils.isSlab(itemStack.getItem()) && itemStack.isEnchanted()) {
            SlabUtils.flipSlab(world, pos, state);
        }
    }
}
