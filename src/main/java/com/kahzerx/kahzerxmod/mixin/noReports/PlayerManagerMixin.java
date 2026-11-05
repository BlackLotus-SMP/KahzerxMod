package com.kahzerx.kahzerxmod.mixin.noReports;

import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {
    @Inject(method = "verifyChatTrusted", at = @At(value = "RETURN"), cancellable = true)
    public void onVerify(PlayerChatMessage message, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
