package com.kahzerx.kahzerxmod.extensions.maintenanceExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;

public class MaintenanceExtension extends GenericExtension implements Extensions {
    public static boolean isExtensionEnabled = false;
    private MinecraftServer server = null;

    public MaintenanceExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "maintenance", "Sets your server in maintenance mode so only op players can join."));
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    @Override
    public void onServerRun(MinecraftServer minecraftServer) {
        isExtensionEnabled = this.getSettings().isEnabled();
        this.server = minecraftServer;
    }

    @Override
    public void onExtensionEnabled(ServerCommandSource source) {
        isExtensionEnabled = true;
        for (ServerPlayerEntity player : this.server.getPlayerManager().getPlayerList()) {
            if (!this.server.getPlayerManager().isOperator(player.getGameProfile())) {
                player.networkHandler.disconnect(Text.literal("Server is closed for maintenance"));
            }
        }
    }

    @Override
    public void onExtensionDisabled(ServerCommandSource source) {
        isExtensionEnabled = false;
    }
}
