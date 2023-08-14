package com.kahzerx.kahzerxmod.extensions.playerDropsSkullExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.HashMap;

public class PlayerDropsSkullExtension extends GenericExtension implements Extensions {
    public static boolean isExtensionEnabled = false;

    public PlayerDropsSkullExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "playerDropsSkull", "Players have a 12% chance of dropping skull on death by trident lightning and a 30% by natural lightning."));
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
    public void onExtensionEnabled(ServerCommandSource source) {
        isExtensionEnabled = true;
    }

    @Override
    public void onExtensionDisabled(ServerCommandSource source) {
        isExtensionEnabled = false;
    }
}
