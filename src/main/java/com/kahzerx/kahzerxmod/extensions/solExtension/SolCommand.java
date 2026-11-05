package com.kahzerx.kahzerxmod.extensions.solExtension;

import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;


public class SolCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, SolExtension sol) {
        dispatcher.register(literal("sol").
                requires(enabled -> sol.extensionSettings().isEnabled()).
                executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    player.igniteForSeconds(3);
                    context.getSource().sendSuccess(MarkEnum.SUN.appendMessage("Very hot indeed!"), false);
                    return 1;
                }));
    }
}
