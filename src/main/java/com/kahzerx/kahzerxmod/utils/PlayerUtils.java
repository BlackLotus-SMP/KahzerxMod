package com.kahzerx.kahzerxmod.utils;

import com.google.common.collect.Sets;
import com.kahzerx.kahzerxmod.KahzerxServer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import java.util.Collection;
import java.util.Set;

public class PlayerUtils {
    public static String getPlayerWithColor(final ServerPlayer player) {
        return ChatFormatting.YELLOW + player.getName().getString();
    }

    public static Collection<String> getPlayers(CommandSourceStack source) {
        Set<String> players = Sets.newLinkedHashSet();
        players.addAll(source.getOnlinePlayerNames());
        return players;
    }

    public static void reloadCommands() {
        MinecraftServer server = KahzerxServer.minecraftServer;
        server.schedule(new TickTask(server.getTickCount(), () -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                server.getCommands().sendCommands(player);
            }
        }));
    }

    // Sustituye a ServerPlayer#teleportToWithTicket (eliminado): carga el chunk destino de forma síncrona y teletransporta.
    public static void teleportToWithTicket(ServerPlayer player, double x, double y, double z) {
        ChunkPos chunkPos = ChunkPos.containing(BlockPos.containing(x, y, z));
        player.level().getChunk(chunkPos.x(), chunkPos.z());
        player.teleportTo(x, y, z);
    }
}
