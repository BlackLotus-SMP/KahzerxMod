package com.kahzerx.kahzerxmod.mixin.kloneExtension;

import com.kahzerx.kahzerxmod.klone.KlonePlayerEntity;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static com.kahzerx.kahzerxmod.extensions.kloneExtension.KloneExtension.klones;

@Mixin(value = PlayerManager.class, priority = 5000)
public abstract class PlayerManagerMixin {
    @Shadow public abstract List<ServerPlayerEntity> getPlayerList();

    @Inject(method = "createPlayer", at = @At("HEAD"))
    private void onCreatePlayer(GameProfile profile, PlayerPublicKey publicKey, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        for (ServerPlayerEntity player : getPlayerList()) {
            if (player.getGameProfile().getId().equals(profile.getId()) && player.getClass() == KlonePlayerEntity.class) {
                player.kill();
                klones.remove(player);
            }
        }
    }
}
