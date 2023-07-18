package com.kahzerx.kahzerxmod.extensions.fckPrivacyExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

public class FckPrivacyExtension extends GenericExtension implements Extensions {
    private static final Logger LOGGER = LogManager.getLogger();

    public FckPrivacyExtension(HashMap<String, Boolean> config) {
        super(new ExtensionSettings(config, "fckPrivacy", "Saves every executed command including private messages in the logs file."));
    }

    @Override
    public void onCommand(ServerPlayerEntity player, String command) {
        if (this.getSettings().isEnabled()) {
            LOGGER.info(String.format("<%s> /%s", player.getName().getString(), command));
        }
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    @Override
    public void onExtensionEnabled() { }

    @Override
    public void onExtensionDisabled() { }
}
