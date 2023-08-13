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
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.HashMap;

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

    public DiscordExtension getDiscordExtension() {
        return discordExtension;
    }

    @Override
    public void onServerRun(MinecraftServer minecraftServer) {
        this.getDiscordExtension().getBot().addExtensions(this);
    }

    @Override
    public DiscordAdminToolsSettings extensionSettings() {
        return (DiscordAdminToolsSettings) this.getSettings();
    }

    @Override
    public void onExtensionEnabled() {
        this.getDiscordExtension().getBot().addExtensions(this);

    }

    @Override
    public void onExtensionDisabled() {}

    @Override
    public boolean processCommands(MessageReceivedEvent event, String message, MinecraftServer server) {
        if (!this.getSettings().isEnabled()) {
            return false;
        }
        if (!this.getDiscordExtension().getSettings().isEnabled()) {
            return false;
        }
        if (!this.discordWhitelistExtension.getSettings().isEnabled()) {
            return false;
        }
        if (!DiscordUtils.isAllowed(event.getChannel().getIdLong(), this.extensionSettings().getAdminChats())) {
            if (message.startsWith(this.banCommand.getCommandPrefix() + this.banCommand.getBody())
                    || message.startsWith(this.pardonCommand.getCommandPrefix() + this.pardonCommand.getBody())
                    || message.startsWith(this.exaddCommand.getCommandPrefix() + this.exaddCommand.getBody())
                    || message.startsWith(this.exremoveCommand.getCommandPrefix() + this.exremoveCommand.getBody())) {
                return true;
            }
        }
        if (message.startsWith(this.banCommand.getCommandPrefix() + this.banCommand.getBody() + " ")) {
            this.banCommand.execute(event, server, discordExtension.extensionSettings().getPrefix(), discordWhitelistExtension, this);
            return true;
        } else if (message.startsWith(this.pardonCommand.getCommandPrefix() + this.pardonCommand.getBody() + " ")) {
            this.pardonCommand.execute(event, server, discordExtension.extensionSettings().getPrefix(), discordWhitelistExtension, this);
            return true;
        } else if (message.startsWith(this.exaddCommand.getCommandPrefix() + this.exaddCommand.getBody() + " ")) {
            this.exaddCommand.execute(event, server, discordExtension.extensionSettings().getPrefix(), discordWhitelistExtension, this);
            return true;
        } else if (message.startsWith(this.exremoveCommand.getCommandPrefix() + this.exremoveCommand.getBody() + " ")) {
            this.exremoveCommand.execute(event, server, discordExtension.extensionSettings().getPrefix(), discordWhitelistExtension, this);
            return true;
        }
        return false;
    }

    // TODO refactor prints
    @Override
    public void settingsCommand(LiteralArgumentBuilder<ServerCommandSource> builder) {
        builder.
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
                then(literal("adminChats").
                        then(literal("add").
                                then(argument("chatID", LongArgumentType.longArg()).
                                        executes(context -> {
                                            if (extensionSettings().getAdminChats().contains(LongArgumentType.getLong(context, "chatID"))) {
                                                context.getSource().sendFeedback(() -> Text.literal("ID already added."), false);
                                            } else {
                                                extensionSettings().addAdminChatID(LongArgumentType.getLong(context, "chatID"));
                                                context.getSource().sendFeedback(() -> Text.literal("ID added."), false);
                                                this.em.saveSettings();
                                            }
                                            return 1;
                                        }))).
                        then(literal("remove").
                                then(argument("chatID", LongArgumentType.longArg()).
                                        executes(context -> {
                                            if (extensionSettings().getAdminChats().contains(LongArgumentType.getLong(context, "chatID"))) {
                                                extensionSettings().removeAdminChatID(LongArgumentType.getLong(context, "chatID"));
                                                context.getSource().sendFeedback(() -> Text.literal("ID removed."), false);
                                                this.em.saveSettings();
                                            } else {
                                                context.getSource().sendFeedback(() -> Text.literal("This ID doesn't exist."), false);
                                            }
                                            return 1;
                                        }))).
                        then(literal("list").
                                executes(context -> {
                                    context.getSource().sendFeedback(() -> Text.literal(extensionSettings().getAdminChats().toString()), false);
                                    return 1;
                                })).
                        executes(context -> {
                            String help = "ChatIDs where !ban, !pardon, !exadd and !exremove work.";
                            context.getSource().sendFeedback(() -> Text.literal(help), false);
                            return 1;
                        }));
    }
}
