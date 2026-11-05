package com.kahzerx.kahzerxmod.mixin.pranksExtension;

import com.kahzerx.kahzerxmod.extensions.prankExtension.PrankExtension;
import com.kahzerx.kahzerxmod.extensions.prankExtension.PrankLevel;
import com.kahzerx.kahzerxmod.klone.KlonePlayerEntity;
import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;


@Mixin(ServerPlayer.class)
public abstract class PlayerMixin extends Player {
    @Shadow @Final private MinecraftServer server;

    public PlayerMixin(Level world, GameProfile gameProfile) {
        super(world, gameProfile);
    }


    @Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
    private void onGet(CallbackInfoReturnable<Component> cir) {
        MutableComponent name = Component.literal("").append(this.getDisplayName());
        if (PrankExtension.isExtensionEnabled && PrankExtension.playerLevel.containsKey(this.getStringUUID()) && PrankExtension.playerLevel.get(this.getStringUUID()) != PrankLevel.LEVEL0) {
            name.append(PrankExtension.playerLevel.get(this.getStringUUID()).getFormatting() + " " + PrankExtension.playerLevel.get(this.getStringUUID()).getIdentifier());
        }
        ServerPlayer p = server.getPlayerList().getPlayer(UUID.fromString(this.getStringUUID()));
        if (p != null && p.getClass() == KlonePlayerEntity.class) {
            name.append(Component.literal(" [Bot]").withStyle(style -> style.withColor(ChatFormatting.GRAY)));
        }
        cir.setReturnValue(name);
    }
}
