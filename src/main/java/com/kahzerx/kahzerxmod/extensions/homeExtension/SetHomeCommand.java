package com.kahzerx.kahzerxmod.extensions.homeExtension;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.literal;

public class SetHomeCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, HomeExtension home) {
        dispatcher.register(literal("setHome").
                requires(server -> home.extensionSettings().isEnabled()).
                executes(context -> home.saveHome(context.getSource())));
    }
}
