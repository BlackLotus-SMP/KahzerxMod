package com.kahzerx.kahzerxmod.mixin.fbiExtension;


import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import static com.kahzerx.kahzerxmod.extensions.fbiExtension.FBIExtension.getHiddenPlayers;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {
    @Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundPlayerInfoUpdatePacket;createPlayerInitializing(Ljava/util/Collection;)Lnet/minecraft/network/protocol/game/ClientboundPlayerInfoUpdatePacket;"))
    public ClientboundPlayerInfoUpdatePacket onConnect(Collection<ServerPlayer> players) {
        Collection<ServerPlayer> players_ = new ArrayList<>();
        for (ServerPlayer player : players) {
            if (!isHidden(player.getGameProfile())) {
                players_.add(player);
            }
        }
        return ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(players_);
    }

    private boolean isHidden(GameProfile profile) {
        for (ServerPlayer player : getHiddenPlayers()) {
            if (player.getGameProfile().equals(profile)) {
                return true;
            }
        }
        return false;
    }
}
