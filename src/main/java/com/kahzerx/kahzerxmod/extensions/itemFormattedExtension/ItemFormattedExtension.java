package com.kahzerx.kahzerxmod.extensions.itemFormattedExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.HashMap;

public class ItemFormattedExtension extends GenericExtension implements Extensions {
    public static boolean isExtensionEnabled = false;

    public ItemFormattedExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "formattedItems", "Items renamed on anvils can set format if set on the usual mc formatting replacing § with %."));
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

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }
}
