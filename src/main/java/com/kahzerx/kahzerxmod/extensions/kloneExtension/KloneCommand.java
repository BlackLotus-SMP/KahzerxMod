package com.kahzerx.kahzerxmod.extensions.kloneExtension;

import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsLevels;
import com.kahzerx.kahzerxmod.klone.KlonePlayerEntity;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.server.command.CommandManager.literal;

public class KloneCommand {
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, KloneExtension klone) {
        dispatcher.register(literal("klone").
                requires(server -> {
                    if (klone.extensionSettings().isEnabled() && klone.getPermsExtension().extensionSettings().isEnabled()) {
                        return klone.getPermsExtension().getDBPlayerPerms(server.getPlayer().getUuidAsString()).getId() >= PermsLevels.MEMBER.getId();
                    }
                    return false;
                }).
                executes(context -> {
                    ServerPlayerEntity sourcePlayer = context.getSource().getPlayer();
                    KlonePlayerEntity.createKlone(context.getSource().getServer(), sourcePlayer);
                    return 1;
                }));
        dispatcher.register(literal("clown").
                requires(server -> {
                    if (klone.extensionSettings().isEnabled() && klone.getPermsExtension().extensionSettings().isEnabled()) {
                        return klone.getPermsExtension().getDBPlayerPerms(server.getPlayer().getUuidAsString()).getId() >= PermsLevels.MEMBER.getId();
                    }
                    return false;
                }).
                executes(context -> {
                    ServerPlayerEntity sourcePlayer = context.getSource().getPlayer();
                    KlonePlayerEntity.createKlone(context.getSource().getServer(), sourcePlayer);
                    return 1;
                }));
    }
}
