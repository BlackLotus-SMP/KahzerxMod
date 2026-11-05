package com.kahzerx.kahzerxmod;

import com.kahzerx.kahzerxmod.utils.PermUtils;
import com.kahzerx.kahzerxmod.database.ServerDatabase;
import com.kahzerx.kahzerxmod.utils.FileUtils;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import java.util.*;

import static net.minecraft.commands.Commands.literal;

public class KahzerxServer {
    // TODO cleanup based on interfaces, static func removal
    public static MinecraftServer minecraftServer;
    public static ServerDatabase db = new ServerDatabase();
    public static CommandDispatcher<CommandSourceStack> dispatcher;
    public static CommandBuildContext commandRegistryAccess;
    private static final String SETTINGS_BASE_COMMAND = "KSettings";
    private static final int DEFAULT_SETTINGS_COMMAND_LEVEL = 2;
    private static final ExtensionManager extensionManager = new ExtensionManager(SETTINGS_BASE_COMMAND, DEFAULT_SETTINGS_COMMAND_LEVEL);

    public static void onRunServer(MinecraftServer minecraftServer) {
        KahzerxServer.minecraftServer = minecraftServer;
        extensionManager.loadExtensions(FileUtils.loadConfig(minecraftServer.getWorldPath(LevelResource.ROOT).toString()));
        extensionManager.getExtensions().forEach((k, e) -> e.onExtensionsReady(extensionManager));
        extensionManager.getExtensions().forEach((k, e) -> e.onServerRun(minecraftServer));

        extensionManager.saveSettings();

        extensionManager.getExtensions().forEach((k, e) -> e.onRegisterCommands(dispatcher));
        extensionManager.getExtensions().forEach((k, e) -> e.onRegisterCommands(dispatcher, commandRegistryAccess));

        // TODO command of reload, from file, check diffs and apply
        // TODO refactor so you know which extension depends on which extension for dep tree on disable
        LiteralArgumentBuilder<CommandSourceStack> settingsCommand = literal(SETTINGS_BASE_COMMAND).
                requires(serverCommandSource -> PermUtils.hasPermission(serverCommandSource, extensionManager.getDefaultSettingCommandLevel()));
        for (Extensions ex : extensionManager.getExtensions().values()) {
            LiteralArgumentBuilder<CommandSourceStack> extensionSubCommand = literal(ex.extensionSettings().getName());
            extensionSubCommand.
                    then(literal("enable").
                            executes(context -> {
                                if (ex.extensionSettings().isEnabled()) {
                                    context.getSource().sendSuccess(MarkEnum.CROSS.appendMessage(ex.extensionSettings().getName() + " extension already enabled"), false);
                                    return 1;
                                }
                                ex.extensionSettings().setEnabled(true);
                                ex.onExtensionEnabled(context.getSource());
                                extensionManager.saveSettings();
                                if (ex.extensionSettings().isEnabled()) {
                                    context.getSource().sendSuccess(() -> MarkEnum.TICK.
                                            appendMsg(ex.extensionSettings().getName() + " extension ").
                                            append(Component.literal("enabled").withStyle(style -> style.
                                                    withColor(ChatFormatting.GREEN).
                                                    withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to disable"))).
                                                    withClickEvent(new ClickEvent.RunCommand(String.format("/%s %s disable", SETTINGS_BASE_COMMAND, ex.extensionSettings().getName()))))), false);
                                }
                                return 1;
                            })).
                    then(literal("disable").
                            executes(context -> {
                                if (!ex.extensionSettings().isEnabled()) {
                                    context.getSource().sendSuccess(MarkEnum.CROSS.appendMessage(ex.extensionSettings().getName() + " extension already disabled"), false);
                                    return 1;
                                }
                                ex.extensionSettings().setEnabled(false);
                                ex.onExtensionDisabled(context.getSource());
                                extensionManager.saveSettings();
                                if (!ex.extensionSettings().isEnabled()) {
                                    context.getSource().sendSuccess(() -> MarkEnum.TICK.appendMsg(ex.extensionSettings().getName() + " extension ").
                                            append(Component.literal("disabled").withStyle(style -> style.
                                                    withColor(ChatFormatting.RED).
                                                    withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to enable"))).
                                                    withClickEvent(new ClickEvent.RunCommand(String.format("/%s %s enable", SETTINGS_BASE_COMMAND, ex.extensionSettings().getName()))))), false);
                                }
                                return 1;
                            })).
                    executes(context -> {
                        context.getSource().sendSuccess(() -> Component.literal("\n" + ex.extensionSettings().getName() + "\n").withStyle(style -> style.withBold(true)).
                                        append(MarkEnum.INFO.appendMsg(ex.extensionSettings().getDescription() + "\n", ChatFormatting.GRAY).withStyle(style -> style.withBold(false))).
                                        append(Component.literal("Enabled: ").withStyle(style -> style.withBold(false).withColor(ChatFormatting.WHITE))).
                                        append(Component.literal(String.format("%b", ex.extensionSettings().isEnabled())).withStyle(style -> style.
                                                withBold(false).
                                                withColor(ex.extensionSettings().isEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED).
                                                withHoverEvent(new HoverEvent.ShowText(Component.literal(String.format("Click to %s", ex.extensionSettings().isEnabled() ? "disable" : "enable")))).
                                                withClickEvent(new ClickEvent.RunCommand(String.format("/%s %s %s", SETTINGS_BASE_COMMAND, ex.extensionSettings().getName(), ex.extensionSettings().isEnabled() ? "disable" : "enable")))
                                        )), false);
                        return 1;
                    });
            ex.settingsCommand(extensionSubCommand);  // Otros ajustes por si fueran necesarios para las extensiones más complejas.
            settingsCommand.then(extensionSubCommand);
        }
        settingsCommand.executes(context -> {
            List<MutableComponent> extensionNames = new ArrayList<>();
            for (Extensions ex : extensionManager.getExtensions().values()) {
                MutableComponent exData = MarkEnum.DOT.appendText(Component.literal(ex.extensionSettings().getName() + " | ").withStyle(
                        style -> style.
                                withBold(false).
                                withUnderlined(false).
                                withColor(ChatFormatting.WHITE).
                                withHoverEvent(new HoverEvent.ShowText(Component.literal(ex.extensionSettings().getDescription())))));  // TODO click event to show metadata
                exData.append(Component.literal("true").withStyle(
                        style -> style.
                                withBold(false).
                                withColor(ex.extensionSettings().isEnabled() ? ChatFormatting.GREEN : ChatFormatting.GRAY).
                                withHoverEvent(new HoverEvent.ShowText(Component.literal(String.format("Enable %s", ex.extensionSettings().getName())))).
                                withClickEvent(new ClickEvent.RunCommand(String.format("/%s %s enable", SETTINGS_BASE_COMMAND, ex.extensionSettings().getName())))));
                exData.append(Component.literal(" ").withStyle(
                        style -> style.
                                withHoverEvent(new HoverEvent.ShowText(Component.literal(""))).
                                withUnderlined(false)));
                exData.append(Component.literal("false").withStyle(
                        style -> style.
                                withBold(false).
                                withColor(!ex.extensionSettings().isEnabled() ? ChatFormatting.RED : ChatFormatting.GRAY).
                                withHoverEvent(new HoverEvent.ShowText(Component.literal(String.format("Disable %s", ex.extensionSettings().getName())))).
                                withClickEvent(new ClickEvent.RunCommand(String.format("/%s %s disable", SETTINGS_BASE_COMMAND, ex.extensionSettings().getName())))));
                extensionNames.add(exData);
            }
            context.getSource().sendSuccess(() -> Component.literal("\nSettings").withStyle(style -> style.withBold(true).withUnderlined(true).withColor(ChatFormatting.GOLD)), false);
            for (Component t : extensionNames) {
                context.getSource().sendSuccess(() -> t, false);
            }
            return 1;
        });
        dispatcher.register(settingsCommand);
    }

    public static void onCreateDatabase() {
        db = new ServerDatabase();
        db.initializeConnection(minecraftServer.getWorldPath(LevelResource.ROOT).toString());
        db.createPlayerTable();
        extensionManager.getExtensions().forEach((k, e) -> e.onCreateDatabase(db.getConnection()));
        extensionManager.getExtensions().forEach((k, e) -> e.onCreateDatabase(minecraftServer.getWorldPath(LevelResource.ROOT).toString()));
    }

    public static void onServerStarted(MinecraftServer minecraftServer) {
        extensionManager.getExtensions().forEach((k, e) -> e.onServerStarted(minecraftServer));
    }

    public static void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess) {
        KahzerxServer.dispatcher = dispatcher;
        KahzerxServer.commandRegistryAccess = commandRegistryAccess;
    }

    public static void onStopServer() {
        extensionManager.getExtensions().forEach((k, e) -> e.onServerStop());
        db.close();
    }

    public static void onAutoSave() {
        extensionManager.getExtensions().forEach((k, e) -> e.onAutoSave());
    }

    public static void onAutoSave(MinecraftServer server) {
        extensionManager.getExtensions().forEach((k, e) -> e.onAutoSave(server));
    }

    public static void onPlayerJoined(ServerPlayer player) {
        db.getQuery().insertPlayerUUID(player.getStringUUID(), player.getName().getString());
        extensionManager.getExtensions().forEach((k, e) -> e.onPlayerJoined(player));
    }

    public static void onPlayerConnected(ServerPlayer player) {
        extensionManager.getExtensions().forEach((k, e) -> e.onPlayerConnected(player));
    }

    public static void onPlayerLeft(ServerPlayer player) {
        extensionManager.getExtensions().forEach((k, e) -> e.onPlayerLeft(player));
    }

    public static void onPlayerDied(ServerPlayer player) {
        extensionManager.getExtensions().forEach((k, e) -> e.onPlayerDied(player));
    }

    public static void onPlayerBreakBlock(ServerPlayer player, Level world, BlockPos pos) {
        extensionManager.getExtensions().forEach((k, e) -> e.onPlayerBreakBlock(player, world, pos));
    }

    public static void onPlayerPlaceBlock(ServerPlayer player, Level world, BlockPos pos) {
        extensionManager.getExtensions().forEach((k, e) -> e.onPlayerPlaceBlock(player, world, pos));
    }

    public static void onChatMessage(ServerPlayer player, String chatMessage) {
        extensionManager.getExtensions().forEach((k, e) -> e.onChatMessage(player, chatMessage));
    }

    public static void onCommand(ServerPlayer player, String command) {
        extensionManager.getExtensions().forEach((k, e) -> e.onCommand(player, command));
    }

    public static void onAdvancement(String advancement) {
        extensionManager.getExtensions().forEach((k, e) -> e.onAdvancement(advancement));
    }

    public static void onClick(ServerPlayer player) {
        extensionManager.getExtensions().forEach((k, e) -> e.onClick(player));
    }

    public static void onSleep(ServerPlayer player) {
        extensionManager.getExtensions().forEach((k, e) -> e.onPlayerSleep(player));
    }

    public static void onWakeUp(ServerPlayer player) {
        extensionManager.getExtensions().forEach((k, e) -> e.onPlayerWakeUp(player));
    }

    public static void onTick(MinecraftServer server) {
        if (server.getTickCount() < 20) {
            return;
        }
        extensionManager.getExtensions().forEach((k, e) -> e.onTick(server));
    }
}
