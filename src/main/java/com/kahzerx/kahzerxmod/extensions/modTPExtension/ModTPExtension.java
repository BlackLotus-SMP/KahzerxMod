package com.kahzerx.kahzerxmod.extensions.modTPExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsExtension;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ModTPExtension extends GenericExtension implements Extensions {
    public final PermsExtension permsExtension;
    public ModTPExtension(ExtensionSettings settings, PermsExtension perms) {
        super(settings);
        this.permsExtension = perms;
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        new ModTPCommand().register(dispatcher, this);
    }

    public int tp(ServerCommandSource source, String playerName) {
        ServerPlayerEntity player = source.getServer().getPlayerManager().getPlayer(playerName);
        if (player == null) {
            source.sendFeedback(MarkEnum.CROSS.appendMessage("Not online"), false);
            return 1;
        }
        ServerPlayerEntity sourcePlayer = source.getPlayer();
        sourcePlayer.teleport(player.getServerWorld(), player.getX(), player.getY(), player.getZ(), sourcePlayer.getYaw(), sourcePlayer.getPitch());
        return 1;
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }
}
