package com.kahzerx.kahzerxmod.mixin.fbiExtension;


import com.mojang.authlib.GameProfile;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Collection;

import static com.kahzerx.kahzerxmod.extensions.fbiExtension.FBIExtension.getHiddenPlayers;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/PlayerListS2CPacket;entryFromPlayer(Ljava/util/Collection;)Lnet/minecraft/network/packet/s2c/play/PlayerListS2CPacket;"))
    public PlayerListS2CPacket onConnect(Collection<ServerPlayerEntity> players) {
        Collection<ServerPlayerEntity> players_ = new ArrayList<>();
        for (ServerPlayerEntity player : players) {
            if (!isHidden(player.getGameProfile())) {
                players_.add(player);
            }
        }
        return PlayerListS2CPacket.entryFromPlayer(players_);
    }

    private boolean isHidden(GameProfile profile) {
        for (ServerPlayerEntity player : getHiddenPlayers()) {
            if (player.getGameProfile().equals(profile)) {
                return true;
            }
        }
        return false;
    }
}
