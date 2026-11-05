package com.kahzerx.kahzerxmod.mixin.noReports;

import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundChatSessionUpdatePacket.class)
public class PlayerSessionC2SPacketMixin {
    @Inject(method = "handle(Lnet/minecraft/network/protocol/game/ServerGamePacketListener;)V", at = @At("HEAD"), cancellable = true)
    private void onHandle(ServerGamePacketListener serverPlayPacketListener, CallbackInfo ci) {
        ci.cancel();
    }
}
