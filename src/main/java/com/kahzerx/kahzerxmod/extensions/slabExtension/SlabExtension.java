package com.kahzerx.kahzerxmod.extensions.slabExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.HashMap;

public class SlabExtension extends GenericExtension implements Extensions {
    public static boolean isExtensionEnabled = false;

    public SlabExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "slab", "Enchants the slab on your main hand with the /slab command so you can always place the upper slab."));
    }

    @Override
    public void onServerRun(MinecraftServer minecraftServer) {
        isExtensionEnabled = this.getSettings().isEnabled();
    }

    @Override
    public void onExtensionDisabled(ServerCommandSource source) {
        Extensions.super.onExtensionDisabled(source);
        isExtensionEnabled = false;
    }

    @Override
    public void onExtensionEnabled(ServerCommandSource source) {
        Extensions.super.onExtensionEnabled(source);
        isExtensionEnabled = true;
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        new SlabCommand().register(dispatcher, this);
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }
}
