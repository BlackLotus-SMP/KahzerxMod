package com.kahzerx.kahzerxmod.extensions.farmlandMyceliumExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
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
    public void onExtensionDisabled(CommandSourceStack source) {
        Extensions.super.onExtensionDisabled(source);
        isExtensionEnabled = false;
    }

    @Override
    public void onExtensionEnabled(CommandSourceStack source) {
        Extensions.super.onExtensionEnabled(source);
        isExtensionEnabled = true;
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }
}
