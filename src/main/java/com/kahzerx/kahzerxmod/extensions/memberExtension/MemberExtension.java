package com.kahzerx.kahzerxmod.extensions.memberExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.HashMap;

public class MemberExtension extends GenericExtension implements Extensions {
    public MemberExtension(HashMap<String, Boolean> config) {
        super(new ExtensionSettings(config, "member", "Gives member role on player first joined, it also creates member, mod and admin teams if not exist."));
    }

    @Override
    public void onPlayerJoined(ServerPlayerEntity player) {
        if (!this.getSettings().isEnabled()) {
            return;
        }
        MinecraftServer server = player.getServer();
        assert server != null;
        Collection<String> teamNames = server.getScoreboard().getTeamNames();
        if (!teamNames.contains("ADMIN")) {
            Team team = server.getScoreboard().addTeam("ADMIN");
            team.setPrefix(Text.literal("[ADMIN] ").styled(style -> style.withBold(true).withColor(Formatting.GOLD)));
            team.setShowFriendlyInvisibles(false);
        }
        if (!teamNames.contains("MOD")) {
            Team team = server.getScoreboard().addTeam("MOD");
            team.setPrefix(Text.literal("[MOD] ").styled(style -> style.withBold(true).withColor(Formatting.DARK_AQUA)));
            team.setShowFriendlyInvisibles(false);
        }
        if (!teamNames.contains("HELPER")) {
            Team team = server.getScoreboard().addTeam("HELPER");
            team.setPrefix(Text.literal("[HELPER] ").styled(style -> style.withBold(true).withColor(Formatting.AQUA)));
            team.setShowFriendlyInvisibles(false);
        }
        if (!teamNames.contains("MEMBER")) {
            Team team = server.getScoreboard().addTeam("MEMBER");
            team.setPrefix(Text.literal("[MEMBER] ").styled(style -> style.withBold(true).withColor(Formatting.DARK_PURPLE)));
            team.setShowFriendlyInvisibles(false);
        }
        if (!teamNames.contains("TEST_MEMBER")) {
            Team team = server.getScoreboard().addTeam("TEST_MEMBER");
            team.setPrefix(Text.literal("[TEST_MEMBER] ").styled(style -> style.withBold(true).withColor(Formatting.LIGHT_PURPLE)));
            team.setShowFriendlyInvisibles(false);
        }
        if (teamNames.contains("TEST_MEMBER")) {
            Team playerTeam = server.getScoreboard().getPlayerTeam(player.getName().getString());
            if (playerTeam != null) {
                return;
            }
            Team team = server.getScoreboard().getTeam("TEST_MEMBER");
            server.getScoreboard().addPlayerToTeam(player.getName().getString(), team);
        }
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    @Override
    public void onExtensionEnabled() { }

    @Override
    public void onExtensionDisabled() { }
}
