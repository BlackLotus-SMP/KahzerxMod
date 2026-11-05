package com.kahzerx.kahzerxmod.mixin.farmlandMyceliumExtension;

import com.kahzerx.kahzerxmod.extensions.farmlandMyceliumExtension.FarmlandMyceliumExtension;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Desde 26.x el arado con azada es data-driven (BlockTransformer "hoe" + tag turns_into_farmland), ya no existe HoeItem.
// Con la extensión activa, el micelio se ara como si fuera tierra.
@Mixin(BlockTransformer.class)
public class HoeItemMixin {
    @Inject(method = "transformBlock", at = @At("HEAD"), cancellable = true)
    private void onTransformBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!FarmlandMyceliumExtension.isExtensionEnabled) {
            return;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.getBlockState(pos).is(Blocks.MYCELIUM) || context.getClickedFace() == Direction.DOWN || !level.getBlockState(pos.above()).isAir()) {
            return;
        }
        BlockTransformer hoe = level.registryAccess().lookupOrThrow(Registries.BLOCK_TRANSFORMER).getValue(BlockTransformers.HOE);
        if (hoe != (Object) this) {
            return;
        }
        Player player = context.getPlayer();
        // Igual que vanilla: con escudo en la otra mano y sin agacharse no se transforma.
        if (player != null && context.getHand() == InteractionHand.MAIN_HAND && player.getOffhandItem().has(DataComponents.BLOCKS_ATTACKS) && !player.isSecondaryUseActive()) {
            return;
        }
        ItemStack itemInHand = context.getItemInHand();
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, itemInHand);
        }
        if (player != null) {
            itemInHand.hurtAndBreak(1, player, context.getHand().asEquipmentSlot());
        }
        BlockState farmland = Blocks.FARMLAND.defaultBlockState();
        level.setBlock(pos, farmland, 11);
        // null: el cliente no predice este arado, así que el sonido se manda también a quien ara.
        level.playSound(null, pos, SoundEvents.HOE_TILL.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, farmland));
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
