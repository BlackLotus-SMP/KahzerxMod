package com.kahzerx.kahzerxmod.extensions.blockInfoExtension.database;

import com.kahzerx.kahzerxmod.extensions.blockInfoExtension.helpers.BlockActionLog;
import com.kahzerx.kahzerxmod.extensions.blockInfoExtension.utils.BlockInfoUtils;
import com.kahzerx.kahzerxmod.utils.DimUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public List<MutableText> getBlockInfo(int x, int y, int z, int dim, int page) {
        List<MutableText> msg = new ArrayList<>();
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
                MutableText line = BlockInfoUtils.buildLine(rs);
                msg.add(line);
                i++;
            }
            if (msg.isEmpty()) {
                msg.add(Text.literal("No logs :("));
            }
            rs.close();
            ps.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            msg.clear();
            msg.add(Text.literal("Error."));
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

    public void getInfo(ServerCommandSource source, BlockPos pos, int page) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        List<MutableText> msg = getBlockInfo(x, y, z, DimUtils.getWorldID(DimUtils.getDim(source.getWorld())), page);
        Collections.reverse(msg);
        source.sendFeedback(() -> Text.literal("======BlockInfo======"), false);
        int nLine = getLines(x, y, z, DimUtils.getWorldID(DimUtils.getDim(source.getWorld())));

        for (MutableText line : msg) {
            source.sendFeedback(() -> line, false);
        }

        if (page > nLine) {  // No pages.
            return;
        } else if (page == nLine && page == 1) {  // There is only 1 page.
            source.sendFeedback(() -> Text.literal(String.format("%d/%d.", page, nLine)), false);
        } else if (page == 1) {  // First page but there are more
            MutableText pages = BlockInfoUtils.getPages(page, nLine);
            MutableText next = BlockInfoUtils.getNext(x, y, z, page);
            source.sendFeedback(() -> Text.literal("").append(pages).append(next).append(BlockInfoUtils.getHelp(x, y, z)), false);
        } else if (page == nLine) {  // The last page.
            MutableText prev = BlockInfoUtils.getPrev(x, y, z, page);
            MutableText pages = BlockInfoUtils.getPages(page, nLine);
            source.sendFeedback(() -> Text.literal("").append(prev).append(pages).append(BlockInfoUtils.getHelp(x, y, z)), false);
        } else {  // Have pages before and after the one you are in.
            MutableText prev = BlockInfoUtils.getPrev(x, y, z, page);
            MutableText pages = BlockInfoUtils.getPages(page, nLine);
            MutableText next = BlockInfoUtils.getNext(x, y, z, page);
            source.sendFeedback(() -> Text.literal("").append(prev).append(pages).append(next).append(BlockInfoUtils.getHelp(x, y, z)), false);
        }
    }

    public int threadedGetInfo(ServerCommandSource source, BlockPos pos, int page) {
        new Thread(() -> getInfo(source, pos, page)).start();
        return 1;
    }
}
