package com.kahzerx.kahzerxmod.extensions.hereExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.utils.DimUtils;
import com.kahzerx.kahzerxmod.utils.PlayerUtils;
import com.mojang.brigadier.CommandDispatcher;
import java.util.HashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

public class HereExtension extends GenericExtension implements Extensions {
    public HereExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "here", "/here, print current location + glowing 5 seconds."));
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        new HereCommand().register(dispatcher, this);
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    public int sendLocation(CommandSourceStack src, ServerPlayer toPlayer) {
        ServerPlayer player = src.getPlayer();
        if (player == null) {
            return 1;
        }
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        MutableComponent msg;
        if (player.level().dimension().identifier().equals(Level.OVERWORLD.identifier())) {
            msg = Component.literal(String.format(
                    "%s %s %s -> %s %s",
                    PlayerUtils.getPlayerWithColor(player),
                    DimUtils.getDimensionWithColor(player.level()),
                    DimUtils.formatCoords(x, y, z),
                    DimUtils.getDimensionWithColor(Level.NETHER.identifier()),
                    DimUtils.formatCoords(x / 8, y, z / 8)
            ));
            if (toPlayer != null) {
                toPlayer.sendSystemMessage(msg);
            } else {
                src.getServer().getPlayerList().broadcastSystemMessage(msg, false);
            }
        } else if (player.level().dimension().identifier().equals(Level.NETHER.identifier())) {
            msg = Component.literal(String.format(
                    "%s %s %s -> %s %s",
                    PlayerUtils.getPlayerWithColor(player),
                    DimUtils.getDimensionWithColor(player.level()),
                    DimUtils.formatCoords(x, y, z),
                    DimUtils.getDimensionWithColor(Level.OVERWORLD.identifier()),
                    DimUtils.formatCoords(x *  8, y, z * 8)
            ));
            if (toPlayer != null) {
                toPlayer.sendSystemMessage(msg);
            } else {
                src.getServer().getPlayerList().broadcastSystemMessage(msg, false);
            }
        } else if (player.level().dimension().identifier().equals(Level.END.identifier())) {
            msg = Component.literal(String.format(
                    "%s %s %s",
                    PlayerUtils.getPlayerWithColor(player),
                    DimUtils.getDimensionWithColor(player.level()),
                    DimUtils.formatCoords(x, y, z)
            ));
            if (toPlayer != null) {
                toPlayer.sendSystemMessage(msg);
            } else {
                src.getServer().getPlayerList().broadcastSystemMessage(msg, false);
            }
        }
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, false));
        player.level().playSound(null, player.blockPosition(), NoteBlockInstrument.HARP.getSoundEvent().value(), SoundSource.RECORDS, 3.0f, (float)Math.pow(2.0, (double)(24 - 12) / 12.0));
        return 1;
    }
}
