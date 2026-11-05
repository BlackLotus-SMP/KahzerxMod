package com.kahzerx.kahzerxmod.extensions.totopoExtension;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

public class TotopoCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, TotopoExtension totopo) {
        dispatcher.register(literal("totopo").
                requires(server -> totopo.extensionSettings().isEnabled()).
                executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    double x = player.getX();
                    double y = player.getY();
                    double z = player.getZ();
                    totopo.totopoShape(player, context.getSource().getLevel(), x, y, z);
                    return 1;
                }));
    }
}
