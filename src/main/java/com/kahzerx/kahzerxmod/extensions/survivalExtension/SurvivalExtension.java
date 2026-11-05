package com.kahzerx.kahzerxmod.extensions.survivalExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.HashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;

public class SurvivalExtension extends GenericExtension implements Extensions {
    public SurvivalExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "survival", "/s, survival - night vision - conduit (stolen from carpet)."));
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        new SurvivalCommand().register(dispatcher, this);
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    public int setSurvivalMode(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer player = src.getPlayer();
        if (player == null) {
            return 1;
        }
        if (player.isSpectator() || player.isCreative()) {
            player.setGameMode(GameType.SURVIVAL);
            player.removeEffect(MobEffects.NIGHT_VISION);
            player.removeEffect(MobEffects.CONDUIT_POWER);
        }
        return 1;
    }
}
