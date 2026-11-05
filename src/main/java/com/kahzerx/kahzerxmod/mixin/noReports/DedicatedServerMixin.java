package com.kahzerx.kahzerxmod.mixin.noReports;

import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public class DedicatedServerMixin {
    @Inject(method = "enforceSecureProfile", at = @At("HEAD"), cancellable = true)
    private void onSecure(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
