package com.kahzerx.kahzerxmod.extensions.hatExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

import java.util.HashMap;

public class HatExtension extends GenericExtension implements Extensions {
    public HatExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "hat", "Puts whatever item you have in the main hand on your head."));
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        new HatCommand().register(dispatcher, this);
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }
}
