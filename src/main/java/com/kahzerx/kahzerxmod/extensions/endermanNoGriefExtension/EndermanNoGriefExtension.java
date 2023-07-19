package com.kahzerx.kahzerxmod.extensions.endermanNoGriefExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;

public class EndermanNoGriefExtension extends GenericExtension implements Extensions {
    public static boolean isExtensionEnabled = false;

    public EndermanNoGriefExtension(HashMap<String, Boolean> config) {
        super(new ExtensionSettings(config, "endermanNoGrief", "Prevents endermans to pickup or place blocks (this will break enderman based farms)."));
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
    public void onExtensionEnabled() {
        isExtensionEnabled = true;
    }

    @Override
    public void onExtensionDisabled() {
        isExtensionEnabled = false;
    }
}
