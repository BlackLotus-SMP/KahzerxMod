package com.kahzerx.kahzerxmod.extensions.deathMsgExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.utils.DimUtils;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import java.util.HashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class DeathMsgExtension extends GenericExtension implements Extensions {
    public DeathMsgExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "deathMessage", "Print death position when player dies."));
    }

    @Override
    public void onPlayerDied(ServerPlayer player) {
        if (this.getSettings().isEnabled()) {
            player.sendSystemMessage(MarkEnum.RIP.appendMsg(String.format("%s %s", DimUtils.getDimensionWithColor(player.level()), DimUtils.formatCoords(player.getX(), player.getY(), player.getZ()))));
        }
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    @Override
    public void onExtensionEnabled(CommandSourceStack source) { }

    @Override
    public void onExtensionDisabled(CommandSourceStack source) { }
}
