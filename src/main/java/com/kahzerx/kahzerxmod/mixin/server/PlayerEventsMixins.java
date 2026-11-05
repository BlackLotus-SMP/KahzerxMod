package com.kahzerx.kahzerxmod.mixin.server;

import com.kahzerx.kahzerxmod.KahzerxServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class PlayerEventsMixins {
    @Mixin(PlayerList.class)
    public static class PlayerConn {
        @Inject(method = "remove", at = @At("RETURN"))
        private void onPlayerLeft(ServerPlayer player, CallbackInfo ci) {
            KahzerxServer.onPlayerLeft(player);
        }

        // En 26.x los datos del jugador ya vienen cargados al entrar en placeNewPlayer (antes: justo tras PlayerList#load).
        @Inject(method = "placeNewPlayer", at = @At("HEAD"))
        private void onPlayerJoined(Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo ci) {
            KahzerxServer.onPlayerJoined(player);
        }

        @Inject(method = "placeNewPlayer", at = @At("TAIL"))
        private void onPlayerConnected(Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo ci) {
            KahzerxServer.onPlayerConnected(player);
        }
    }

    @Mixin(ServerPlayerGameMode.class)
    public static class PlayerBreak {
        @Shadow @Final protected ServerPlayer player;

        @Shadow protected ServerLevel level;

        @Inject(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;destroy(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", shift = At.Shift.BEFORE))
        private void onBroken(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
            KahzerxServer.onPlayerBreakBlock(player, level, pos);
        }
    }

    @Mixin(BlockItem.class)
    public static class PlayerPlace {
        @Redirect(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;setPlacedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V"))
        private void onPlaced(Block instance, Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
            instance.setPlacedBy(world, pos, state, placer, itemStack);
            if (placer instanceof ServerPlayer) {
                KahzerxServer.onPlayerPlaceBlock((ServerPlayer) placer, world, pos);
            }
        }
    }

    @Mixin(ServerPlayer.class)
    public static class PlayerDied {
        @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatTracker;getDeathMessage()Lnet/minecraft/network/chat/Component;"))
        private void onPlayerDied(DamageSource source, CallbackInfo ci) {
            KahzerxServer.onPlayerDied((ServerPlayer) (Object) this);
        }

    }

    // En 26.x ServerPlayer ya no sobrescribe swing: tanto handlePunch como swingAndResetAttackStrength acaban aquí.
    @Mixin(LivingEntity.class)
    public static class PlayerSwing {
        @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/component/SwingAnimation;Z)Z", at = @At("RETURN"))
        private void onPlayerClicker(InteractionHand hand, SwingAnimation animation, boolean sendToSwingingEntity, CallbackInfoReturnable<Boolean> cir) {
            if ((Object) this instanceof ServerPlayer player) {
                KahzerxServer.onClick(player);
            }
        }
    }

    @Mixin(PlayerAdvancements.class)
    public static class PlayerAdvancement {
        @Shadow private ServerPlayer player;

        // lambda de award(): holder.value().display().ifPresent(display -> ... broadcastSystemMessage(...))
        @Inject(method = "lambda$award$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
        private void onAdvancement(AdvancementHolder advancementEntry, DisplayInfo display, CallbackInfo ci) {
            Component text = display.type().createAnnouncement(advancementEntry, player);
            KahzerxServer.onAdvancement(text.getString().replace("_", "\\_"));
        }
    }

    @Mixin(ServerPlayer.class)
    public static class PlayerSleeping {
        // En 26.x startSleeping devuelve si ha podido dormir; solo se avisa si lo ha hecho.
        @Inject(method = "startSleeping", at = @At("RETURN"))
        private void onSleep(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
            if (cir.getReturnValueZ()) {
                KahzerxServer.onSleep((ServerPlayer) (Object) this);
            }
        }

        @Inject(method = "stopSleepInBed", at = @At("HEAD"))
        private void onWakeUp(boolean skipSleepTimer, boolean updateSleepingPlayers, CallbackInfo ci) {
            if (updateSleepingPlayers) {
                KahzerxServer.onWakeUp((ServerPlayer) (Object) this);
            }
        }
    }
}
