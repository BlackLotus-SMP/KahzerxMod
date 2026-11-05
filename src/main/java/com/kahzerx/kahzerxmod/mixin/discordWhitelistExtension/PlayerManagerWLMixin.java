package com.kahzerx.kahzerxmod.mixin.discordWhitelistExtension;

import com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistExtension.DiscordWhitelistExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserBanList;


public class PlayerManagerWLMixin {
    @Mixin(PlayerList.class)
    public static abstract class PlayerNotWhitelisted {
        @Shadow @Final private UserBanList bans;

        @Shadow public abstract boolean isWhiteListed(NameAndId profile);

        @Inject(method = "canPlayerLogin", at = @At(value = "HEAD"), cancellable = true)
        private void onCheckWhitelisted(SocketAddress address, NameAndId profile, CallbackInfoReturnable<Component> cir) {
            if (!bans.isBanned(profile) && DiscordWhitelistExtension.isExtensionEnabled) {
                if (!isWhiteListed(profile)) {
                    cir.setReturnValue(Component.literal(String.format("You are not whitelisted :(\n!add %s on discord.", profile.name())));
                }
            }
        }
    }
}
