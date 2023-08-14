package com.kahzerx.kahzerxmod.extensions.farmlandMyceliumExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.HashMap;

public class FarmlandMyceliumExtension extends GenericExtension implements Extensions {
    public static boolean isExtensionEnabled = false;
    public FarmlandMyceliumExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "farmlandMycelium", "Hoe can be used to farm mycelium."));
    }

    @Override
    public void onServerRun(MinecraftServer minecraftServer) {
        isExtensionEnabled = this.getSettings().isEnabled();
    }

    @Override
    public void onExtensionDisabled(ServerCommandSource source) {
        Extensions.super.onExtensionDisabled(source);
        isExtensionEnabled = false;
    }

    @Override
    public void onExtensionEnabled(ServerCommandSource source) {
        Extensions.super.onExtensionEnabled(source);
        isExtensionEnabled = true;
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }
}
