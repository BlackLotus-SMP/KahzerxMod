package com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension;

import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.KahzerxServer;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordGenericExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordListener;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordSendCommand;
import com.kahzerx.kahzerxmod.klone.KlonePlayerEntity;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.kahzerx.kahzerxmod.utils.PlayerUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DiscordExtension extends DiscordGenericExtension implements Extensions {
    private ExtensionManager em = null;

    public DiscordExtension(HashMap<String, String> fileSettings) {
        super(new DiscordSettings(fileSettings, "discord", "Connects minecraft chat + some events with a discord chat (chatbridge). Prefix is necessary if you want crossServerChat to work properly and not having duplicated messages."));
    }

    @Override
    public void onExtensionsReady(ExtensionManager em) {
        this.em = em;
    }

    @Override
    public void onServerRun(MinecraftServer minecraftServer) {
        if (!extensionSettings().isEnabled()) {
            return;
        }
        if (extensionSettings().getPrefix().equals("")) {
            extensionSettings().setCrossServerChat(false);
            this.em.saveSettings();
        }
        DiscordListener.start(minecraftServer, extensionSettings().getToken(), String.valueOf(extensionSettings().getChatChannelID()), this);
    }

    @Override
    public void onServerStarted(MinecraftServer minecraftServer) {
        DiscordListener.sendSysMessage("**Server is ON**", this.extensionSettings().getPrefix());
    }

    @Override
    public void onServerStop() {
        if (extensionSettings().isRunning()) {
            DiscordListener.stop();
        }
    }

    // TODO messages with translation keys
    @Override
    public void onPlayerJoined(ServerPlayerEntity player) {
        boolean isBot = player.getClass() == KlonePlayerEntity.class;
        String msg = ":arrow_right: **" + player.getName().getString().replace("_", "\\_") + (isBot ? " [Bot]" : "") + " joined the game!**";
        DiscordListener.sendSysMessage(msg, this.extensionSettings().getPrefix());
    }

    @Override
    public void onPlayerLeft(ServerPlayerEntity player) {
        boolean isBot = player.getClass() == KlonePlayerEntity.class;
        String msg = ":arrow_left: **" + player.getName().getString().replace("_", "\\_") + (isBot ? " [Bot]" : "") + " left the game!**";
        DiscordListener.sendSysMessage(msg, this.extensionSettings().getPrefix());
    }

    @Override
    public void onPlayerDied(ServerPlayerEntity player) {
        boolean isBot = player.getClass() == KlonePlayerEntity.class;
        String msg = ":skull_crossbones: **" + player.getDamageTracker().getDeathMessage().getString().replace("_", "\\_") + (isBot ? " [Bot]" : "") + "**";
        DiscordListener.sendSysMessage(msg, this.extensionSettings().getPrefix());
    }

    @Override
    public void onChatMessage(ServerPlayerEntity player, String chatMessage) {
        if (chatMessage.startsWith("/me ") || !chatMessage.startsWith("/")) {
            DiscordListener.sendChatMessage(player, chatMessage, this.extensionSettings().getPrefix());
        }
    }

    @Override
    public void onAdvancement(String advancement) {
        String msg = ":confetti_ball: **" + advancement + "**";
        DiscordListener.sendSysMessage(msg, this.extensionSettings().getPrefix());
    }

    @Override
    public DiscordSettings extensionSettings() {
        return (DiscordSettings) this.getSettings();
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        new DiscordSendCommand().register(dispatcher, this);
    }

    @Override
    public void onExtensionEnabled() {
        if (!extensionSettings().isEnabled()) {
            return;
        }
        DiscordListener.start(KahzerxServer.minecraftServer, extensionSettings().getToken(), String.valueOf(extensionSettings().getChatChannelID()), this);
        DiscordListener.sendSysMessage("**Server is ON**", this.extensionSettings().getPrefix());
        PlayerUtils.reloadCommands();
    }

    // TODO update whitelist, admintools & sync if this gets disabled!

    @Override
    public void onExtensionDisabled() {
        if (extensionSettings().isRunning()) {
            DiscordListener.stop();
        }
        this.extensionSettings().setRunning(false);
        PlayerUtils.reloadCommands();
    }

    // TODO ugly prints
    @Override
    public void settingsCommand(LiteralArgumentBuilder<ServerCommandSource> builder) {
        builder.  // TODO Interact with the description and add functionality maybe other than just if its enabled and the description
                then(literal("setBot").
                        then(argument("token", StringArgumentType.string()).
                                then(argument("chatChannelID", LongArgumentType.longArg()).
                                        executes(context -> {
                                            if (DiscordListener.chatbridge) {
                                                context.getSource().sendFeedback(() -> Text.literal("Stop the bot before you make any changes..."), false);
                                            } else {
                                                extensionSettings().setToken(StringArgumentType.getString(context, "token"));
                                                extensionSettings().setChatChannelID(LongArgumentType.getLong(context, "chatChannelID"));
                                                context.getSource().sendFeedback(() -> Text.literal("Done!"), false);
                                                this.em.saveSettings();
                                            }
                                            return 1;
                                        })))).
                then(literal("shouldFeedback").
                        then(argument("feedback", BoolArgumentType.bool()).
                                executes(context -> {
                                    extensionSettings().setShouldFeedback(BoolArgumentType.getBool(context, "feedback"));
                                    context.getSource().sendFeedback(() -> Text.literal("[shouldFeedback] > " + extensionSettings().isShouldFeedback() + "."), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal("[shouldFeedback] > " + extensionSettings().isShouldFeedback() + "."), false);
                            return 1;
                        })).
                then(literal("chatBridgePrefix").
                        then(argument("prefix", StringArgumentType.string()).
                                executes(context -> {
                                    extensionSettings().setPrefix(StringArgumentType.getString(context, "prefix"));
                                    context.getSource().sendFeedback(() -> Text.literal("[Prefix] > " + extensionSettings().getPrefix() + "."), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            String help = "Server prefix.";
                            context.getSource().sendFeedback(() -> Text.literal(help), false);
                            String prefix = extensionSettings().getPrefix();
                            context.getSource().sendFeedback(() -> Text.literal(prefix.equals("") ? "There is no prefix." : "[Prefix] > " + extensionSettings().getPrefix() + "."), false);
                            return 1;
                        })).
                then(literal("crossServerChat").
                        then(argument("enabled", BoolArgumentType.bool()).
                                executes(context -> {
                                    if (extensionSettings().getPrefix().equals("")) {
                                        context.getSource().sendFeedback(() -> Text.literal("You need to set a prefix!"), false);
                                        return 1;
                                    }
                                    extensionSettings().setCrossServerChat(BoolArgumentType.getBool(context, "enabled"));
                                    context.getSource().sendFeedback(() -> Text.literal("[CrossServerChat] > " + extensionSettings().isCrossServerChat() + "."), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            String help = "Same bot(same token) on same chatID and different prefix on many servers will connect their chats.";
                            context.getSource().sendFeedback(() -> Text.literal(help), false);
                            context.getSource().sendFeedback(() -> Text.literal("[CrossServerChat] > " + extensionSettings().isCrossServerChat() + "."), false);
                            return 1;
                        })).
                then(literal("allowedChats").
                        then(literal("add").
                                then(argument("chatID", LongArgumentType.longArg()).
                                        executes(context -> {
                                            long chat = LongArgumentType.getLong(context, "chatID");
                                            if (extensionSettings().getAllowedChats().contains(chat)) {
                                                context.getSource().sendFeedback(() -> MarkEnum.CROSS.appendText(this.formatLongID("The chat ID ", chat, " was already on the list", true, false, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "allowedChats")), false);
                                            } else {
                                                extensionSettings().addAllowedChatID(chat);
                                                context.getSource().sendFeedback(() -> MarkEnum.TICK.appendText(this.formatLongID("The chat with ID ", chat, " has been", true, true, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "allowedChats")), false);
                                                this.em.saveSettings();
                                            }
                                            return 1;
                                        }))).
                        then(literal("remove").
                                then(argument("chatID", LongArgumentType.longArg()).
                                        executes(context -> {
                                            long chat = LongArgumentType.getLong(context, "chatID");
                                            if (extensionSettings().getAllowedChats().contains(chat)) {
                                                this.extensionSettings().removeAllowedChatID(chat);
                                                context.getSource().sendFeedback(() -> MarkEnum.CROSS.appendText(this.formatLongID("The chat with ID ", chat, " has been", false, true, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "allowedChats")), false);
                                                this.em.saveSettings();
                                            } else {
                                                context.getSource().sendFeedback(() -> MarkEnum.TICK.appendText(this.formatLongID("The chat ID ", chat, " does not exist!", false, false, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "allowedChats")), false);
                                            }
                                            return 1;
                                        }))).
                        then(literal("list").
                                executes(context -> {
                                    MutableText chats = Text.literal("");
                                    int chatCount = this.extensionSettings().getAllowedChats().size();
                                    if (chatCount == 0) {
                                        chats.append(Text.literal("Not set!").styled(style -> style.
                                                withColor(Formatting.RED).
                                                withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to add!"))).
                                                withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s allowedChats add ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName())))));
                                    } else {
                                        chats.
                                                append(Text.literal("[+]").styled(style -> style.
                                                        withColor(Formatting.GREEN).
                                                        withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to add!"))).
                                                        withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s allowedChats add ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName()))))).
                                                append(Text.literal(" ")).
                                                append(Text.literal("[-]\n").styled(style -> style.
                                                        withColor(Formatting.RED).
                                                        withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to remove!"))).
                                                        withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s allowedChats remove ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName())))));
                                        for (int i = 0; i < chatCount; i++) {
                                            long chat = this.extensionSettings().getAllowedChats().get(i);
                                            chats.
                                                    append(MarkEnum.DOT.appendText(Text.literal(String.format("%d", chat)).styled(style -> style.
                                                            withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(String.format("Click to copy %d", chat)))).
                                                            withBold(false).
                                                            withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.format("%d", chat)))), Formatting.GRAY)).
                                                    append(Text.literal(" ")).
                                                    append(MarkEnum.CROSS.getFormattedIdentifier().styled(style -> style.
                                                            withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(String.format("Click to delete %d", chat)))).
                                                            withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %s allowedChats remove %d", this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), chat))))).
                                                    append(i == chatCount-1 ? Text.literal("") : Text.literal("\n"));
                                        }
                                    }
                                    context.getSource().sendFeedback(() -> Text.literal("\n" + this.extensionSettings().getName() + "/allowedChats/list" + "\n").styled(style -> style.withBold(true)).
                                            append(chats), false);
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal("\n" + this.extensionSettings().getName() + "/allowedChats\n").styled(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("Chats where !online work\n", Formatting.GRAY).styled(style -> style.withBold(false))).
                                    append(Text.literal("[Chats]").styled(style -> style.
                                            withColor(Formatting.DARK_GRAY).
                                            withUnderline(true).
                                            withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to display the already added whitelist chat IDs"))).
                                            withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %s allowedChats list", this.em.getSettingsBaseCommand(), this.extensionSettings().getName()))))), false);
                            return 1;
                        }));
    }
}
