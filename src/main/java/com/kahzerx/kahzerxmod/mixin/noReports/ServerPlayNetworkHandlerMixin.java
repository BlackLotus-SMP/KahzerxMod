package com.kahzerx.kahzerxmod.mixin.noReports;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin implements ServerPlayPacketListener {
    @Shadow public abstract void sendPacket(Packet<?> packet);

    @Shadow public ServerPlayerEntity player;

    @Inject(method = "sendPacket(Lnet/minecraft/network/packet/Packet;)V", at = @At(value = "HEAD"), cancellable = true)
    private void onSend(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ChatMessageS2CPacket chatPacket) {
            ci.cancel();
            this.replaceChatMessage(chatPacket);
        }
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At(value = "HEAD"), cancellable = true)
    private void onSend(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
        if (packet instanceof ChatMessageS2CPacket chatPacket) {
            ci.cancel();
            this.replaceChatMessage(chatPacket);
        }
    }

    private void replaceChatMessage(ChatMessageS2CPacket chatPacket) {
        GameMessageS2CPacket gamePacket = new GameMessageS2CPacket(
                chatPacket.serializedParameters().toParameters(this.player.world.getRegistryManager()).get().applyChatDecoration(
                        chatPacket.unsignedContent() != null ? chatPacket.unsignedContent() : Text.literal(chatPacket.body().content())
                ), false
        );
        this.sendPacket(gamePacket);
    }
}
