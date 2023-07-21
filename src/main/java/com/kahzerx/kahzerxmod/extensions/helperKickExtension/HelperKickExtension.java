package com.kahzerx.kahzerxmod.extensions.helperKickExtension;

import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsExtension;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;

public class HelperKickExtension extends GenericExtension implements Extensions {
    public static boolean isExtensionEnabled = false;
    public static PermsExtension permsExtension = null;

    public HelperKickExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "helperKick", "Allows helpers and above to run /kick"));
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    @Override
    public void onExtensionsReady(ExtensionManager em) {
        permsExtension = (PermsExtension) em.getExtensions().get("perms");
    }

    @Override
    public void onServerRun(MinecraftServer minecraftServer) {
        isExtensionEnabled = this.getSettings().isEnabled();
    }

    @Override
    public void onExtensionEnabled() {
        Extensions.super.onExtensionEnabled();
        isExtensionEnabled = true;
    }

    @Override
    public void onExtensionDisabled() {
        Extensions.super.onExtensionDisabled();
        isExtensionEnabled = false;
    }
}
