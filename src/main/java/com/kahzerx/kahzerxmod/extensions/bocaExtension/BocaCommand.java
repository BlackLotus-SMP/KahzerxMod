package com.kahzerx.kahzerxmod.extensions.bocaExtension;

import com.mojang.brigadier.CommandDispatcher;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

public class BocaCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, BocaExtension boca) {
        dispatcher.register(literal("boca").
                requires(server -> boca.extensionSettings().isEnabled()).
                then(literal("annoying").
                        executes(context -> {
                            List<ServerPlayer> players = context.getSource().getServer().getPlayerList().getPlayers();
                            for (ServerPlayer player : players) {
                                player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§9❤§r §l§9BO§eQUI§9TA§r §9❤§r")));
                            }
                            return 1;
                        })).
                executes(context -> {
                    context.getSource().getServer().getPlayerList().broadcastSystemMessage(Component.literal(
                            "§9❤§r §l§9BO§eQUI§9TA§r §9❤§r"
                    ), false);
                    return 1;
                }));
    }
}
