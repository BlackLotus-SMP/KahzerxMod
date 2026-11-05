package com.kahzerx.kahzerxmod;

import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.utils.PlayerUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import java.sql.Connection;

public interface Extensions extends Comparable<Extensions> {
    default void onServerRun(MinecraftServer minecraftServer) {}
    default void onServerStarted(MinecraftServer minecraftServer) {}
    default void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {}
    default void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess) {}
    default void onCreateDatabase(Connection conn) {}
    default void onCreateDatabase(String worldPath) {}
    default void onServerStop() {}
    default void onAutoSave() {}
    default void onAutoSave(MinecraftServer server) {}
    default void onPlayerJoined(ServerPlayer player) {}
    default void onPlayerConnected(ServerPlayer player) {}
    default void onPlayerLeft(ServerPlayer player) {}
    default void onPlayerDied(ServerPlayer player) {}
    default void onPlayerBreakBlock(ServerPlayer player, Level world, BlockPos pos) {}
    default void onPlayerPlaceBlock(ServerPlayer player, Level world, BlockPos pos) {}
    default void onChatMessage(ServerPlayer player, String chatMessage) {}
    default void onCommand(ServerPlayer player, String command) {}
    default void onAdvancement(String advancement) {}
    default void onTick(MinecraftServer server) {}
    default void onClick(ServerPlayer player) {}
    default void onPlayerSleep(ServerPlayer player) {}
    default void onPlayerWakeUp(ServerPlayer player) {}
    default void onExtensionEnabled(CommandSourceStack source) {
        PlayerUtils.reloadCommands();
    }
    default void onExtensionDisabled(CommandSourceStack source) {
        PlayerUtils.reloadCommands();
    }
    default void settingsCommand(LiteralArgumentBuilder<CommandSourceStack> builder) {}
    ExtensionSettings extensionSettings();
    default void onExtensionsReady(ExtensionManager em) {}

    @Override
    default int compareTo(Extensions extensions) {
        return this.extensionSettings().getName().compareTo(extensions.extensionSettings().getName());
    }
}
