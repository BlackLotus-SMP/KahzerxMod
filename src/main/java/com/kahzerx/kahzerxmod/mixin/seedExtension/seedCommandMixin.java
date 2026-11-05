package com.kahzerx.kahzerxmod.mixin.seedExtension;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.SeedCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SeedCommand.class)
public class seedCommandMixin {
    // TODO inject in the hasPermission method
    @Inject(method = "register", at = @At(value = "HEAD"), cancellable = true)
    private static void onRegisterCommand(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated, CallbackInfo ci) {
        ci.cancel();
    }
}
