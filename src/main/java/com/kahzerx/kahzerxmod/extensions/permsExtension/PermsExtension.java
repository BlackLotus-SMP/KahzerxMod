package com.kahzerx.kahzerxmod.extensions.permsExtension;

import com.google.common.collect.Sets;
import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.extensions.memberExtension.MemberExtension;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.kahzerx.kahzerxmod.utils.PlayerUtils;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class PermsExtension extends GenericExtension implements Extensions {
    private final HashMap<String, PermsLevels> playerPerms = new HashMap<>();
    private Connection conn;
    private MinecraftServer minecraftServer;
    private MemberExtension memberExtension;

    public PermsExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "perms", "Permission levels for other commands like /back, /c or /modTP. Enables /kPerms command."));
    }

    @Override
    public void onExtensionsReady(ExtensionManager em) {
        this.memberExtension = (MemberExtension) em.getExtensions().get("member");
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    @Override
    public void onServerRun(MinecraftServer minecraftServer) {
        this.minecraftServer = minecraftServer;
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        new PermsCommand().register(dispatcher, this);
    }

    @Override
    public void onCreateDatabase(Connection conn) {
        this.conn = conn;
        try {
            String createBackDatabase = "CREATE TABLE IF NOT EXISTS `perms` (" +
                    "`uuid` VARCHAR(50) PRIMARY KEY NOT NULL," +
                    "`level` NUMERIC DEFAULT 1," +
                    "FOREIGN KEY(uuid) REFERENCES player(uuid));";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(createBackDatabase);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPlayerJoined(ServerPlayer player) {
        String playerUUID = player.getStringUUID();
        try {
            String query = "INSERT OR IGNORE INTO perms(uuid, level) VALUES (?, ?);";
            PreparedStatement ps = this.conn.prepareStatement(query);
            ps.setString(1, playerUUID);
            ps.setInt(2, PermsLevels.TEST_MEMBER.getId());
            ps.executeUpdate();
            ps.close();

            if (this.playerPerms.containsKey(playerUUID)) {
                playerPerms.remove(playerUUID);
            }
            playerPerms.put(playerUUID, getDBPlayerPerms(playerUUID));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if (!memberExtension.extensionSettings().isEnabled() || !this.extensionSettings().isEnabled()) {
            return;
        }
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return;
        }
        PlayerTeam actual = server.getScoreboard().getPlayersTeam(player.getName().getString());
        PlayerTeam shouldTeam = server.getScoreboard().getPlayerTeam(playerPerms.get(playerUUID).getName());
        if (actual == null || shouldTeam == null) {
            return;
        }
        if (!actual.isAlliedTo(shouldTeam)) {
            server.getScoreboard().addPlayerToTeam(player.getName().getString(), shouldTeam);
        }
    }

    @Override
    public void onExtensionDisabled(CommandSourceStack source) {
        if (this.minecraftServer != null) {
            for (ServerPlayer player : this.minecraftServer.getPlayerList().getPlayers()) {
                this.onPlayerLeft(player);
            }
        }
        Extensions.super.onExtensionDisabled(source);
    }

    @Override
    public void onExtensionEnabled(CommandSourceStack source) {
        if (this.minecraftServer != null) {
            for (ServerPlayer player : this.minecraftServer.getPlayerList().getPlayers()) {
                this.onPlayerJoined(player);
            }
        }
        Extensions.super.onExtensionEnabled(source);
    }

    @Override
    public void onPlayerLeft(ServerPlayer player) {
        String playerUUID = player.getStringUUID();
        if (this.playerPerms.containsKey(playerUUID)) {
            playerPerms.remove(playerUUID);
        }
    }

    // Sin jugador (consola o el source de compilación que usa 26.x para marcar comandos restringidos) no hay nivel.
    public boolean hasLevel(CommandSourceStack source, PermsLevels level) {
        ServerPlayer player = source.getPlayer();
        return player != null && getDBPlayerPerms(player.getStringUUID()).getId() >= level.getId();
    }

    public PermsLevels getDBPlayerPerms(String playerUUID) {
        try {
            String query = "SELECT level FROM perms WHERE uuid = ?;";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, playerUUID);
            ResultSet rs = ps.executeQuery();
            int level = -1;
            if (rs.next()) {
                level = rs.getInt("level");
            }
            rs.close();
            ps.close();
            if (level != -1) {
                return PermsLevels.getValue(level);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return PermsLevels.TEST_MEMBER;
    }

    private String getPlayerUUID(String name) {
        String playerUUID = null;
        try {
            String query = "SELECT uuid FROM player WHERE name = ?;";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                playerUUID = rs.getString("uuid");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerUUID;
    }

    public int updatePerms(CommandSourceStack source, String player, String value) {
        String playerUUID = getPlayerUUID(player);
        if (playerUUID == null) {
            return 1;
        }
        int level = PermsLevels.getLevel(value);
        if (level == -1) {
            source.sendSuccess(MarkEnum.CROSS.appendMessage("Not a valid level!"), false);
            return 1;
        }
        try {
            String query = "INSERT INTO perms (uuid, level) VALUES (?, ?)" +
                    "ON CONFLICT (uuid) DO UPDATE SET level = ?;";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, playerUUID);
            ps.setInt(2, level);
            ps.setInt(3, level);
            ps.executeUpdate();
            ps.close();
            source.sendSuccess(MarkEnum.TICK.appendMessage(String.format("Player %s > %s", player, value)), false);
            if (this.playerPerms.containsKey(playerUUID)) {
                playerPerms.remove(playerUUID);
            }
            playerPerms.put(playerUUID, getDBPlayerPerms(playerUUID));
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        }
        PlayerUtils.reloadCommands();

        Collection<String> teamNames = source.getServer().getScoreboard().getTeamNames();
        if (teamNames.contains(PermsLevels.getValue(level).getName())) {
            source.getServer().getScoreboard().addPlayerToTeam(player, source.getServer().getScoreboard().getPlayerTeam(PermsLevels.getValue(level).getName()));
        }

        return 1;
    }

    public Collection<String> getPlayers() {
        Set<String> players = Sets.newLinkedHashSet();
        try {
            String query = "SELECT name FROM player;";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                players.add(rs.getString("name"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return players;
    }
}
