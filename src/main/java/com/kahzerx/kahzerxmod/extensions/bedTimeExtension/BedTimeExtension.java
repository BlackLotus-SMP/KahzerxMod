package com.kahzerx.kahzerxmod.extensions.bedTimeExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

import java.util.HashMap;

public class BedTimeExtension extends GenericExtension implements Extensions {
    public BedTimeExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "bedTime", "Notifies when a player goes to sleep."));
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    @Override
    public void onPlayerSleep(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server != null && this.extensionSettings().isEnabled()) {
            server.getPlayerManager().broadcast(MarkEnum.SLEEP.appendMsg(player.getName().getString() + " went to sleep", Formatting.YELLOW), false);
        }
    }

    @Override
    public void onPlayerWakeUp(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server != null && this.extensionSettings().isEnabled()) {
            server.getPlayerManager().broadcast(MarkEnum.SLEEP.appendMsg(player.getName().getString() + " woke up", Formatting.YELLOW), false);
        }
    }
}
