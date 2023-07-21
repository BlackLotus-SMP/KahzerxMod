package com.kahzerx.kahzerxmod.extensions.survivalExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

import java.util.HashMap;

public class SurvivalExtension extends GenericExtension implements Extensions {
    public SurvivalExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "survival", "/s, survival - night vision - conduit (stolen from carpet)."));
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        new SurvivalCommand().register(dispatcher, this);
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    public int setSurvivalMode(ServerCommandSource src) throws CommandSyntaxException {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) {
            return 1;
        }
        if (player.isSpectator() || player.isCreative()) {
            player.changeGameMode(GameMode.SURVIVAL);
            player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            player.removeStatusEffect(StatusEffects.CONDUIT_POWER);
        }
        return 1;
    }
}
