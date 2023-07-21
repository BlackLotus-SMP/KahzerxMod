package com.kahzerx.kahzerxmod.extensions.cameraExtension;

import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsExtension;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

import java.util.HashMap;

public class CameraExtension extends GenericExtension implements Extensions {
    public PermsExtension permsExtension;

    public CameraExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "camera", "/c, spectator + night vision + conduit (stolen from carpet)."));
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        new CameraCommand().register(dispatcher, this);
    }

    @Override
    public void onExtensionsReady(ExtensionManager em) {
        this.permsExtension = (PermsExtension) em.getExtensions().get("perms");
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    public int setCameraMode(ServerCommandSource src) {
        ServerPlayerEntity player = src.getPlayer();
        if (player == null) {
            return 1;
        }
        player.changeGameMode(GameMode.SPECTATOR);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 999999, 0, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.CONDUIT_POWER, 999999, 0, false, false));
        return 1;
    }
}
