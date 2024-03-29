package com.kahzerx.kahzerxmod.extensions.discordExtension.discordAdminToolsExtension;

import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordGenericExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.commands.BanCommand;
import com.kahzerx.kahzerxmod.extensions.discordExtension.commands.ExaddCommand;
import com.kahzerx.kahzerxmod.extensions.discordExtension.commands.ExremoveCommand;
import com.kahzerx.kahzerxmod.extensions.discordExtension.commands.PardonCommand;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension.DiscordExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistExtension.DiscordWhitelistExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.utils.DiscordUtils;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;

import static net.minecraft.command.CommandSource.suggestMatching;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DiscordAdminToolsExtension extends DiscordGenericExtension implements Extensions {
    private DiscordExtension discordExtension;
    private DiscordWhitelistExtension discordWhitelistExtension;

    private ExtensionManager em = null;

    private final BanCommand banCommand = new BanCommand();
    private final PardonCommand pardonCommand = new PardonCommand();
    private final ExaddCommand exaddCommand = new ExaddCommand();
    private final ExremoveCommand exremoveCommand = new ExremoveCommand();

    public DiscordAdminToolsExtension(HashMap<String, String> fileSettings) {
        super(new DiscordAdminToolsSettings(fileSettings, "discordAdminTools", "Enables !ban, !pardon, !exadd, !exremove on discord AdminChats."));
        this.addCommands(this.banCommand, this.pardonCommand, this.exaddCommand, this.exremoveCommand);
    }

    @Override
    public void onExtensionsReady(ExtensionManager em) {
        this.em = em;
        this.discordWhitelistExtension = (DiscordWhitelistExtension) em.getExtensions().get("discordWhitelist");
        this.discordExtension = (DiscordExtension) em.getExtensions().get("discord");
        if (this.extensionSettings().isEnabled() && (!this.discordWhitelistExtension.extensionSettings().isEnabled() || !this.discordExtension.extensionSettings().isEnabled())) {
            this.extensionSettings().setEnabled(false);
            em.saveSettings();
        }
    }

    @Override
    public void onServerStarted(MinecraftServer minecraftServer) {
        this.getDiscordExtension().getBot().addExtensions(this);
    }

    public DiscordExtension getDiscordExtension() {
        return discordExtension;
    }

    public ExtensionManager getEm() {
        return em;
    }

    @Override
    public DiscordAdminToolsSettings extensionSettings() {
        return (DiscordAdminToolsSettings) this.getSettings();
    }

    @Override
    public void onExtensionEnabled(ServerCommandSource source) {}

    @Override
    public void onExtensionDisabled(ServerCommandSource source) {}

    @Override
    public boolean processCommands(MessageReceivedEvent event, String message, MinecraftServer server) {
        CommandFound commandFound = this.findValidCommand(event, message, this.extensionSettings().getAdminChats(), this.extensionSettings().isShouldFeedback(), this.getDiscordExtension().extensionSettings().getPrefix());
        if (!commandFound.found()) {
            return false;
        }
        if (commandFound.command() != null) {
            commandFound.command().executeCommand(event, server, this.getEm());
            return true;
        }
        return false;
    }

    @Override
    public void settingsCommand(LiteralArgumentBuilder<ServerCommandSource> builder) {
        builder.
                then(literal("shouldFeedback").
                        then(argument("feedback", BoolArgumentType.bool()).
                                executes(context -> {
                                    this.extensionSettings().setShouldFeedback(BoolArgumentType.getBool(context, "feedback"));
                                    context.getSource().sendFeedback(() -> this.getBooleanSettingMessage(true, this.extensionSettings().isShouldFeedback(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "shouldFeedback"), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal("\n" + this.extensionSettings().getName() + "/shouldFeedback\n").styled(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("The bot should respond to user commands\n", Formatting.GRAY).styled(style -> style.withBold(false))).
                                    append(this.getBooleanSettingMessage(false, this.extensionSettings().isShouldFeedback(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "shouldFeedback")), false);
                            return 1;
                        })).
                then(literal("adminChats").
                        then(literal("add").
                                then(argument("chatID", LongArgumentType.longArg()).
                                        suggests((c, b) -> suggestMatching(new String[]{"1234"}, b)).
                                        executes(context -> {
                                            long chat = LongArgumentType.getLong(context, "chatID");
                                            if (this.extensionSettings().getAdminChats().contains(chat)) {
                                                context.getSource().sendFeedback(() -> MarkEnum.CROSS.appendText(this.formatLongID("The chat ID ", chat, " was already on the list", true, false, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "adminChats")), false);
                                            } else {
                                                this.extensionSettings().addAdminChatID(chat);
                                                context.getSource().sendFeedback(() -> MarkEnum.TICK.appendText(this.formatLongID("The chat with ID ", chat, " has been", true, true, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "adminChats")), false);
                                                this.em.saveSettings();
                                            }
                                            return 1;
                                        }))).
                        then(literal("remove").
                                then(argument("chatID", LongArgumentType.longArg()).
                                        suggests((c, b) -> suggestMatching(this.extensionSettings().getAdminChats().stream().map(Object::toString), b)).
                                        executes(context -> {
                                            long chat = LongArgumentType.getLong(context, "chatID");
                                            if (this.extensionSettings().getAdminChats().contains(chat)) {
                                                this.extensionSettings().removeAdminChatID(chat);
                                                context.getSource().sendFeedback(() -> MarkEnum.TICK.appendText(this.formatLongID("The chat with ID ", chat, " has been", false, true, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "adminChats")), false);
                                                this.em.saveSettings();
                                            } else {
                                                context.getSource().sendFeedback(() -> MarkEnum.CROSS.appendText(this.formatLongID("The chat ID ", chat, " does not exist!", false, false, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "adminChats")), false);
                                            }
                                            return 1;
                                        }))).
                        then(literal("list").
                                executes(context -> {
                                    MutableText chats = Text.literal("");
                                    int chatCount = this.extensionSettings().getAdminChats().size();
                                    if (chatCount == 0) {
                                        chats.append(Text.literal("Not set!").styled(style -> style.
                                                withColor(Formatting.RED).
                                                withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to add!"))).
                                                withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s adminChats add ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName())))));
                                    } else {
                                        chats.
                                                append(Text.literal("[+]").styled(style -> style.
                                                        withColor(Formatting.GREEN).
                                                        withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to add!"))).
                                                        withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s adminChats add ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName()))))).
                                                append(Text.literal(" ")).
                                                append(Text.literal("[-]\n").styled(style -> style.
                                                        withColor(Formatting.RED).
                                                        withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to remove!"))).
                                                        withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s adminChats remove ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName())))));
                                        for (int i = 0; i < chatCount; i++) {
                                            long chat = this.extensionSettings().getAdminChats().get(i);
                                            chats.
                                                    append(MarkEnum.DOT.appendText(Text.literal(String.format("%d", chat)).styled(style -> style.
                                                            withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(String.format("Click to copy %d", chat)))).
                                                            withBold(false).
                                                            withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.format("%d", chat)))), Formatting.GRAY)).
                                                    append(Text.literal(" ")).
                                                    append(MarkEnum.CROSS.getFormattedIdentifier().styled(style -> style.
                                                            withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(String.format("Click to delete %d", chat)))).
                                                            withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %s adminChats remove %d", this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), chat))))).
                                                    append(i == chatCount-1 ? Text.literal("") : Text.literal("\n"));
                                        }
                                    }
                                    context.getSource().sendFeedback(() -> Text.literal("\n" + this.extensionSettings().getName() + "/adminChats/list" + "\n").styled(style -> style.withBold(true)).
                                            append(chats), false);
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal("\n" + this.extensionSettings().getName() + "/adminChats\n").styled(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("ChatIDs where !ban, !pardon, !exadd and !exremove work\n", Formatting.GRAY).styled(style -> style.withBold(false))).
                                    append(Text.literal("[Chats]").styled(style -> style.
                                            withColor(Formatting.DARK_GRAY).
                                            withUnderline(true).
                                            withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to display the already added adminChats chat IDs"))).
                                            withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %s adminChats list", this.em.getSettingsBaseCommand(), this.extensionSettings().getName()))))), false);
                            return 1;
                        }));
    }
}
