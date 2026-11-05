package com.kahzerx.kahzerxmod.mixin.fbiExtension;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.server.level.ServerPlayer;

import static com.kahzerx.kahzerxmod.extensions.fbiExtension.FBIExtension.getHiddenPlayers;

@Mixin(MsgCommand.class)
public class MessageCommandMixin {
    @Inject(method = "sendMessage", at = @At(value = "HEAD"), cancellable = true)
    private static void onSent(CommandSourceStack source, Collection<ServerPlayer> targets, PlayerChatMessage message, CallbackInfo ci) {
        if (targets.size() == 1) {
            for (ServerPlayer player : targets) {
                if (getHiddenPlayers().contains(player)) {
                    source.sendSuccess(() -> Component.translatable("argument.entity.notfound.player").withStyle(ChatFormatting.RED), false);
                    ci.cancel();
                }
            }
        }
    }
}
