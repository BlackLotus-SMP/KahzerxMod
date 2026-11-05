package com.kahzerx.kahzerxmod.extensions.blockInfoExtension.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BlockInfoUtils {
    public static boolean shouldRegisterBlock(Block block, ServerPlayer player) {
        return !player.isCrouching() && (block instanceof BaseEntityBlock
                || block instanceof DoorBlock || block instanceof FenceGateBlock
                || block instanceof TrapDoorBlock || block instanceof FaceAttachedHorizontalDirectionalBlock || block instanceof NoteBlock);
    }

    public static boolean shouldRegisterItem(ServerPlayer player, ItemStack itemStack) {
        return !player.isCrouching() && (itemStack.getItem() == Items.LAVA_BUCKET
                || itemStack.getItem() == Items.WATER_BUCKET);
    }

    public static MutableComponent buildLine(final ResultSet rs) throws SQLException {
        LocalDateTime date = LocalDateTime.parse(rs.getString("date"), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        String playerName = rs.getString("playerName");
        String block = rs.getString("block");
        int amount = rs.getInt("amount");
        block = block.split("\\.")[block.split("\\.").length - 1];
        MutableComponent fDate = getDateWithHover(date);
        return switch (rs.getInt("action")) {
            case 0 -> fDate.append(String.format(" %s %s %s.",
                    ChatFormatting.WHITE + playerName,
                    ChatFormatting.RED + "broke",
                    ChatFormatting.YELLOW + block + ChatFormatting.WHITE));
            case 1 -> fDate.append(String.format(" %s %s %s.",
                    ChatFormatting.WHITE + playerName,
                    ChatFormatting.GREEN + "placed",
                    ChatFormatting.YELLOW + block + ChatFormatting.WHITE));
            case 2 -> fDate.append(String.format(" %s %s %s.",
                    ChatFormatting.WHITE + playerName,
                    ChatFormatting.DARK_AQUA + "used",
                    ChatFormatting.YELLOW + block + ChatFormatting.WHITE));
            case 3 -> fDate.append(String.format(" %s %s %s %s.",
                    ChatFormatting.WHITE + playerName,
                    ChatFormatting.AQUA + "added",
                    ChatFormatting.YELLOW + String.valueOf(amount),
                    block + ChatFormatting.WHITE));
            default -> fDate.append(String.format(" %s %s %s %s.",
                    ChatFormatting.WHITE + playerName,
                    ChatFormatting.DARK_RED + "removed",
                    ChatFormatting.YELLOW + String.valueOf(amount),
                    block + ChatFormatting.WHITE));
        };
    }

    private static MutableComponent getDateWithHover(LocalDateTime date) {
        return Component.literal("[" + date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + "]").withStyle(style -> style.
                withColor(ChatFormatting.GRAY).
                withHoverEvent(new HoverEvent.ShowText(Component.literal(date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))))));
    }

    public static MutableComponent getPages(int page, int nLine) {
        return Component.literal(String.format("%d/%d", page, nLine)).withStyle((style -> style.withColor(ChatFormatting.WHITE)));
    }

    public static MutableComponent getNext(int x, int y, int z, int page) {
        return Component.literal(" >>>").withStyle(style -> style.withColor(ChatFormatting.GOLD).
                withClickEvent(new ClickEvent.RunCommand(String.format("/blockInfo %d %d %d %d", x, y, z, page + 1))).
                withHoverEvent(new HoverEvent.ShowText(Component.literal("Next"))));
    }

    public static MutableComponent getPrev(int x, int y, int z, int page) {
        return Component.literal("<<< ").withStyle(style -> style.withColor(ChatFormatting.GOLD).
                withClickEvent(new ClickEvent.RunCommand(String.format("/blockInfo %d %d %d %d", x, y, z, page - 1))).
                withHoverEvent(new HoverEvent.ShowText(Component.literal("Prev"))));
    }

    public static MutableComponent getHelp(int x, int y, int z) {
        return Component.literal(
                String.format(". Page with /blockInfo %d %d %d <page>.", x, y, z))
                .withStyle(style -> style.withColor(ChatFormatting.WHITE));
    }
}
