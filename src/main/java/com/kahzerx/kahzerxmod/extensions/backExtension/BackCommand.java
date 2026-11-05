package com.kahzerx.kahzerxmod.extensions.backExtension;

import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsLevels;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.literal;

public class BackCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, BackExtension back) {
        dispatcher.register(literal("back").
                requires(serverCommandSource -> {
                    if (back.extensionSettings().isEnabled() && back.permsExtension.extensionSettings().isEnabled()) {
                        return back.permsExtension.hasLevel(serverCommandSource, PermsLevels.MOD);
                    }
                    return false;
                }).
                executes(context -> back.tpBack(context.getSource())));
    }
}
