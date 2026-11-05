package com.kahzerx.kahzerxmod.extensions.joinMOTDExtension;

import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsLevels;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class JoinMOTDCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, JoinMOTDExtension joinMOTD) {
        dispatcher.register(literal("joinMOTD").
                requires(server -> {
                    if (joinMOTD.extensionSettings().isEnabled() && joinMOTD.getPermsExtension().extensionSettings().isEnabled()) {
                        return joinMOTD.getPermsExtension().hasLevel(server, PermsLevels.HELPER);
                    }
                    return false;
                }).
                then(literal("set").
                        then(argument("message", MessageArgument.message()).
                                executes(context -> {
                                    joinMOTD.updateMessage(context.getSource(), MessageArgument.getMessage(context, "message").getString());
                                    joinMOTD.getEm().saveSettings();
                                    return 1;
                                }))).
                then(literal("clear").
                        executes(context -> {
                            joinMOTD.extensionSettings().setMessage("");
                            joinMOTD.getEm().saveSettings();
                            return 1;
                        })).
                executes(context -> {
                    context.getSource().sendSuccess(MarkEnum.TICK.appendMessage("Actual MOTD!"), false);
                    context.getSource().sendSuccess(() -> joinMOTD.getFormatted(joinMOTD.extensionSettings().getMessage()), false);
                    return 1;
                }));
    }
}
