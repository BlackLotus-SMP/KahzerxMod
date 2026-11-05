package com.kahzerx.kahzerxmod.extensions.kloneExtension;

import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsLevels;
import com.kahzerx.kahzerxmod.klone.KlonePlayerEntity;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

public class KloneCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, KloneExtension klone) {
        dispatcher.register(literal("klone").
                requires(server -> {
                    if (klone.extensionSettings().isEnabled() && klone.getPermsExtension().extensionSettings().isEnabled()) {
                        return klone.getPermsExtension().hasLevel(server, PermsLevels.MEMBER);
                    }
                    return false;
                }).
                executes(context -> {
                    ServerPlayer sourcePlayer = context.getSource().getPlayer();
                    KlonePlayerEntity klonePlayer = KlonePlayerEntity.createKlone(context.getSource().getServer(), sourcePlayer, klone);
                    klone.addKlone(klonePlayer);
                    return 1;
                }));
        dispatcher.register(literal("clown").
                requires(server -> {
                    if (klone.extensionSettings().isEnabled() && klone.getPermsExtension().extensionSettings().isEnabled()) {
                        return klone.getPermsExtension().hasLevel(server, PermsLevels.MEMBER);
                    }
                    return false;
                }).
                executes(context -> {
                    ServerPlayer sourcePlayer = context.getSource().getPlayer();
                    KlonePlayerEntity.createKlone(context.getSource().getServer(), sourcePlayer, klone);
                    return 1;
                }));
    }
}
