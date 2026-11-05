package com.kahzerx.kahzerxmod.mixin.maintenanceExtension;

import com.kahzerx.kahzerxmod.extensions.maintenanceExtension.MaintenanceExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;

@Mixin(PlayerList.class)
public abstract class PlayerManagerMixin {

    @Shadow public abstract boolean isOp(NameAndId profile);

    @Inject(method = "canPlayerLogin", at = @At("RETURN"), cancellable = true)
    public void isMaintenance(SocketAddress address, NameAndId profile, CallbackInfoReturnable<Component> cir) {
        if (MaintenanceExtension.isExtensionEnabled && !isOp(profile)) {
            cir.setReturnValue(Component.literal("Server is closed for maintenance"));
        }
    }
}
