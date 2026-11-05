package com.kahzerx.kahzerxmod.extensions.spoofExtension;

import com.kahzerx.kahzerxmod.utils.PermUtils;
import com.kahzerx.kahzerxmod.utils.PlayerUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import static net.minecraft.commands.SharedSuggestionProvider.suggest;
import static net.minecraft.commands.Commands.literal;

public class SpoofCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, SpoofExtension spoof) {
        dispatcher.register(literal("spoof").
                requires(serverCommandSource -> PermUtils.hasPermission(serverCommandSource, 2) && spoof.extensionSettings().isEnabled()).
                then(Commands.argument("player", StringArgumentType.word()).
                        suggests((c, b) -> suggest(PlayerUtils.getPlayers(c.getSource()), b)).
                        then(Commands.literal("enderChest").
                                executes(context -> spoof.spoofEC(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "player"))))
                        /* then(CommandManager.literal("inventory"). // TODO either fix the player interaction or disable completely as in readonly
                                executes(context -> spoof.spoofInv(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "player"))))*/));
    }
}
