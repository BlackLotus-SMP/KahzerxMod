package com.kahzerx.kahzerxmod.extensions.kloneExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsExtension;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

public class KloneExtension extends GenericExtension implements Extensions {
    private final PermsExtension permsExtension;

    public KloneExtension(ExtensionSettings settings, PermsExtension perms) {
        super(settings);
        this.permsExtension = perms;
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    public PermsExtension getPermsExtension() {
        return this.permsExtension;
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        new KloneCommand().register(dispatcher, this);
    }
}
