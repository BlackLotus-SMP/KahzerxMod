package com.kahzerx.kahzerxmod.extensions.lobotomizeVillagersExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;

public class LobotomizeVillagersExtension extends GenericExtension implements Extensions {
    public static boolean isExtensionEnabled = false;

    public LobotomizeVillagersExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "lobotomizeVillagers", "Trading hall villagers stop running their AI when no player is nearby."));
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    @Override
    public void onServerRun(MinecraftServer minecraftServer) {
        isExtensionEnabled = this.getSettings().isEnabled();
    }

    @Override
    public void onExtensionEnabled(CommandSourceStack source) {
        isExtensionEnabled = true;
    }

    @Override
    public void onExtensionDisabled(CommandSourceStack source) {
        isExtensionEnabled = false;
    }
}
