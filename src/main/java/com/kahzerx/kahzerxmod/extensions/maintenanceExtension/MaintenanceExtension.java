package com.kahzerx.kahzerxmod.extensions.maintenanceExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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
    public void onExtensionEnabled(CommandSourceStack source) {
        isExtensionEnabled = true;
        for (ServerPlayer player : this.server.getPlayerList().getPlayers()) {
            if (!this.server.getPlayerList().isOp(player.nameAndId())) {
                player.connection.disconnect(Component.literal("Server is closed for maintenance"));
            }
        }
    }

    @Override
    public void onExtensionDisabled(CommandSourceStack source) {
        isExtensionEnabled = false;
    }
}
