package com.kahzerx.kahzerxmod.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import java.util.Objects;

public class DimUtils {
    public static ServerLevel getWorld(String dim, ServerPlayer player) {
        return switch (dim) {
            case "Overworld" -> Objects.requireNonNull(player.level().getServer()).getLevel(Level.OVERWORLD);
            case "Nether" -> Objects.requireNonNull(player.level().getServer()).getLevel(Level.NETHER);
            default -> Objects.requireNonNull(player.level().getServer()).getLevel(Level.END);
        };
    }

    public static ServerLevel getWorld(int dimID, MinecraftServer server) {
        return switch (dimID) {
            case 0 -> server.getLevel(Level.OVERWORLD);
            case 1 -> server.getLevel(Level.NETHER);
            default -> server.getLevel(Level.END);
        };
    }

    public static int getWorldID(String dim) {
        return switch (dim) {
            case "Overworld" -> 0;
            case "Nether" -> 1;
            default -> 2;
        };
    }

    public static String getWorldString(int dimID) {
        return switch (dimID) {
            case 0 -> "Overworld";
            case 1 -> "Nether";
            default -> "End";
        };
    }

    public static String getDim(Level world) {
        Identifier dimensionType = world.dimension().identifier();
        String msg = world.dimensionType().toString();
        if (dimensionType.equals(Level.OVERWORLD.identifier())) {
            msg = "Overworld";
        } else if (dimensionType.equals(Level.NETHER.identifier())) {
            msg = "Nether";
        } else if (dimensionType.equals(Level.END.identifier())) {
            msg = "End";
        }
        return msg;
    }

    public static String getDimensionWithColor(Level world) {
        Identifier dimensionType = world.dimension().identifier();
        String msg = world.dimensionType().toString();
        return getDim(dimensionType, msg);
    }

    public static String getDimensionWithColor(final Identifier dimensionType) {
        String msg = dimensionType.toString();
        return getDim(dimensionType, msg);
    }

    private static String getDim(Identifier dimensionType, String msg) {
        if (dimensionType.equals(Level.OVERWORLD.identifier())) {
            msg = ChatFormatting.GREEN + "[Overworld]";
        } else if (dimensionType.equals(Level.NETHER.identifier())) {
            msg = ChatFormatting.RED + "[Nether]";
        } else if (dimensionType.equals(Level.END.identifier())) {
            msg = ChatFormatting.DARK_PURPLE + "[End]";
        }
        return msg;
    }

    public static String formatCoords(double x, double y, double z) {
        return ChatFormatting.WHITE + String.format("[x: %d, y: %d, z: %d]", (int) x, (int) y, (int) z);
    }
}
