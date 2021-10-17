package com.kahzerx.kahzerxmod;

import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.sql.Connection;

public interface Extensions {
    default void onServerRun(MinecraftServer minecraftServer) {}
    default void onRegisterCommands(CommandDispatcher<ServerCommandSource> dispatcher) {}
    default void onCreateDatabase(Connection conn) {}
    default void onServerStop() {}
    default void onAutoSave() {}
    default void onPlayerJoined(ServerPlayerEntity player) {}
    default void onPlayerLeft(ServerPlayerEntity player) {}
    default void onPlayerDied(ServerPlayerEntity player) {}
    default void onChatMessage(ServerPlayerEntity player, String chatMessage) {}
    default LiteralArgumentBuilder<ServerCommandSource> settingsCommand() {
        return null;
    }

    default void onAdvancement(String advancement) {}
    ExtensionSettings extensionSettings();
    void onExtensionEnabled();
    void onExtensionDisabled();
}
