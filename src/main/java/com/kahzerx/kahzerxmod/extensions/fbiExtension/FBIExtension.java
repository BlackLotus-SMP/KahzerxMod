package com.kahzerx.kahzerxmod.extensions.fbiExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.mojang.brigadier.CommandDispatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class FBIExtension extends GenericExtension implements Extensions {
    private static final List<ServerPlayer> hiddenPlayers = new ArrayList<>();
    public FBIExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "fbi", "Allows ops and mods to be in the server without players noticing."));
    }

    @Override
    public void onPlayerLeft(ServerPlayer player) {
        hiddenPlayers.remove(player);
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        new FBICommand().register(dispatcher, this);
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    public static List<ServerPlayer> getHiddenPlayers() {
        return hiddenPlayers;
    }
}
