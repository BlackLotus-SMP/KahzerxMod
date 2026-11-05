package com.kahzerx.kahzerxmod.mixin.blockInfoExtension;

import com.kahzerx.kahzerxmod.extensions.blockInfoExtension.*;
import com.kahzerx.kahzerxmod.extensions.blockInfoExtension.helpers.*;
import com.kahzerx.kahzerxmod.utils.DateUtils;
import com.kahzerx.kahzerxmod.utils.DimUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class ContainerInteractionLoggingMixin {
    @Mixin(AbstractContainerMenu.class)
    public static abstract class ScreenHandlerMixin implements PlayerHandler {
        @Shadow public ItemStack quickMoveStack(Player player, int index) {
            this.player = player;
            return null;
        }

        @Unique private Player player = null;

        @Inject(method = "addSlot", at = @At("HEAD"))
        private void onAddSlot(Slot slot, CallbackInfoReturnable<Slot> cir) {
            ((SlotHandler) slot).setHandler((AbstractContainerMenu) (Object) this);
        }

        @Inject(method = "clickMenuButton", at = @At("HEAD"))
        private void onButtonClick(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
            this.player = player;
        }

        // TODO esto está to mal xd
//        @Inject(method = "transferSlot", at = @At("INVOKE"))
//        private void onTransferSlot(PlayerEntity player, int index, CallbackInfoReturnable<ItemStack> cir) {
//            this.player =  player;
//        }

        @Inject(method = "clicked", at = @At("HEAD"))
        private void onSlotClick(int slotIndex, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
            this.player = player;
        }

        @Inject(method = "clearContainer", at = @At("HEAD"))
        private void onDrop(Player player, Container inventory, CallbackInfo ci) {
            this.player = player;
        }

        @Override
        public Player getPlayer() {
            return player;
        }
    }

    @Mixin(CompoundContainer.class)
    public static class DoubleInventoryMixin implements DoubleInventoryHelper {
        @Shadow @Final private Container container1;

        @Shadow @Final private Container container2;

        @Override
        public Container getInventory(int slot) {
            return slot >= this.container1.getContainerSize() ? this.container2 : container1;
        }
    }

    @Mixin(Slot.class)
    public static abstract class SlotMixin implements SlotHandler {
        @Shadow public abstract ItemStack getItem();

        @Shadow @Final public Container container;

        @Shadow public abstract int getContainerSlot();

        private AbstractContainerMenu handler = null;
        private ItemStack oldStack = null;

        @Override
        public AbstractContainerMenu getHandler() {
            return handler;
        }

        @Override
        public void setHandler(AbstractContainerMenu handler) {
            this.handler = handler;
            this.oldStack = this.getItem() == null ? ItemStack.EMPTY : this.getItem().copy();
        }

        @Inject(method = "setChanged", at = @At("HEAD"))
        private void logChanges(CallbackInfo ci) {
            LoggedBE be = getBE();
            PlayerHandler playerHandler = (PlayerHandler) handler;

            if (be != null && playerHandler.getPlayer() != null) {
                log(playerHandler.getPlayer(), oldStack, this.getItem().copy(), be);
            }

            oldStack = this.getItem().copy();
        }

        private LoggedBE getBE() {
            Container inv = this.container;
            if (inv instanceof DoubleInventoryHelper) {
                inv = ((DoubleInventoryHelper) inv).getInventory(this.getContainerSlot());
            }
            if (inv instanceof LocationalInventory) {
                return new LoggedBE(((LocationalInventory) inv).getLoc(), ((LocationalInventory) inv).getInvWorld());
            }
            return null;
        }

        private void log(Player player, ItemStack stack, ItemStack newStack, LoggedBE loggedBE) {
            if (stack.isEmpty() && newStack.isEmpty()) {
                return;
            }
            if (!stack.isEmpty() && !newStack.isEmpty()) {
                if (stack.getItem() == newStack.getItem()) {
                    int oCnt = stack.getCount();
                    int nCnt = newStack.getCount();
                    if (nCnt > oCnt) {
                        log(player, ItemStack.EMPTY, new ItemStack(newStack.getItem(), nCnt - oCnt), loggedBE);
                    } else {
                        log(player, new ItemStack(newStack.getItem(), oCnt - nCnt), ItemStack.EMPTY, loggedBE);
                    }
                } else {
                    log(player, stack, ItemStack.EMPTY, loggedBE);
                    log(player, ItemStack.EMPTY, newStack, loggedBE);
                }
                return;
            }
            boolean oldEmpty = stack.isEmpty();
            ItemStack updatedStack = oldEmpty ? newStack : stack;
            BlockInfoExtension.enqueue(new BlockActionLog(
                    player.getName().getString(),
                    updatedStack.getCount(),
                    updatedStack.getItem().getName(updatedStack).getString(),
                    loggedBE.getPos().getX(),
                    loggedBE.getPos().getY(),
                    loggedBE.getPos().getZ(),
                    DimUtils.getWorldID(DimUtils.getDim(loggedBE.getWorld())),
                    oldEmpty ? ActionTypes.ADDED.getId() : ActionTypes.REMOVED.getId(),
                    DateUtils.getDate()
            ));
        }
    }

    @Mixin(BaseContainerBlockEntity.class)
    public static class LockableContainerBlockEntityMixin extends BlockEntity implements LocationalInventory {
        public LockableContainerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }

        @Override
        public BlockPos getLoc() {
            return this.getBlockPos();
        }

        @Override
        public Level getInvWorld() {
            return this.getLevel();
        }
    }
}
