package com.kahzerx.kahzerxmod.extensions.memberExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import java.util.Collection;
import java.util.HashMap;

public class MemberExtension extends GenericExtension implements Extensions {
    public MemberExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "member", "Gives member role on player first joined, it also creates member, mod and admin teams if not exist."));
    }

    @Override
    public void onPlayerJoined(ServerPlayer player) {
        if (!this.getSettings().isEnabled()) {
            return;
        }
        MinecraftServer server = player.level().getServer();
        assert server != null;
        Collection<String> teamNames = server.getScoreboard().getTeamNames();
        if (!teamNames.contains("ADMIN")) {
            PlayerTeam team = server.getScoreboard().addPlayerTeam("ADMIN");
            team.setPlayerPrefix(Component.literal("[ADMIN] ").withStyle(style -> style.withBold(true).withColor(ChatFormatting.GOLD)));
            team.setSeeFriendlyInvisibles(false);
        }
        if (!teamNames.contains("MOD")) {
            PlayerTeam team = server.getScoreboard().addPlayerTeam("MOD");
            team.setPlayerPrefix(Component.literal("[MOD] ").withStyle(style -> style.withBold(true).withColor(ChatFormatting.DARK_AQUA)));
            team.setSeeFriendlyInvisibles(false);
        }
        if (!teamNames.contains("HELPER")) {
            PlayerTeam team = server.getScoreboard().addPlayerTeam("HELPER");
            team.setPlayerPrefix(Component.literal("[HELPER] ").withStyle(style -> style.withBold(true).withColor(ChatFormatting.AQUA)));
            team.setSeeFriendlyInvisibles(false);
        }
        if (!teamNames.contains("MEMBER")) {
            PlayerTeam team = server.getScoreboard().addPlayerTeam("MEMBER");
            team.setPlayerPrefix(Component.literal("[MEMBER] ").withStyle(style -> style.withBold(true).withColor(ChatFormatting.DARK_PURPLE)));
            team.setSeeFriendlyInvisibles(false);
        }
        if (!teamNames.contains("TEST_MEMBER")) {
            PlayerTeam team = server.getScoreboard().addPlayerTeam("TEST_MEMBER");
            team.setPlayerPrefix(Component.literal("[TEST_MEMBER] ").withStyle(style -> style.withBold(true).withColor(ChatFormatting.LIGHT_PURPLE)));
            team.setSeeFriendlyInvisibles(false);
        }
        if (teamNames.contains("TEST_MEMBER")) {
            PlayerTeam playerTeam = server.getScoreboard().getPlayersTeam(player.getName().getString());
            if (playerTeam != null) {
                return;
            }
            PlayerTeam team = server.getScoreboard().getPlayerTeam("TEST_MEMBER");
            server.getScoreboard().addPlayerToTeam(player.getName().getString(), team);
        }
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    @Override
    public void onExtensionEnabled(CommandSourceStack source) { }

    @Override
    public void onExtensionDisabled(CommandSourceStack source) { }
}
