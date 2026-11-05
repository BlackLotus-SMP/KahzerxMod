package com.kahzerx.kahzerxmod.extensions.randomTPExtension;

import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsLevels;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.literal;

public class RandomTPCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, RandomTPExtension rTP) {
        dispatcher.register(literal("randomTP").
                requires(server -> {
                    if (rTP.extensionSettings().isEnabled() && rTP.getPermsExtension().extensionSettings().isEnabled()) {
                        return rTP.getPermsExtension().hasLevel(server, PermsLevels.MEMBER);
                    }
                    return false;
                }).
                executes(context -> rTP.tpAndSpawnPoint(context.getSource())));
        dispatcher.register(literal("rTP").
                requires(server -> {
                    if (rTP.extensionSettings().isEnabled() && rTP.getPermsExtension().extensionSettings().isEnabled()) {
                        return rTP.getPermsExtension().hasLevel(server, PermsLevels.MEMBER);
                    }
                    return false;
                }).
                executes(context -> rTP.tpAndSpawnPoint(context.getSource())));
    }
}
