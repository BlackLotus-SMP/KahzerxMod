package com.kahzerx.kahzerxmod.extensions.itemFormattedExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;

public class ItemFormattedExtension extends GenericExtension implements Extensions {
    public static boolean isExtensionEnabled = false;

    public ItemFormattedExtension(HashMap<String, Boolean> config) {
        super(new ExtensionSettings(config, "formattedItems", "Items renamed on anvils can set format if set on the usual mc formatting replacing § with %."));
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

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }
}
