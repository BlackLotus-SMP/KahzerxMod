package com.kahzerx.kahzerxmod.extensions.villagersFollowEmeraldExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;

public class VillagersFollowEmeraldExtension extends GenericExtension implements Extensions {
    public static boolean isExtensionEnabled = false;

    public VillagersFollowEmeraldExtension(HashMap<String, Boolean> config) {
        super(new ExtensionSettings(config, "villagersFollowEmeralds", "Villagers will follow any player holding emerald blocks."));
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
