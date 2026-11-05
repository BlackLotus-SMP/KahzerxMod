package com.kahzerx.kahzerxmod.extensions.blockInfoExtension.database;

import com.kahzerx.kahzerxmod.extensions.blockInfoExtension.helpers.BlockActionLog;
import com.kahzerx.kahzerxmod.extensions.blockInfoExtension.utils.BlockInfoUtils;
import com.kahzerx.kahzerxmod.utils.DimUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public record BlockInfoQuery(Connection connection) {
    public void onCreateDatabase() {
        try {
            String createBlockInfoTable = "CREATE TABLE IF NOT EXISTS `action_logger` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "`playerName` TEXT(20) NOT NULL," +
                    "`amount` NUMERIC NOT NULL," +
                    "`block` TEXT(30) NOT NULL," +
                    "`posX` NUMERIC NOT NULL," +
                    "`posY` NUMERIC NOT NULL," +
                    "`posZ` NUMERIC NOT NULL," +
                    "`dim` NUMERIC NOT NULL," +
                    "`action` NUMERIC(3) NOT NULL," +
                    "`date` TEXT NOT NULL);";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(createBlockInfoTable);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logAction(BlockActionLog action) {
        String q = "INSERT INTO `action_logger` (playerName, amount, block, posX, posY, posZ, dim, action, date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try {
            PreparedStatement ps = connection.prepareStatement(q);
            ps.setString(1, action.getPlayer());
            ps.setInt(2, action.getAmount());
            ps.setString(3, action.getBlock().toLowerCase());
            ps.setInt(4, action.getX());
            ps.setInt(5, action.getY());
            ps.setInt(6, action.getZ());
            ps.setInt(7, action.getDim());
            ps.setInt(8, action.getActionType());
            ps.setString(9, action.getDate());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void clearLogTable() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS rows FROM `action_logger`;");
            if (rs.next()) {
                if (rs.getInt("rows") > Math.pow(10, 8)) {
                    String deleteRows = "DELETE FROM `action_logger` WHERE id IN (SELECT id FROM `action_logger` LIMIT POWER(10, 7));";
                    stmt.executeUpdate(deleteRows);
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public List<MutableComponent> getBlockInfo(int x, int y, int z, int dim, int page) {
        List<MutableComponent> msg = new ArrayList<>();
        try {
            String q = "SELECT amount, action, date, playerName, block FROM `action_logger` " +
                    "WHERE posX = ? AND posY = ? AND posZ = ? AND dim = ? ORDER BY id DESC LIMIT 10 OFFSET ?;";
            PreparedStatement ps = this.connection.prepareStatement(q);
            ps.setInt(1, x);
            ps.setInt(2, y);
            ps.setInt(3, z);
            ps.setInt(4, dim);
            ps.setInt(5, (page - 1) * 10);
            ResultSet rs = ps.executeQuery();
            int i = 0;
            while (rs.next() && i <= 10) {
                MutableComponent line = BlockInfoUtils.buildLine(rs);
                msg.add(line);
                i++;
            }
            if (msg.isEmpty()) {
                msg.add(Component.literal("No logs :("));
            }
            rs.close();
            ps.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            msg.clear();
            msg.add(Component.literal("Error."));
        }
        return msg;
    }

    public int getLines(int x, int y, int z, int dim) {
        int lines = 0;
        try {
            String q = "SELECT (COUNT(id) / 10) + 1 AS line FROM `action_logger` " +
                    "WHERE posX = ? AND posY = ? AND posZ = ? AND dim = ?;";
            PreparedStatement ps = this.connection.prepareStatement(q);
            ps.setInt(1, x);
            ps.setInt(2, y);
            ps.setInt(3, z);
            ps.setInt(4, dim);
            ResultSet rs = ps.executeQuery();
            lines = rs.getInt("line");
            rs.close();
            ps.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return lines;
    }

    public void getInfo(CommandSourceStack source, BlockPos pos, int page) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        List<MutableComponent> msg = getBlockInfo(x, y, z, DimUtils.getWorldID(DimUtils.getDim(source.getLevel())), page);
        Collections.reverse(msg);
        source.sendSuccess(() -> Component.literal("======BlockInfo======"), false);
        int nLine = getLines(x, y, z, DimUtils.getWorldID(DimUtils.getDim(source.getLevel())));

        for (MutableComponent line : msg) {
            source.sendSuccess(() -> line, false);
        }

        if (page > nLine) {  // No pages.
            return;
        } else if (page == nLine && page == 1) {  // There is only 1 page.
            source.sendSuccess(() -> Component.literal(String.format("%d/%d.", page, nLine)), false);
        } else if (page == 1) {  // First page but there are more
            MutableComponent pages = BlockInfoUtils.getPages(page, nLine);
            MutableComponent next = BlockInfoUtils.getNext(x, y, z, page);
            source.sendSuccess(() -> Component.literal("").append(pages).append(next).append(BlockInfoUtils.getHelp(x, y, z)), false);
        } else if (page == nLine) {  // The last page.
            MutableComponent prev = BlockInfoUtils.getPrev(x, y, z, page);
            MutableComponent pages = BlockInfoUtils.getPages(page, nLine);
            source.sendSuccess(() -> Component.literal("").append(prev).append(pages).append(BlockInfoUtils.getHelp(x, y, z)), false);
        } else {  // Have pages before and after the one you are in.
            MutableComponent prev = BlockInfoUtils.getPrev(x, y, z, page);
            MutableComponent pages = BlockInfoUtils.getPages(page, nLine);
            MutableComponent next = BlockInfoUtils.getNext(x, y, z, page);
            source.sendSuccess(() -> Component.literal("").append(prev).append(pages).append(next).append(BlockInfoUtils.getHelp(x, y, z)), false);
        }
    }

    public int threadedGetInfo(CommandSourceStack source, BlockPos pos, int page) {
        new Thread(() -> getInfo(source, pos, page)).start();
        return 1;
    }
}
