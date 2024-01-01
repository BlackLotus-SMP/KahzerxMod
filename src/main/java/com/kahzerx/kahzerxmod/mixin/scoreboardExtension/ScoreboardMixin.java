package com.kahzerx.kahzerxmod.mixin.scoreboardExtension;

import com.kahzerx.kahzerxmod.extensions.scoreboardExtension.ScoreboardExtension;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;
import net.minecraft.scoreboard.*;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerScoreboard.class)
public class ScoreboardMixin extends Scoreboard {
    @Shadow @Final private MinecraftServer server;

    @Inject(method = "createChangePackets", at = @At(value = "RETURN"))
    private void onCreate(ScoreboardObjective objective, CallbackInfoReturnable<List<Packet<?>>> cir) {
        if (ScoreboardExtension.isExtensionEnabled) {
            int i = 0;
            for (ScoreboardEntry se : getScoreboardEntries(objective)) {
                i += se.value();
            }
            cir.getReturnValue().add(new ScoreboardScoreUpdateS2CPacket(Formatting.BOLD + "TOTAL", objective.getName(), i, Text.literal(Formatting.BOLD + "TOTAL"), new StyledNumberFormat(Style.EMPTY)));
        }
    }

    @Inject(method = "updateScore", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/packet/Packet;)V"))
    private void onUpdate(ScoreHolder scoreHolder, ScoreboardObjective objective, ScoreboardScore score, CallbackInfo ci) {
        if (ScoreboardExtension.isExtensionEnabled) {
            if (objective == null) {
                return;
            }
            int i = 0;
            for (ScoreboardEntry se : getScoreboardEntries(objective)) {
                i += se.value();
            }
            server.getPlayerManager().sendToAll(new ScoreboardScoreUpdateS2CPacket(Formatting.BOLD + "TOTAL", objective.getName(), i, Text.literal(Formatting.BOLD + "TOTAL"), new StyledNumberFormat(Style.EMPTY)));
        }
    }
}
