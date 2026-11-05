package com.kahzerx.kahzerxmod.extensions.seedExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.KahzerxServer;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.mojang.brigadier.CommandDispatcher;
import java.util.HashMap;
import net.minecraft.commands.CommandSourceStack;

public class SeedExtension extends GenericExtension implements Extensions {
    public SeedExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "seed", "Enables seed command for everyone in the server."));
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        new KSeedCommand().register(dispatcher, this);
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }
}
