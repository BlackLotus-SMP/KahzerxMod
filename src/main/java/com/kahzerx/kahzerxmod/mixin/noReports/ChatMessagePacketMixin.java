package com.kahzerx.kahzerxmod.mixin.noReports;

import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ServerboundChatPacket.class)
public class ChatMessagePacketMixin {
    // En 26.x signature() devuelve Optional: con null, getSignedMessage haría .orElse(null) sobre null (NPE en cada mensaje).
    @Inject(method = "signature", at = @At("HEAD"), cancellable = true)
    private void onSignature(CallbackInfoReturnable<Optional<MessageSignature>> cir) {
        cir.setReturnValue(Optional.empty());
    }
}
