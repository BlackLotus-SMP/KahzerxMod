package com.kahzerx.kahzerxmod.extensions.deepslateInstaMineExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.HashMap;

public class DeepslateInstaMineExtension extends GenericExtension implements Extensions {
    public static boolean isExtensionEnabled = false;

    public DeepslateInstaMineExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "deepslateInstaMine", "Deepslate instamine as if it was stone."));
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
