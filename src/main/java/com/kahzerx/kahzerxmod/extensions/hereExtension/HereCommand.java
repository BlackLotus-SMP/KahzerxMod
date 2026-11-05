package com.kahzerx.kahzerxmod.extensions.hereExtension;

import com.kahzerx.kahzerxmod.utils.PlayerUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.SharedSuggestionProvider.suggest;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class HereCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, HereExtension here) {
        dispatcher.register(literal("here").
                requires(server -> here.extensionSettings().isEnabled()).
                then(literal("to").
                        then(argument("player", StringArgumentType.word()).
                                suggests((c, b) -> suggest(PlayerUtils.getPlayers(c.getSource()), b)).
                                executes(context -> {
                                    ServerPlayer playerEntity = context.getSource().getServer().getPlayerList().getPlayerByName(StringArgumentType.getString(context, "player"));
                                    if (playerEntity != null) {
                                        return here.sendLocation(context.getSource(), playerEntity);
                                    }
                                    return 1;
                                }))).
                executes(context -> here.sendLocation(context.getSource(), null)));
    }
}
