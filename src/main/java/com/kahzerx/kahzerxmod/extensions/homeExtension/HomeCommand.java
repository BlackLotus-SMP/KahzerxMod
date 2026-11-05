package com.kahzerx.kahzerxmod.extensions.homeExtension;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.literal;

public class HomeCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, HomeExtension home) {
        dispatcher.register(literal("home").
                requires(server -> home.extensionSettings().isEnabled()).
                executes(context -> home.tpHome(context.getSource())));
        dispatcher.register(literal("HOME").  // Popular request smh...
                requires(server -> home.extensionSettings().isEnabled()).
                executes(context -> home.tpHome(context.getSource())));
    }
}
