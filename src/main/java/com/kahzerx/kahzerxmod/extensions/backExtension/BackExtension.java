package com.kahzerx.kahzerxmod.extensions.backExtension;

import java.util.Set;
import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsExtension;
import com.kahzerx.kahzerxmod.utils.DimUtils;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.sql.*;
import java.util.HashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class BackExtension extends GenericExtension implements Extensions {
    private final HashMap<String, BackPos> playerBack = new HashMap<>();
    private Connection conn;
    public PermsExtension permsExtension;

    public BackExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "back", "/back command to tp to the last death position."));
    }

    @Override
    public void onExtensionsReady(ExtensionManager em) {
        this.permsExtension = (PermsExtension) em.getExtensions().get("perms");
    }

    @Override
    public void onCreateDatabase(Connection conn) {
        this.conn = conn;
        try {
            String createBackDatabase = "CREATE TABLE IF NOT EXISTS `back` (" +
                    "`uuid` VARCHAR(50) PRIMARY KEY NOT NULL," +
                    "`deathX` NUMERIC DEFAULT NULL," +
                    "`deathY` NUMERIC DEFAULT NULL," +
                    "`deathZ` NUMERIC DEFAULT NULL," +
                    "`deathDim` NUMERIC DEFAULT NULL," +
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
        if (this.playerBack.containsKey(playerUUID)) {
            playerBack.remove(playerUUID);
        }
        playerBack.put(playerUUID, getDeathPos(player));
    }

    @Override
    public void onPlayerLeft(ServerPlayer player) {
        String playerUUID = player.getStringUUID();
        if (this.playerBack.containsKey(playerUUID)) {
            playerBack.remove(playerUUID);
        }
    }

    @Override
    public void onPlayerDied(ServerPlayer player) {
        String playerUUID = player.getStringUUID();
        BackPos backPos = new BackPos(player.getX(), player.getY(), player.getZ(), DimUtils.getDim(player.level()));
        playerBack.put(playerUUID, backPos);
        updateDeathPos(player, backPos);
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        new BackCommand().register(dispatcher, this);
    }

    private void updateDeathPos(ServerPlayer player, BackPos pos) {
        try {
            String q = "INSERT INTO `back` (uuid, deathX, deathY, deathZ, deathDim)" +
                    "VALUES (?, ?, ?, ?, ?)" +
                    "ON CONFLICT (uuid)" +
                    "DO UPDATE SET deathX = ?, deathY = ?, deathZ = ?, deathDim = ?;";
            PreparedStatement ps = conn.prepareStatement(q);
            ps.setString(1, player.getStringUUID());
            ps.setDouble(2, pos.getX());
            ps.setDouble(3, pos.getY());
            ps.setDouble(4, pos.getZ());
            ps.setInt(5, DimUtils.getWorldID(pos.dim()));
            ps.setDouble(6, pos.getX());
            ps.setDouble(7, pos.getY());
            ps.setDouble(8, pos.getZ());
            ps.setInt(9, DimUtils.getWorldID(pos.dim()));
            ps.executeUpdate();
            ps.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private BackPos getDeathPos(ServerPlayer player) {
        try {
            String query = "SELECT deathX, deathY, deathZ, deathDim FROM back WHERE uuid = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, player.getStringUUID());
            ResultSet rs = ps.executeQuery();
            double x = rs.getDouble("deathX");
            double y = rs.getDouble("deathY");
            double z = rs.getDouble("deathZ");
            int dim = rs.getInt("deathDim");
            rs.close();
            ps.close();
            return new BackPos(x, y, z, DimUtils.getWorldString(dim));
        } catch (SQLException s) {
            return new BackPos(0, 0, 0, "");
        }
    }

    public int tpBack(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer player = src.getPlayer();
        if (player == null) {
            return 1;
        }
        String playerUUID = player.getStringUUID();
        if (!this.playerBack.containsKey(playerUUID)) {
            player.sendSystemMessage(MarkEnum.CROSS.appendMsg("Error"));
            return 1;
        }
        BackPos backPos = playerBack.get(playerUUID);
        if (backPos.isValid()) {
            player.teleportTo(
                    DimUtils.getWorld(backPos.getDim(), player),
                    backPos.getX(),
                    backPos.getY(),
                    backPos.getZ(),
                    Set.of(),
                    player.getYRot(),
                    player.getXRot(),
                    true
            );
            player.giveExperiencePoints(0);  // xp resets when you tp from other dimension and needs to update smh, mojang pls.
        } else {
            player.sendSystemMessage(MarkEnum.INFO.appendMsg("You haven't died yet :("));
        }
        return 1;
    }
}
