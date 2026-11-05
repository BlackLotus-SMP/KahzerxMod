package com.kahzerx.kahzerxmod.extensions.prankExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import java.sql.*;
import java.util.HashMap;

public class PrankExtension extends GenericExtension implements Extensions {
    public static boolean isExtensionEnabled = false;
    public static final HashMap<String, PrankLevel> playerLevel = new HashMap<>();
    private Connection conn;
    private MinecraftServer server;

    public PrankExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "pranks", "Sets a prank level on your name."));
    }

    @Override
    public void onCreateDatabase(Connection conn) {
        this.conn = conn;
        try {
            String createBackDatabase = "CREATE TABLE IF NOT EXISTS `prank` (" +
                    "`uuid` VARCHAR(50) PRIMARY KEY NOT NULL," +
                    "`id` NUMERIC DEFAULT NULL," +
                    "FOREIGN KEY(uuid) REFERENCES player(uuid));";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(createBackDatabase);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServerRun(MinecraftServer minecraftServer) {
        this.server = minecraftServer;
        isExtensionEnabled = this.getSettings().isEnabled();
    }

    @Override
    public void onExtensionDisabled(CommandSourceStack source) {
        Extensions.super.onExtensionDisabled(source);
        playerLevel.clear();
        isExtensionEnabled = false;
        updatePlayerList();
    }

    @Override
    public void onExtensionEnabled(CommandSourceStack source) {
        Extensions.super.onExtensionEnabled(source);
        for (ServerPlayer player : this.server.getPlayerList().getPlayers()) {
            this.onPlayerJoined(player);
        }
        isExtensionEnabled = true;
        updatePlayerList();
    }

    @Override
    public void onPlayerJoined(ServerPlayer player) {
        String playerUUID = player.getStringUUID();
        playerLevel.remove(playerUUID);
        playerLevel.put(playerUUID, getPlayerLevel(player));
    }

    @Override
    public void onPlayerLeft(ServerPlayer player) {
        String playerUUID = player.getStringUUID();
        playerLevel.remove(playerUUID);
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        new PrankCommand().register(dispatcher, this);
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    private PrankLevel getPlayerLevel(ServerPlayer player) {
        try {
            String query = "SELECT id FROM prank WHERE uuid = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, player.getStringUUID());
            ResultSet rs = ps.executeQuery();
            int level = rs.getInt("id");
            rs.close();
            ps.close();
            return PrankLevel.idToLevel(level);
        } catch (SQLException s) {
            return PrankLevel.idToLevel(0);
        }
    }

    public void updateLevel(ServerPlayer player, PrankLevel level) {
        try {
            String q = "INSERT INTO `prank` (uuid, id)" +
                    "VALUES (?, ?)" +
                    "ON CONFLICT (uuid)" +
                    "DO UPDATE SET id = ?;";
            PreparedStatement ps = conn.prepareStatement(q);
            ps.setString(1, player.getStringUUID());
            ps.setInt(2, level.getID());
            ps.setInt(3, level.getID());
            ps.executeUpdate();
            ps.close();
            this.onPlayerJoined(player);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        updatePlayerList();
    }

    private void updatePlayerList() {
        // TODO wut
//        Collection<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player));
        }
//        server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, players));
    }
}
