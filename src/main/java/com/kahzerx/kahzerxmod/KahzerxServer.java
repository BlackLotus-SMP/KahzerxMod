package com.kahzerx.kahzerxmod;

import com.kahzerx.kahzerxmod.database.ServerDatabase;
import com.kahzerx.kahzerxmod.utils.FileUtils;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

import static net.minecraft.server.command.CommandManager.literal;

public class KahzerxServer {
    public static MinecraftServer minecraftServer;
    public static ServerDatabase db = new ServerDatabase();
    public static CommandDispatcher<ServerCommandSource> dispatcher;
    public static CommandRegistryAccess commandRegistryAccess;
    private static final String SETTINGS_BASE_COMMAND = "KSettings";
    private static final ExtensionManager extensionManager = new ExtensionManager(SETTINGS_BASE_COMMAND);

    public static void onRunServer(MinecraftServer minecraftServer) {
        KahzerxServer.minecraftServer = minecraftServer;
        extensionManager.loadExtensions(FileUtils.loadConfig(minecraftServer.getSavePath(WorldSavePath.ROOT).toString()));
        extensionManager.getExtensions().forEach((k, e) -> e.onExtensionsReady(extensionManager));
        extensionManager.getExtensions().forEach((k, e) -> e.onServerRun(minecraftServer));

        extensionManager.saveSettings();

        extensionManager.getExtensions().forEach((k, e) -> e.onRegisterCommands(dispatcher));
        extensionManager.getExtensions().forEach((k, e) -> e.onRegisterCommands(dispatcher, commandRegistryAccess));

        // TODO command of reload, from file, check diffs and apply
        LiteralArgumentBuilder<ServerCommandSource> settingsCommand = literal(SETTINGS_BASE_COMMAND).
                requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2));  // TODO this has to be customizable for CMPs
        for (Extensions ex : extensionManager.getExtensions().values()) {
            LiteralArgumentBuilder<ServerCommandSource> extensionSubCommand = literal(ex.extensionSettings().getName());
            extensionSubCommand.
                    then(literal("enable").
                            executes(context -> {
                                if (ex.extensionSettings().isEnabled()) {
                                    context.getSource().sendFeedback(MarkEnum.CROSS.appendMessage(ex.extensionSettings().getName() + " extension already enabled"), false);
                                    return 1;
                                }
                                ex.extensionSettings().setEnabled(true);
                                ex.onExtensionEnabled();
                                extensionManager.saveSettings();
                                context.getSource().sendFeedback(MarkEnum.TICK.appendMessage(ex.extensionSettings().getName() + " extension enabled"), false);  // TODO color enabled
                                return 1;
                            })).
                    then(literal("disable").
                            executes(context -> {
                                if (!ex.extensionSettings().isEnabled()) {
                                    context.getSource().sendFeedback(MarkEnum.CROSS.appendMessage(ex.extensionSettings().getName() + " extension already disabled"), false);
                                    return 1;
                                }
                                ex.extensionSettings().setEnabled(false);
                                ex.onExtensionDisabled();
                                extensionManager.saveSettings();
                                context.getSource().sendFeedback(MarkEnum.TICK.appendMessage(ex.extensionSettings().getName() + " extension disabled"), false);
                                return 1;
                            })).
                    executes(context -> {
                        // TODO click on status to enable or disable maybe?
                        context.getSource().sendFeedback(() -> Text.literal("\n" + ex.extensionSettings().getName() + "\n").styled(style -> style.withBold(true)).
                                        append(MarkEnum.INFO.appendMsg(ex.extensionSettings().getDescription() + "\n", Formatting.GRAY).styled(style -> style.withBold(false))).
                                        append(Text.literal("Enabled: ").styled(style -> style.withBold(false).withColor(Formatting.WHITE))).
                                        append(Text.literal(String.format("%b", ex.extensionSettings().isEnabled())).styled(style -> style.withBold(false).withColor(ex.extensionSettings().isEnabled() ? Formatting.GREEN : Formatting.RED))), false);
                        return 1;
                    });
            ex.settingsCommand(extensionSubCommand);  // Otros ajustes por si fueran necesarios para las extensiones más complejas.
            settingsCommand.then(extensionSubCommand);
        }
        settingsCommand.executes(context -> {
            List<MutableText> extensionNames = new ArrayList<>();
            for (Extensions ex : extensionManager.getExtensions().values()) {
                MutableText exData = MarkEnum.DOT.appendText(Text.literal(ex.extensionSettings().getName() + " | ").styled(
                        style -> style.
                                withBold(false).
                                withUnderline(false).
                                withColor(Formatting.WHITE).
                                withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(ex.extensionSettings().getDescription())))));  // TODO click event to show metadata
                exData.append(Text.literal("true").styled(
                        style -> style.
                                withBold(false).
                                withColor(ex.extensionSettings().isEnabled() ? Formatting.GREEN : Formatting.GRAY).
                                withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(String.format("Enable %s", ex.extensionSettings().getName())))).
                                withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/KSettings %s enable", ex.extensionSettings().getName())))));
                exData.append(Text.literal(" ").styled(
                        style -> style.
                                withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(""))).
                                withUnderline(false)));
                exData.append(Text.literal("false").styled(
                        style -> style.
                                withBold(false).
                                withColor(!ex.extensionSettings().isEnabled() ? Formatting.RED : Formatting.GRAY).
                                withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(String.format("Disable %s", ex.extensionSettings().getName())))).
                                withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/KSettings %s disable", ex.extensionSettings().getName())))));
                extensionNames.add(exData);
            }
            context.getSource().sendFeedback(() -> Text.literal("\nSettings").styled(style -> style.withBold(true).withUnderline(true).withColor(Formatting.GOLD)), false);
            for (Text t : extensionNames) {
                context.getSource().sendFeedback(() -> t, false);
            }
            return 1;
        });
        dispatcher.register(settingsCommand);
    }

    public static void onCreateDatabase() {
        db = new ServerDatabase();
        db.initializeConnection(minecraftServer.getSavePath(WorldSavePath.ROOT).toString());
        db.createPlayerTable();
        extensionManager.getExtensions().forEach((k, e) -> e.onCreateDatabase(db.getConnection()));
        extensionManager.getExtensions().forEach((k, e) -> e.onCreateDatabase(minecraftServer.getSavePath(WorldSavePath.ROOT).toString()));
    }

    public static void onServerStarted(MinecraftServer minecraftServer) {
        extensionManager.getExtensions().forEach((k, e) -> e.onServerStarted(minecraftServer));
    }

    public static void onRegisterCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
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

    public static void onPlayerJoined(ServerPlayerEntity player) {
        db.getQuery().insertPlayerUUID(player.getUuidAsString(), player.getName().getString());
        extensionManager.getExtensions().forEach((k, e) -> e.onPlayerJoined(player));
    }

    public static void onPlayerConnected(ServerPlayerEntity player) {
        extensionManager.getExtensions().forEach((k, e) -> e.onPlayerConnected(player));
    }

    public static void onPlayerLeft(ServerPlayerEntity player) {
        extensionManager.getExtensions().forEach((k, e) -> e.onPlayerLeft(player));
    }

    public static void onPlayerDied(ServerPlayerEntity player) {
        extensionManager.getExtensions().forEach((k, e) -> e.onPlayerDied(player));
    }

    public static void onPlayerBreakBlock(ServerPlayerEntity player, World world, BlockPos pos) {
        extensionManager.getExtensions().forEach((k, e) -> e.onPlayerBreakBlock(player, world, pos));
    }

    public static void onPlayerPlaceBlock(ServerPlayerEntity player, World world, BlockPos pos) {
        extensionManager.getExtensions().forEach((k, e) -> e.onPlayerPlaceBlock(player, world, pos));
    }

    public static void onChatMessage(ServerPlayerEntity player, String chatMessage) {
        extensionManager.getExtensions().forEach((k, e) -> e.onChatMessage(player, chatMessage));
    }

    public static void onCommand(ServerPlayerEntity player, String command) {
        extensionManager.getExtensions().forEach((k, e) -> e.onCommand(player, command));
    }

    public static void onAdvancement(String advancement) {
        extensionManager.getExtensions().forEach((k, e) -> e.onAdvancement(advancement));
    }

    public static void onClick(ServerPlayerEntity player) {
        extensionManager.getExtensions().forEach((k, e) -> e.onClick(player));
    }

    public static void onSleep(ServerPlayerEntity player) {
        extensionManager.getExtensions().forEach((k, e) -> e.onPlayerSleep(player));
    }

    public static void onWakeUp(ServerPlayerEntity player) {
        extensionManager.getExtensions().forEach((k, e) -> e.onPlayerWakeUp(player));
    }

    public static void onTick(MinecraftServer server) {
        if (server.getTicks() < 20) {
            return;
        }
        extensionManager.getExtensions().forEach((k, e) -> e.onTick(server));
    }
}
