package com.kahzerx.kahzerxmod.extensions.skullExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.mojang.brigadier.CommandDispatcher;
import java.util.HashMap;
import net.minecraft.commands.CommandSourceStack;

public class SkullExtension extends GenericExtension implements Extensions {
    public SkullExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "skull", "Gives player heads."));
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        new SkullCommand().register(dispatcher, this);
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }
}
