package com.kahzerx.kahzerxmod.extensions.deathMsgExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.utils.DimUtils;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;

public class DeathMsgExtension extends GenericExtension implements Extensions {
    public DeathMsgExtension(HashMap<String, Boolean> config) {
        super(new ExtensionSettings(config, "deathMessage", "Print death position when player dies."));
    }

    @Override
    public void onPlayerDied(ServerPlayerEntity player) {
        if (this.getSettings().isEnabled()) {
            player.sendMessage(MarkEnum.RIP.appendMsg(String.format("%s %s", DimUtils.getDimensionWithColor(player.getWorld()), DimUtils.formatCoords(player.getX(), player.getY(), player.getZ()))), false);
        }
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    @Override
    public void onExtensionEnabled() { }

    @Override
    public void onExtensionDisabled() { }
}
