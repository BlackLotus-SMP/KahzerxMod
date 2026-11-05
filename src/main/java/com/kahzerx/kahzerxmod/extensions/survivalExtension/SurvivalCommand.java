package com.kahzerx.kahzerxmod.extensions.survivalExtension;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.literal;

public class SurvivalCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, SurvivalExtension survival) {
        dispatcher.register(literal("s").
                requires(server -> survival.extensionSettings().isEnabled()).
                executes(context -> survival.setSurvivalMode(context.getSource())));
    }
}
