package com.kahzerx.kahzerxmod.mixin.noReports;

import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.PlayerSessionC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerSessionC2SPacket.class)
public class PlayerSessionC2SPacketMixin {
    @Inject(method = "apply(Lnet/minecraft/network/listener/ServerPlayPacketListener;)V", at = @At("HEAD"), cancellable = true)
    private void onHandle(ServerPlayPacketListener serverPlayPacketListener, CallbackInfo ci) {
        ci.cancel();
    }
}
