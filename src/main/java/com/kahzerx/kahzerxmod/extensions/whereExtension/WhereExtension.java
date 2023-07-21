package com.kahzerx.kahzerxmod.extensions.whereExtension;

import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsExtension;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

import java.util.HashMap;

public class WhereExtension extends GenericExtension implements Extensions {
    private PermsExtension permsExtension;

    public WhereExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "where", "Enables /where."));
    }

    @Override
    public void onExtensionsReady(ExtensionManager em) {
        this.permsExtension = (PermsExtension) em.getExtensions().get("perms");
    }

    public PermsExtension getPermsExtension() {
        return this.permsExtension;
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        new WhereCommand().register(dispatcher, this);
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }
}
