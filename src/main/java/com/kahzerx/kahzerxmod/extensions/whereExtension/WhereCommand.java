package com.kahzerx.kahzerxmod.extensions.whereExtension;

import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsLevels;
import com.kahzerx.kahzerxmod.utils.DimUtils;
import com.kahzerx.kahzerxmod.utils.PlayerUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.SharedSuggestionProvider.suggest;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class WhereCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, WhereExtension where) {
        dispatcher.register(literal("where").
                requires(server -> {
                    if (where.extensionSettings().isEnabled() && where.getPermsExtension().extensionSettings().isEnabled()) {
                        return where.getPermsExtension().hasLevel(server, PermsLevels.MEMBER);
                    }
                    return false;
                }).
                then(argument("player", StringArgumentType.word()).
                        suggests((c, b) -> suggest(PlayerUtils.getPlayers(c.getSource()), b)).
                        executes(context -> {
                            ServerPlayer playerEntity = context.getSource().getServer().getPlayerList().getPlayerByName(StringArgumentType.getString(context, "player"));
                            if (playerEntity != null) {
                                context.getSource().sendSuccess(() -> Component.literal(String.format(
                                        "%s %s %s",
                                        PlayerUtils.getPlayerWithColor(playerEntity),
                                        DimUtils.getDimensionWithColor(playerEntity.level()),
                                        DimUtils.formatCoords(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ())
                                )), false);
                            }
                            return 1;
                        })));
    }
}
