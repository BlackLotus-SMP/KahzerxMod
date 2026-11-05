package com.kahzerx.kahzerxmod.mixin.noReports;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonNetworkHandlerMixin implements ServerCommonPacketListener {
    @Shadow public abstract void send(Packet<?> packet);

    // send(Packet) delega en send(Packet, ChannelFutureListener), así que basta con este.
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At(value = "HEAD"), cancellable = true)
    private void onSend(Packet<?> packet, ChannelFutureListener listener, CallbackInfo ci) {
        if (packet instanceof ClientboundPlayerChatPacket chatPacket) {
            ci.cancel();
            this.replaceChatMessage(chatPacket);
        }
    }

    @Unique
    private void replaceChatMessage(ClientboundPlayerChatPacket chatPacket) {
        Component content = chatPacket.unsignedContent().orElseGet(() -> Component.literal(chatPacket.body().content()));
        this.send(new ClientboundSystemChatPacket(chatPacket.chatType().decorate(content), false));
    }
}
