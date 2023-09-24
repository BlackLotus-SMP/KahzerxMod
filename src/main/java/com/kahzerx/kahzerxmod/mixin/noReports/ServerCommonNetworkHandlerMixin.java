package com.kahzerx.kahzerxmod.mixin.noReports;

import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin implements ServerCommonPacketListener {
    @Shadow public abstract void sendPacket(Packet<?> packet);

    @Shadow @Final protected MinecraftServer server;

    @Inject(method = "sendPacket", at = @At(value = "HEAD"), cancellable = true)
    private void onSend(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ChatMessageS2CPacket chatPacket) {
            ci.cancel();
            this.replaceChatMessage(chatPacket);
        }
    }

    @Inject(method = "send", at = @At(value = "HEAD"), cancellable = true)
    private void onSend(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
        if (packet instanceof ChatMessageS2CPacket chatPacket) {
            ci.cancel();
            this.replaceChatMessage(chatPacket);
        }
    }

    @Unique
    private void replaceChatMessage(ChatMessageS2CPacket chatPacket) {
        GameMessageS2CPacket gamePacket = new GameMessageS2CPacket(
                chatPacket.serializedParameters().toParameters(this.server.getOverworld().getRegistryManager()).get().applyChatDecoration(
                        chatPacket.unsignedContent() != null ? chatPacket.unsignedContent() : Text.literal(chatPacket.body().content())
                ), false
        );
        this.sendPacket(gamePacket);
    }
}
