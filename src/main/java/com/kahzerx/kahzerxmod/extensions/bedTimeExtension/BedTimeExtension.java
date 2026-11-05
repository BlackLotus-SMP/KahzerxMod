package com.kahzerx.kahzerxmod.extensions.bedTimeExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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
    public void onPlayerSleep(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        if (server != null && this.extensionSettings().isEnabled()) {
            server.getPlayerList().broadcastSystemMessage(MarkEnum.SLEEP.appendMsg(player.getName().getString() + " went to sleep", ChatFormatting.YELLOW), false);
        }
    }

    @Override
    public void onPlayerWakeUp(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        if (server != null && this.extensionSettings().isEnabled()) {
            server.getPlayerList().broadcastSystemMessage(MarkEnum.SLEEP.appendMsg(player.getName().getString() + " woke up", ChatFormatting.YELLOW), false);
        }
    }
}
