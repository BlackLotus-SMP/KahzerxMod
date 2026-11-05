package com.kahzerx.kahzerxmod.extensions.homeExtension;

import java.util.Set;
import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.utils.DimUtils;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import java.sql.*;
import java.util.HashMap;

public class HomeExtension extends GenericExtension implements Extensions {
    private final HashMap<String, HomePos> playerHomes = new HashMap<>();
    private Connection conn;
    private MinecraftServer server;

    public HomeExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "home", "/home and /setHome commands."));
    }

    @Override
    public void onCreateDatabase(Connection conn) {
        this.conn = conn;
        try {
            String createHomeDatabase = "CREATE TABLE IF NOT EXISTS `home` (" +
                    "`uuid` VARCHAR(50) PRIMARY KEY NOT NULL," +
                    "`homeX` NUMERIC DEFAULT NULL," +
                    "`homeY` NUMERIC DEFAULT NULL," +
                    "`homeZ` NUMERIC DEFAULT NULL," +
                    "`homeDim` NUMERIC DEFAULT NULL," +
                    "FOREIGN KEY(uuid) REFERENCES player(uuid));";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(createHomeDatabase);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServerRun(MinecraftServer minecraftServer) {
        this.server = minecraftServer;
    }

    @Override
    public void onPlayerJoined(ServerPlayer player) {
        String playerUUID = player.getStringUUID();
        playerHomes.remove(playerUUID);
        playerHomes.put(playerUUID, getHomePos(player));
    }

    @Override
    public void onPlayerLeft(ServerPlayer player) {
        String playerUUID = player.getStringUUID();
        playerHomes.remove(playerUUID);
    }

    @Override
    public void onExtensionDisabled(CommandSourceStack source) {
        Extensions.super.onExtensionDisabled(source);
        playerHomes.clear();
    }

    @Override
    public void onExtensionEnabled(CommandSourceStack source) {
        Extensions.super.onExtensionEnabled(source);
        for (ServerPlayer player : this.server.getPlayerList().getPlayers()) {
            this.onPlayerJoined(player);
        }
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        new HomeCommand().register(dispatcher, this);
        new SetHomeCommand().register(dispatcher, this);
    }

    private void updateHomePos(ServerPlayer player, HomePos pos) {
        try {
            String q = "INSERT INTO `home` (uuid, homeX, homeY, homeZ, homeDim)" +
                    "VALUES (?, ?, ?, ?, ?)" +
                    "ON CONFLICT (uuid)" +
                    "DO UPDATE SET homeX = ?, homeY = ?, homeZ = ?, homeDim = ?;";
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

    private HomePos getHomePos(ServerPlayer player) {
        try {
            String query = "SELECT homeX, homeY, homeZ, homeDim FROM home WHERE uuid = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, player.getStringUUID());
            ResultSet rs = ps.executeQuery();
            double x = rs.getDouble("homeX");
            double y = rs.getDouble("homeY");
            double z = rs.getDouble("homeZ");
            int dim = rs.getInt("homeDim");
            rs.close();
            ps.close();
            return new HomePos(x, y, z, DimUtils.getWorldString(dim));
        } catch (SQLException s) {
            return new HomePos(0, 0, 0, "");
        }
    }

    public int tpHome(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer player = src.getPlayer();
        if (player == null) {
            return 1;
        }
        String playerUUID = player.getStringUUID();
        HomePos homePos = playerHomes.get(playerUUID);
        if (homePos.isValid()) {
            player.teleportTo(
                    DimUtils.getWorld(homePos.getDim(), player),
                    homePos.getX(),
                    homePos.getY(),
                    homePos.getZ(),
                    Set.of(),
                    player.getYRot(),
                    player.getXRot(),
                    true
            );
            player.giveExperiencePoints(0);  // xp resets when you tp from other dimension and needs to update smh, mojang pls.
        } else {
            player.sendSystemMessage(MarkEnum.INFO.appendText(Component.literal("You don't have a home yet, use ").withStyle(style -> style.withColor(ChatFormatting.WHITE)).append(getClickableSetHomeCommand())));
        }
        return 1;
    }

    public int saveHome(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer player = src.getPlayer();
        if (player == null) {
            return 1;
        }
        HomePos newHomePos = new HomePos(player.getX(), player.getY(), player.getZ(), DimUtils.getDim(player.level()));
        String playerUUID = player.getStringUUID();
        playerHomes.put(playerUUID, newHomePos);
        src.sendSuccess(() -> Component.literal(String.format(
                "Home @ %s %s",
                DimUtils.getDimensionWithColor(player.level()),
                DimUtils.formatCoords(player.getX(), player.getY(), player.getZ())
            )),
            false
        );
        updateHomePos(player, newHomePos);
        return 1;
    }

    public static MutableComponent getClickableSetHomeCommand() {
        return Component.literal("/setHome").withStyle((style -> style.withColor(ChatFormatting.DARK_GREEN).
                withClickEvent(new ClickEvent.RunCommand("/setHome")).
                withHoverEvent(new HoverEvent.ShowText(Component.literal("setHome")))));
    }
}
