package com.kahzerx.kahzerxmod.extensions.kloneExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsExtension;
import com.kahzerx.kahzerxmod.klone.KlonePlayerEntity;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

public class KloneExtension extends GenericExtension implements Extensions {
    private final PermsExtension permsExtension;
    private final ArrayList<KlonePlayerEntity> klones = new ArrayList<>();
    private Timer timer;

    public KloneExtension(HashMap<String, String> fileSettings, PermsExtension perms) {
        super(new ExtensionSettings(fileSettings, "klone", "Clones your player to afk (will kick you) for up to 1 day; the bot will leave once you rejoin."));
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

    public void removeKlone(KlonePlayerEntity klonedPlayer) {
        klones.remove(klonedPlayer);
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
