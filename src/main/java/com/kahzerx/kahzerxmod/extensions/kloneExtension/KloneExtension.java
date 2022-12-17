package com.kahzerx.kahzerxmod.extensions.kloneExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsExtension;
import com.kahzerx.kahzerxmod.klone.KlonePlayerEntity;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Timer;

public class KloneExtension extends GenericExtension implements Extensions {
    private final PermsExtension permsExtension;
    public static final ArrayList<KlonePlayerEntity> klones = new ArrayList<>();
    private Timer timer;

    public KloneExtension(ExtensionSettings settings, PermsExtension perms) {
        super(settings);
        this.permsExtension = perms;
    }

    public ArrayList<KlonePlayerEntity> getKlones() {
        return klones;
    }

    @Override
    public void onServerStarted(MinecraftServer minecraftServer) {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer.purge();
        }
        this.timer = new Timer("KLONE_TIMEOUT");
        this.timer.schedule(new KloneTimeout(this), 5_000, 60 * 60 * 1_000);
    }

    @Override
    public void onServerStop() {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer.purge();
        }
    }

    public void addKlone(KlonePlayerEntity klonedPlayer) {
        klones.add(klonedPlayer);
    }

    @Override
    public void onPlayerJoined(ServerPlayerEntity player) {
        if (player instanceof KlonePlayerEntity) {
            return;
        }
        for (KlonePlayerEntity kp : klones) {
            if (player.getUuid() == kp.getUuid()) {
                klones.remove(kp);
                return;
            }
        }
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    public PermsExtension getPermsExtension() {
        return this.permsExtension;
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        new KloneCommand().register(dispatcher, this);
    }
}
