package com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension;

import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordBot;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordGenericExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordSendCommand;
import com.kahzerx.kahzerxmod.extensions.discordExtension.EventResponse;
import com.kahzerx.kahzerxmod.extensions.discordExtension.commands.OnlineCommand;
import com.kahzerx.kahzerxmod.extensions.discordExtension.utils.DiscordUtils;
import com.kahzerx.kahzerxmod.klone.KlonePlayerEntity;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.kahzerx.kahzerxmod.utils.PlayerUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

import static net.minecraft.commands.SharedSuggestionProvider.suggest;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class DiscordExtension extends DiscordGenericExtension implements Extensions {
    private static final Logger LOGGER = LogManager.getLogger();
    private ExtensionManager em = null;
    private DiscordBot bot;
    private final OnlineCommand onlineCommand = new OnlineCommand();

    public DiscordExtension(HashMap<String, String> fileSettings) {
        super(new DiscordSettings(fileSettings, "discord", "Connects minecraft chat + some events with a discord chat (chatbridge). Prefix is necessary if you want crossServerChat to work properly and not having duplicated messages."));
        this.addCommands(onlineCommand);
    }

    @Override
    public void onExtensionsReady(ExtensionManager em) {
        this.em = em;
    }

    @Override
    public void onServerRun(MinecraftServer minecraftServer) {
        this.bot = new DiscordBot(minecraftServer, this);
        if (!extensionSettings().isEnabled()) {
            return;
        }
        if (extensionSettings().getPrefix().equals("")) {
            extensionSettings().setCrossServerChat(false);
            this.em.saveSettings();
        }
        EventResponse resp = this.getBot().start();
        if (!resp.success()) {
            LOGGER.error("Unable to start discord extension!");
            LOGGER.error(resp.message());
            this.extensionSettings().setEnabled(false);
            this.em.saveSettings();
            this.onExtensionDisabled(null);
        }
    }

    @Override
    protected boolean processCommands(MessageReceivedEvent event, String message, MinecraftServer server) {
        CommandFound commandFound = this.findValidCommand(event, message, this.extensionSettings().getAllowedChats(), this.extensionSettings().isShouldFeedback(), this.extensionSettings().getPrefix());
        if (!commandFound.found()) {
            return false;
        }
        if (commandFound.command() != null) {
            commandFound.command().executeCommand(event, server, this.getEm());
            return true;
        }
        return false;
    }

    public DiscordBot getBot() {
        return bot;
    }

    public ExtensionManager getEm() {
        return em;
    }

    @Override
    public void onServerStarted(MinecraftServer minecraftServer) {
        this.getBot().addExtensions(this);
        this.getBot().sendSysMessage("**Server is ON**", this.extensionSettings().getPrefix());
    }

    @Override
    public void onServerStop() {
        this.getBot().stop();
    }

    // TODO messages with translation keys
    @Override
    public void onPlayerJoined(ServerPlayer player) {
        boolean isBot = player.getClass() == KlonePlayerEntity.class;
        String msg = ":arrow_right: **" + player.getName().getString().replace("_", "\\_") + (isBot ? " [Bot]" : "") + " joined the game!**";
        this.getBot().sendSysMessage(msg, this.extensionSettings().getPrefix());
    }

    @Override
    public void onPlayerLeft(ServerPlayer player) {
        boolean isBot = player.getClass() == KlonePlayerEntity.class;
        String msg = ":arrow_left: **" + player.getName().getString().replace("_", "\\_") + (isBot ? " [Bot]" : "") + " left the game!**";
        this.getBot().sendSysMessage(msg, this.extensionSettings().getPrefix());
    }

    @Override
    public void onPlayerDied(ServerPlayer player) {
        boolean isBot = player.getClass() == KlonePlayerEntity.class;
        String msg = ":skull_crossbones: **" + player.getCombatTracker().getDeathMessage().getString().replace("_", "\\_") + (isBot ? " [Bot]" : "") + "**";
        this.getBot().sendSysMessage(msg, this.extensionSettings().getPrefix());
    }

    @Override
    public void onChatMessage(ServerPlayer player, String chatMessage) {
        if (chatMessage.startsWith("/me ") || !chatMessage.startsWith("/")) {
            this.getBot().sendChatMessage(player, chatMessage, this.extensionSettings().getPrefix());
        }
    }

    @Override
    public void onAdvancement(String advancement) {
        String msg = ":confetti_ball: **" + advancement + "**";
        this.getBot().sendSysMessage(msg, this.extensionSettings().getPrefix());
    }

    @Override
    public DiscordSettings extensionSettings() {
        return (DiscordSettings) this.getSettings();
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        new DiscordSendCommand().register(dispatcher, this);
    }

    @Override
    public void onExtensionEnabled(CommandSourceStack source) {
        EventResponse resp = this.getBot().start();
        if (!resp.success()) {
            LOGGER.error("Unable to start discord extension!");
            source.sendSuccess(() -> MarkEnum.CROSS.appendMsg(resp.message()), false);
            this.extensionSettings().setEnabled(false);
            this.em.saveSettings();
            this.onExtensionDisabled(source);
            return;
        }
        this.getBot().sendSysMessage("**Server is ON**", this.extensionSettings().getPrefix());
        PlayerUtils.reloadCommands();
    }

    // TODO update whitelist, admintools & sync if this gets disabled!

    @Override
    public void onExtensionDisabled(CommandSourceStack source) {
        this.getBot().stop();
        PlayerUtils.reloadCommands();
    }

    @Override
    public void settingsCommand(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.  // TODO Interact with the description and add functionality maybe other than just if its enabled and the description
                then(literal("token").
                        then(argument("token", StringArgumentType.string()).
                                suggests((c, b) -> suggest(new String[]{"0"}, b)).
                                executes(context -> {
                                    String tok = StringArgumentType.getString(context, "token");
                                    if (tok.equals(this.extensionSettings().getToken())) {
                                        context.getSource().sendSuccess(() -> MarkEnum.CROSS.appendMsg("This discord token is already set!"), false);
                                        return 1;
                                    }
                                    boolean wasRunning = this.extensionSettings().isEnabled();
                                    if (wasRunning) {
                                        this.getBot().stop();
                                        context.getSource().sendSuccess(() -> MarkEnum.INFO.appendMsg("Stopping the discord bot..."), false);
                                    }
                                    if (tok.equals("0")) {
                                        this.extensionSettings().setToken("");
                                        context.getSource().sendSuccess(() -> MarkEnum.TICK.appendMsg("Discord Bot disabled!"), false);
                                        return 1;
                                    }
                                    this.extensionSettings().setToken(tok);
                                    context.getSource().sendSuccess(() -> this.getStringSettingMessage(true, "token", this.extensionSettings().getToken(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName()), false);
                                    this.em.saveSettings();
                                    if (wasRunning) {
                                        context.getSource().sendSuccess(() -> MarkEnum.INFO.appendMsg("Restarting the discord bot..."), false);
                                        EventResponse resp = this.getBot().start();
                                        if (!resp.success()) {
                                            LOGGER.error("Unable to start discord extension!");
                                            context.getSource().sendSuccess(() -> MarkEnum.CROSS.appendMsg(resp.message()), false);
                                            this.extensionSettings().setEnabled(false);
                                            this.em.saveSettings();
                                            this.onExtensionDisabled(context.getSource());
                                            return 1;
                                        }
                                    }
                                    return 1;
                                }))).
                then(literal("channelID").
                        then(argument("chatChannelID", LongArgumentType.longArg()).
                                suggests((c, b) -> suggest(new String[]{"0"}, b)).
                                executes(context -> {
                                    this.extensionSettings().setChatChannelID(LongArgumentType.getLong(context, "chatChannelID"));
                                    this.em.saveSettings();
                                    if (this.extensionSettings().getChatChannelID() == 0) {
                                        this.getBot().stop();
                                        context.getSource().sendSuccess(() -> MarkEnum.INFO.appendMsg("Stopping the discord bot..."), false);
                                    }
                                    context.getSource().sendSuccess(() -> this.getLongSettingMessage(true, "channelID", this.extensionSettings().getChatChannelID(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName()), false);
                                    EventResponse resp = this.getBot().onUpdateChannelID();
                                    if (!resp.success()) {
                                        context.getSource().sendSuccess(() -> MarkEnum.CROSS.appendMsg(resp.message()), false);
                                        this.onExtensionDisabled(context.getSource());
                                    }
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendSuccess(() -> Component.literal("\n" + this.extensionSettings().getName() + "/channelID\n").withStyle(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("channelID, where chat messages should go to\n", ChatFormatting.GRAY).withStyle(style -> style.withBold(false))).
                                    append(this.getLongSettingMessage(false, "channelID", this.extensionSettings().getChatChannelID(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName())), false);
                            return 1;
                        })).
                then(literal("shouldFeedback").
                        then(argument("feedback", BoolArgumentType.bool()).
                                executes(context -> {
                                    this.extensionSettings().setShouldFeedback(BoolArgumentType.getBool(context, "feedback"));
                                    context.getSource().sendSuccess(() -> this.getBooleanSettingMessage(true, this.extensionSettings().isShouldFeedback(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "shouldFeedback"), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendSuccess(() -> Component.literal("\n" + this.extensionSettings().getName() + "/shouldFeedback\n").withStyle(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("The bot should respond to user commands\n", ChatFormatting.GRAY).withStyle(style -> style.withBold(false))).
                                    append(this.getBooleanSettingMessage(false, this.extensionSettings().isShouldFeedback(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "shouldFeedback")), false);
                            return 1;
                        })).
                then(literal("prefix").
                        then(argument("prefix", StringArgumentType.string()).
                                suggests((c, b) -> suggest(new String[]{"SMP", "CMP"}, b)).
                                executes(context -> {
                                    String prefix = StringArgumentType.getString(context, "prefix");
                                    if (prefix.equals("0")) {
                                        prefix = "";
                                    }
                                    this.extensionSettings().setPrefix(prefix);
                                    context.getSource().sendSuccess(() -> this.getStringSettingMessage(true, "prefix", this.extensionSettings().getPrefix(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName()), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendSuccess(() -> Component.literal("\n" + this.extensionSettings().getName() + "/prefix\n").withStyle(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("Server prefix/name\n", ChatFormatting.GRAY).withStyle(style -> style.withBold(false))).
                                    append(this.getStringSettingMessage(false, "prefix", this.extensionSettings().getPrefix(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName())), false);
                            return 1;
                        })).
                then(literal("commandPrefix").
                        then(argument("prefix", StringArgumentType.word()).  // TODO can't use special chars...
                                suggests((c, b) -> suggest(new String[]{"!", ";", "."}, b)).
                                executes(context -> {  // TODO 0 is not "to disable"
                                    String prefix = StringArgumentType.getString(context, "prefix");
                                    this.extensionSettings().setCommandPrefix(prefix);
                                    this.getBot().updateCommandPrefix(prefix);
                                    context.getSource().sendSuccess(() -> this.getStringSettingMessage(true, "commandPrefix", this.extensionSettings().getCommandPrefix(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName()), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendSuccess(() -> Component.literal("\n" + this.extensionSettings().getName() + "/commandPrefix\n").withStyle(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("Discord command prefix\n", ChatFormatting.GRAY).withStyle(style -> style.withBold(false))).
                                    append(this.getStringSettingMessage(false, "commandPrefix", this.extensionSettings().getCommandPrefix(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName())), false);
                            return 1;
                        })).
                then(literal("crossServerChat").
                        then(argument("enabled", BoolArgumentType.bool()).
                                executes(context -> {
                                    if (!this.extensionSettings().isCrossServerChat() && this.extensionSettings().getPrefix().equals("")) {
                                        context.getSource().sendSuccess(() -> MarkEnum.CROSS.appendText(Component.literal("You need to set a ").
                                                append(Component.literal("prefix").withStyle(style -> style.
                                                        withColor(ChatFormatting.DARK_GREEN).
                                                        withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to set a prefix"))).
                                                        withClickEvent(new ClickEvent.SuggestCommand(String.format("/%s %s prefix ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName()))))).
                                                append(Component.literal(" first!"))), false);
                                        return 1;
                                    }
                                    extensionSettings().setCrossServerChat(BoolArgumentType.getBool(context, "enabled"));
                                    context.getSource().sendSuccess(() -> this.getBooleanSettingMessage(true, this.extensionSettings().isCrossServerChat(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "crossServerChat"), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendSuccess(() -> Component.literal("\n" + this.extensionSettings().getName() + "/crossServerChat\n").withStyle(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("Same bot(same token) on same chatID and different prefix on many servers will connect their chats\n", ChatFormatting.GRAY).withStyle(style -> style.withBold(false))).
                                    append(this.getBooleanSettingMessage(false, this.extensionSettings().isCrossServerChat(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "crossServerChat")), false);
                            return 1;
                        })).
                then(literal("allowedChats").
                        then(literal("add").
                                then(argument("chatID", LongArgumentType.longArg()).
                                        executes(context -> {
                                            long chat = LongArgumentType.getLong(context, "chatID");
                                            if (this.extensionSettings().getAllowedChats().contains(chat)) {
                                                context.getSource().sendSuccess(() -> MarkEnum.CROSS.appendText(this.formatLongID("The chat ID ", chat, " was already on the list", true, false, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "allowedChats")), false);
                                            } else {
                                                this.extensionSettings().addAllowedChatID(chat);
                                                context.getSource().sendSuccess(() -> MarkEnum.TICK.appendText(this.formatLongID("The chat with ID ", chat, " has been", true, true, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "allowedChats")), false);
                                                this.em.saveSettings();
                                            }
                                            return 1;
                                        }))).
                        then(literal("remove").
                                then(argument("chatID", LongArgumentType.longArg()).
                                        executes(context -> {
                                            long chat = LongArgumentType.getLong(context, "chatID");
                                            if (this.extensionSettings().getAllowedChats().contains(chat)) {
                                                this.extensionSettings().removeAllowedChatID(chat);
                                                context.getSource().sendSuccess(() -> MarkEnum.TICK.appendText(this.formatLongID("The chat with ID ", chat, " has been", false, true, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "allowedChats")), false);
                                                this.em.saveSettings();
                                            } else {
                                                context.getSource().sendSuccess(() -> MarkEnum.CROSS.appendText(this.formatLongID("The chat ID ", chat, " does not exist!", false, false, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "allowedChats")), false);
                                            }
                                            return 1;
                                        }))).
                        then(literal("list").
                                executes(context -> {
                                    MutableComponent chats = Component.literal("");
                                    int chatCount = this.extensionSettings().getAllowedChats().size();
                                    if (chatCount == 0) {
                                        chats.append(Component.literal("Not set!").withStyle(style -> style.
                                                withColor(ChatFormatting.RED).
                                                withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to add!"))).
                                                withClickEvent(new ClickEvent.SuggestCommand(String.format("/%s %s allowedChats add ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName())))));
                                    } else {
                                        chats.
                                                append(Component.literal("[+]").withStyle(style -> style.
                                                        withColor(ChatFormatting.GREEN).
                                                        withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to add!"))).
                                                        withClickEvent(new ClickEvent.SuggestCommand(String.format("/%s %s allowedChats add ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName()))))).
                                                append(Component.literal(" ")).
                                                append(Component.literal("[-]\n").withStyle(style -> style.
                                                        withColor(ChatFormatting.RED).
                                                        withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to remove!"))).
                                                        withClickEvent(new ClickEvent.SuggestCommand(String.format("/%s %s allowedChats remove ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName())))));
                                        for (int i = 0; i < chatCount; i++) {
                                            long chat = this.extensionSettings().getAllowedChats().get(i);
                                            chats.
                                                    append(MarkEnum.DOT.appendText(Component.literal(String.format("%d", chat)).withStyle(style -> style.
                                                            withHoverEvent(new HoverEvent.ShowText(Component.literal(String.format("Click to copy %d", chat)))).
                                                            withBold(false).
                                                            withClickEvent(new ClickEvent.CopyToClipboard(String.format("%d", chat)))), ChatFormatting.GRAY)).
                                                    append(Component.literal(" ")).
                                                    append(MarkEnum.CROSS.getFormattedIdentifier().withStyle(style -> style.
                                                            withHoverEvent(new HoverEvent.ShowText(Component.literal(String.format("Click to delete %d", chat)))).
                                                            withClickEvent(new ClickEvent.RunCommand(String.format("/%s %s allowedChats remove %d", this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), chat))))).
                                                    append(i == chatCount-1 ? Component.literal("") : Component.literal("\n"));
                                        }
                                    }
                                    context.getSource().sendSuccess(() -> Component.literal("\n" + this.extensionSettings().getName() + "/allowedChats/list" + "\n").withStyle(style -> style.withBold(true)).
                                            append(chats), false);
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendSuccess(() -> Component.literal("\n" + this.extensionSettings().getName() + "/allowedChats\n").withStyle(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("Chats where !online work\n", ChatFormatting.GRAY).withStyle(style -> style.withBold(false))).
                                    append(Component.literal("[Chats]").withStyle(style -> style.
                                            withColor(ChatFormatting.DARK_GRAY).
                                            withUnderlined(true).
                                            withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to display the already added whitelist chat IDs"))).
                                            withClickEvent(new ClickEvent.RunCommand(String.format("/%s %s allowedChats list", this.em.getSettingsBaseCommand(), this.extensionSettings().getName()))))), false);
                            return 1;
                        }));
    }
}
