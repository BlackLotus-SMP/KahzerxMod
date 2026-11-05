package com.kahzerx.kahzerxmod.extensions.cameraExtension;

import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsExtension;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.HashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;

public class CameraExtension extends GenericExtension implements Extensions {
    public PermsExtension permsExtension;

    public CameraExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "camera", "/c, spectator + night vision + conduit (stolen from carpet)."));
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
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

    public int setCameraMode(CommandSourceStack src) {
        ServerPlayer player = src.getPlayer();
        if (player == null) {
            return 1;
        }
        player.setGameMode(GameType.SPECTATOR);
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 999999, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 999999, 0, false, false));
        return 1;
    }
}
