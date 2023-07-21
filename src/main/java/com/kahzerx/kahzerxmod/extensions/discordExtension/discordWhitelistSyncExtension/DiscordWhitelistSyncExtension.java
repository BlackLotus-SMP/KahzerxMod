package com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistSyncExtension;

import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension.DiscordExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistExtension.DiscordWhitelistExtension;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DiscordWhitelistSyncExtension extends GenericExtension implements Extensions {
    private DiscordExtension discordExtension;
    private DiscordWhitelistExtension discordWhitelistExtension;

    private ExtensionManager em = null;

    private Timer timer;

    public DiscordWhitelistSyncExtension(HashMap<String, String> fileSettings) {
        super(new DiscordWhitelistSyncSettings(fileSettings, "discordWhitelistSync", "Check if people that did !add have a given discord role, if not they will get automatically removed from whitelist, useful for sub twitch role. The groupID is the ID of the discord server/guild. The aggressive mode will force whitelist and discord database have the same users so any player added with /whitelist add will get removed on autosave."));
    }

    @Override
    public void onExtensionsReady(ExtensionManager em) {
        this.em = em;
        this.discordExtension = (DiscordExtension) em.getExtensions().get("discord");
        this.discordWhitelistExtension = (DiscordWhitelistExtension) em.getExtensions().get("discordWhitelist");
        if (this.extensionSettings().isEnabled() && (!this.discordExtension.extensionSettings().isEnabled() || !this.discordWhitelistExtension.extensionSettings().isEnabled())) {
            this.extensionSettings().setEnabled(false);
            em.saveSettings();
        }
    }

    @Override
    public void onServerStarted(MinecraftServer minecraftServer) {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer.purge();
        }
        this.timer = new Timer("WHITELIST_SYNC");
        this.timer.schedule(new DiscordWhitelistSyncThread(minecraftServer, this.discordExtension, this.discordWhitelistExtension, this), 1_000, 60 * 60 * 1_000);
    }

    @Override
    public void onServerStop() {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer.purge();
        }
    }

    @Override
    public DiscordWhitelistSyncSettings extensionSettings() {
        return (DiscordWhitelistSyncSettings) this.getSettings();
    }

    @Override
    public void onExtensionEnabled() { }

    @Override
    public void onExtensionDisabled() { }

    @Override
    public void settingsCommand(LiteralArgumentBuilder<ServerCommandSource> builder) {
        builder.
                then(literal("notifyChatID").
                        then(argument("chatID", LongArgumentType.longArg()).
                                executes(context -> {
                                    extensionSettings().setNotifyChannelID(LongArgumentType.getLong(context, "chatID"));
                                    context.getSource().sendFeedback(() -> Text.literal("[notifyChatID] > " + extensionSettings().getNotifyChannelID() + "."), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            String help = "Channel where you get notified when someone gets removed from the whitelist.";
                            context.getSource().sendFeedback(() -> Text.literal(help), false);
                            context.getSource().sendFeedback(() -> Text.literal("[notifyChatID] > " + extensionSettings().getNotifyChannelID() + "."), false);
                            return 1;
                        })).
                then(literal("groupID").
                        then(argument("groupID", LongArgumentType.longArg()).
                                executes(context -> {
                                    extensionSettings().setGroupID(LongArgumentType.getLong(context, "groupID"));
                                    context.getSource().sendFeedback(() -> Text.literal("[groupID] > " + extensionSettings().getGroupID() + "."), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            String help = "ServerID, to know where to check members.";
                            context.getSource().sendFeedback(() -> Text.literal(help), false);
                            context.getSource().sendFeedback(() -> Text.literal("[groupID] > " + extensionSettings().getGroupID() + "."), false);
                            return 1;
                        })).
                then(literal("aggressive").
                        then(argument("aggressive", BoolArgumentType.bool()).
                                executes(context -> {
                                    extensionSettings().setAggressive(BoolArgumentType.getBool(context, "aggressive"));
                                    context.getSource().sendFeedback(() -> Text.literal("[aggressive] > " + extensionSettings().isAggressive() + "."), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            String help = "Full whitelist/database sync.";
                            context.getSource().sendFeedback(() -> Text.literal(help), false);
                            context.getSource().sendFeedback(() -> Text.literal("[aggressive] > " + extensionSettings().isAggressive() + "."), false);
                            return 1;
                        })).
                then(literal("validRoles").
                        then(literal("add").
                                then(argument("roleID", LongArgumentType.longArg()).
                                        executes(context -> {
                                            if (extensionSettings().getValidRoles().contains(LongArgumentType.getLong(context, "roleID"))) {
                                                context.getSource().sendFeedback(() -> Text.literal("ID already added."), false);
                                            } else {
                                                extensionSettings().addValidRoleID(LongArgumentType.getLong(context, "roleID"));
                                                context.getSource().sendFeedback(() -> Text.literal("ID added."), false);
                                                this.em.saveSettings();
                                            }
                                            return 1;
                                        }))).
                        then(literal("remove").
                                then(argument("roleID", LongArgumentType.longArg()).
                                        executes(context -> {
                                            if (extensionSettings().getValidRoles().contains(LongArgumentType.getLong(context, "roleID"))) {
                                                extensionSettings().removeValidRoleID(LongArgumentType.getLong(context, "roleID"));
                                                context.getSource().sendFeedback(() -> Text.literal("ID removed."), false);
                                                this.em.saveSettings();
                                            } else {
                                                context.getSource().sendFeedback(() -> Text.literal("This ID doesn't exist."), false);
                                            }
                                            return 1;
                                        }))).
                        then(literal("list").
                                executes(context -> {
                                    context.getSource().sendFeedback(() -> Text.literal(extensionSettings().getValidRoles().toString()), false);
                                    return 1;
                                })).
                        executes(context -> {
                            String help = "Role list that a member needs to have (at least one) so dont get kicked from the whitelist (ex: sub role).";
                            context.getSource().sendFeedback(() -> Text.literal(help), false);
                            return 1;
                        }));
    }
}
