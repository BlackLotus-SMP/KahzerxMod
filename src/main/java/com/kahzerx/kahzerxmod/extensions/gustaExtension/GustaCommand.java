package com.kahzerx.kahzerxmod.extensions.gustaExtension;

import com.kahzerx.kahzerxmod.extensions.bocaExtension.BocaExtension;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

public class GustaCommand {
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, GustaExtension gusta) {
        dispatcher.register(literal("gusta").
                requires(server -> gusta.extensionSettings().isEnabled()).
                then(literal("annoying").
                        executes(context -> {
                            List<ServerPlayerEntity> players = context.getSource().getServer().getPlayerManager().getPlayerList();
                            for (ServerPlayerEntity player : players) {
                                player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("§4♦§r §l§6Gustaaaaaaaaaaaaa§r §4♦§r")));
                            }
                            return 1;
                        })).
                executes(context -> {
                    context.getSource().getServer().getPlayerManager().broadcast(Text.literal(
                            "§4♦§r §l§6Gustaaaaaaaaaaaaa§r §4♦§r"
                    ), false);
                    return 1;
                }));
    }
}
