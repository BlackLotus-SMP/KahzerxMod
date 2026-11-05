package com.kahzerx.kahzerxmod.mixin.server;

import com.kahzerx.kahzerxmod.KahzerxServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@Mixin(ServerGamePacketListenerImpl.class)
public class ChatEventsMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "handleChat", at = @At("RETURN"))
    private void onChatMessage(ServerboundChatPacket packet, CallbackInfo ci) {
        KahzerxServer.onChatMessage(player, packet.message());
    }

    @Inject(method = "handleChatCommand", at = @At("HEAD"))
    private void onCommand(ServerboundChatCommandPacket packet, CallbackInfo ci) {
        KahzerxServer.onCommand(player, packet.command());
    }
}
