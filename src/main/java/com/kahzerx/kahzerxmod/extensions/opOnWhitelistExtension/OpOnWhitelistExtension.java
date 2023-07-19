package com.kahzerx.kahzerxmod.extensions.opOnWhitelistExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;

public class OpOnWhitelistExtension extends GenericExtension implements Extensions {
    public static boolean isExtensionEnabled = false;
    public static MinecraftServer server = null;

    public OpOnWhitelistExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "opOnWhitelist", "Auto ops and deops on whitelist add and remove."));
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    @Override
    public void onServerRun(MinecraftServer minecraftServer) {
        isExtensionEnabled = this.getSettings().isEnabled();
        server = minecraftServer;
    }

    @Override
    public void onExtensionEnabled() {
        isExtensionEnabled = true;
    }

    @Override
    public void onExtensionDisabled() {
        isExtensionEnabled = false;
    }
}
