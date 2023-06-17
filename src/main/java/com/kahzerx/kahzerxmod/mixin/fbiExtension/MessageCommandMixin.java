package com.kahzerx.kahzerxmod.mixin.fbiExtension;


import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.command.MessageCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

import static com.kahzerx.kahzerxmod.extensions.fbiExtension.FBIExtension.getHiddenPlayers;

@Mixin(MessageCommand.class)
public class MessageCommandMixin {
    @Inject(method = "execute", at = @At(value = "HEAD"), cancellable = true)
    private static void onSent(ServerCommandSource source, Collection<ServerPlayerEntity> targets, SignedMessage message, CallbackInfo ci) {
        if (targets.size() == 1) {
            for (ServerPlayerEntity player : targets) {
                if (getHiddenPlayers().contains(player)) {
                    source.sendFeedback(() -> Text.translatable("argument.entity.notfound.player").formatted(Formatting.RED), false);
                    ci.cancel();
                }
            }
        }
    }
}
