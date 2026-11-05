package com.kahzerx.kahzerxmod.mixin.scoreboardExtension;

import com.kahzerx.kahzerxmod.extensions.scoreboardExtension.ScoreboardExtension;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(ServerScoreboard.class)
public class ScoreboardMixin extends Scoreboard {
    @Shadow @Final private MinecraftServer server;

    @Inject(method = "getStartTrackingPackets", at = @At(value = "RETURN"))
    private void onCreate(Objective objective, CallbackInfoReturnable<List<Packet<?>>> cir) {
        if (ScoreboardExtension.isExtensionEnabled) {
            int i = 0;
            for (PlayerScoreEntry se : listPlayerScores(objective)) {
                i += se.value();
            }
            cir.getReturnValue().add(new ClientboundSetScorePacket(ChatFormatting.BOLD + "TOTAL", objective.getName(), i, Optional.of(Component.literal(ChatFormatting.BOLD + "TOTAL")), Optional.of(new StyledFormat(Style.EMPTY))));
        }
    }

    @Inject(method = "onScoreChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void onUpdate(ScoreHolder scoreHolder, Objective objective, Score score, CallbackInfo ci) {
        if (ScoreboardExtension.isExtensionEnabled) {
            if (objective == null) {
                return;
            }
            int i = 0;
            for (PlayerScoreEntry se : listPlayerScores(objective)) {
                i += se.value();
            }
            server.getPlayerList().broadcastAll(new ClientboundSetScorePacket(ChatFormatting.BOLD + "TOTAL", objective.getName(), i, Optional.of(Component.literal(ChatFormatting.BOLD + "TOTAL")), Optional.of(new StyledFormat(Style.EMPTY))));
        }
    }
}
