package com.kahzerx.kahzerxmod.extensions.bocaExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

import java.util.HashMap;

public class BocaExtension extends GenericExtension implements Extensions {
    public BocaExtension(HashMap<String, Boolean> config) {
        super(new ExtensionSettings(config, "boca", "Enables /boca & /boquita command."));
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        new BocaCommand().register(dispatcher, this);
        new BoquitaCommand().register(dispatcher, this);
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }
}
