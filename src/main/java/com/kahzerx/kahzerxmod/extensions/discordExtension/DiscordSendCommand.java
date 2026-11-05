package com.kahzerx.kahzerxmod.extensions.discordExtension;

import com.kahzerx.kahzerxmod.utils.PermUtils;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension.DiscordExtension;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class DiscordSendCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, DiscordExtension discordExtension) {
        dispatcher.register(literal("discordSend").
                requires(serverCommandSource -> PermUtils.hasPermission(serverCommandSource, 2) && discordExtension.extensionSettings().isEnabled()).
                then(argument("message", MessageArgument.message()).
                        executes(context -> {
                            context.getSource().getServer().getPlayerList().broadcastSystemMessage(
                                    MessageArgument.getMessage(context, "message"),
                                    false
                            );
                            discordExtension.getBot().sendSysMessage(MessageArgument.getMessage(context, "message").getString(), discordExtension.extensionSettings().getPrefix());
                            return 1;
                        })));
    }
}
