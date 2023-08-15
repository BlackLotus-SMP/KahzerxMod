package com.kahzerx.kahzerxmod.extensions.renewableElytraExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.HashMap;

public class RenewableElytraExtension extends GenericExtension implements Extensions {
    public static boolean isExtensionEnabled = false;

    public RenewableElytraExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "renewableElytra", "Phantoms killed by shulker have 25% chance of dropping elytras."));
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
