package com.kahzerx.kahzerxmod.extensions.randomTPExtension;

import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsLevels;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class RandomTPCommand {
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, RandomTPExtension rTP) {
        dispatcher.register(literal("randomTP").
                requires(server -> {
                    if (rTP.extensionSettings().isEnabled() && rTP.getPermsExtension().extensionSettings().isEnabled()) {
                        return rTP.getPermsExtension().getDBPlayerPerms(server.getPlayer().getUuidAsString()).getId() >= PermsLevels.SUB.getId();
                    }
                    return false;
                }).
                executes(context -> rTP.tpAndSpawnPoint(context.getSource())));
        dispatcher.register(literal("rTP").
                requires(server -> {
                    if (rTP.extensionSettings().isEnabled() && rTP.getPermsExtension().extensionSettings().isEnabled()) {
                        return rTP.getPermsExtension().getDBPlayerPerms(server.getPlayer().getUuidAsString()).getId() >= PermsLevels.SUB.getId();
                    }
                    return false;
                }).
                executes(context -> rTP.tpAndSpawnPoint(context.getSource())));
    }
}
