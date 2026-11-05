package com.kahzerx.kahzerxmod.extensions.permsExtension;

import com.kahzerx.kahzerxmod.utils.PermUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.commands.SharedSuggestionProvider.suggest;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class PermsCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, PermsExtension perms) {
        dispatcher.register(literal("kPerms").
                requires(serverCommandSource -> PermUtils.hasPermission(serverCommandSource, 2) && perms.extensionSettings().isEnabled()).
                then(literal("give").
                        then(argument("player", word()).
                                suggests((c, b) -> suggest(perms.getPlayers(), b)).
                                then(argument("level", word()).
                                        suggests((c, b) -> suggest(PermsLevels.permNames(), b)).
                                        executes(context -> perms.updatePerms(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "player"),
                                                StringArgumentType.getString(context, "level")))))));
    }
}
