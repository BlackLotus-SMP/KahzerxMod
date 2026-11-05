package com.kahzerx.kahzerxmod.mixin.kloneExtension;

import com.kahzerx.kahzerxmod.klone.KlonePlayerEntity;
import com.mojang.authlib.GameProfile;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerCommonPacketListenerImpl.class, priority = 69420)
public abstract class NetworkHandlerMixin {
    @Shadow protected abstract GameProfile playerProfile();

    @Shadow @Final protected MinecraftServer server;

    // send(Packet) delega en send(Packet, ChannelFutureListener), así que basta con este.
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, ChannelFutureListener listener, CallbackInfo ci) {
        if (this.getPlayer() instanceof KlonePlayerEntity) {
            ci.cancel();
        }
    }

    // El klon no tiene cliente que conteste al keep-alive: sin esto se desconectaba por timeout (~30 s) y moría.
    @Inject(method = "keepConnectionAlive", at = @At("HEAD"), cancellable = true)
    private void onKeepAlive(CallbackInfo ci) {
        if (this.getPlayer() instanceof KlonePlayerEntity) {
            ci.cancel();
        }
    }

    // disconnect(Component) delega en disconnect(DisconnectionDetails).
    @Inject(method = "disconnect(Lnet/minecraft/network/DisconnectionDetails;)V", at = @At("HEAD"), cancellable = true)
    private void onDisconnect(DisconnectionDetails details, CallbackInfo ci) {
        if (this.getPlayer() instanceof KlonePlayerEntity klone) {
            klone.kill(details.reason());
            ci.cancel();
        }
    }

    @Unique
    private ServerPlayer getPlayer() {
        return this.server.getPlayerList().getPlayer(this.playerProfile().id());
    }
}
