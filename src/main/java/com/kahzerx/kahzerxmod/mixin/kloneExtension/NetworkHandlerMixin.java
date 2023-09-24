package com.kahzerx.kahzerxmod.mixin.kloneExtension;

import com.kahzerx.kahzerxmod.klone.KlonePlayerEntity;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
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

@Mixin(value = ServerCommonNetworkHandler.class, priority = 69420)
public abstract class NetworkHandlerMixin {
    @Shadow protected abstract GameProfile getProfile();

    @Shadow @Final protected MinecraftServer server;

    @Inject(method = "sendPacket(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (this.getPlayer() instanceof KlonePlayerEntity) {
            ci.cancel();
        }
    }

    @Inject(method = "disconnect", at = @At("HEAD"), cancellable = true)
    private void onDisconnect(Text reason, CallbackInfo ci) {
        if (this.getPlayer() instanceof KlonePlayerEntity) {
            ((KlonePlayerEntity) this.getPlayer()).kill(reason);
            ci.cancel();
        }
    }

    @Unique
    private ServerPlayerEntity getPlayer() {
        return this.server.getPlayerManager().getPlayer(this.getProfile().getId());
    }
}
