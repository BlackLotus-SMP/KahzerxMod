package com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistSyncExtension;

import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension.DiscordExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistExtension.DiscordWhitelistExtension;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
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
                                    this.extensionSettings().setNotifyChannelID(LongArgumentType.getLong(context, "chatID"));
                                    context.getSource().sendFeedback(() -> this.getLongSettingMessage(true, "notifyChatID", this.extensionSettings().getNotifyChannelID()), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal("\n" + this.extensionSettings().getName() + "/" + "notifyChatID\n").styled(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("Channel where you get notified when someone gets removed from the whitelist\n", Formatting.GRAY).styled(style -> style.withBold(false))).
                                    append(this.getLongSettingMessage(false, "notifyChatID", this.extensionSettings().getNotifyChannelID())), false);
                            return 1;
                        })).
                then(literal("groupID").
                        then(argument("groupID", LongArgumentType.longArg()).
                                executes(context -> {
                                    extensionSettings().setGroupID(LongArgumentType.getLong(context, "groupID"));
                                    context.getSource().sendFeedback(() -> this.getLongSettingMessage(true, "groupID", this.extensionSettings().getGroupID()), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal("\n" + this.extensionSettings().getName() + "/" + "groupID\n").styled(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("ServerID, to know where to check members\n", Formatting.GRAY).styled(style -> style.withBold(false))).
                                    append(this.getLongSettingMessage(false, "groupID", this.extensionSettings().getGroupID())), false);
                            return 1;
                        })).
                then(literal("aggressive").
                        then(argument("aggressive", BoolArgumentType.bool()).
                                executes(context -> {
                                    extensionSettings().setAggressive(BoolArgumentType.getBool(context, "aggressive"));
                                    context.getSource().sendFeedback(() -> this.getBooleanSettingMessage(true, "aggressive", this.extensionSettings().isAggressive()), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal("\n" + this.extensionSettings().getName() + "/" + "aggressive\n").styled(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("Full whitelist/database sync\n", Formatting.GRAY).styled(style -> style.withBold(false))).
                                    append(this.getBooleanSettingMessage(false, "aggressive", this.extensionSettings().isAggressive())), false);
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

    private MutableText getLongSettingMessage(boolean isNew, String subcommand, long actualID) {
        MutableText s = this.longSetting(actualID);
        s.styled(style -> style.
                withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s %s %s", this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), subcommand, actualID != 0 ? String.format("%d", actualID) : ""))));
        return (isNew ? MarkEnum.TICK.appendMsg("New ", Formatting.WHITE) : Text.literal("")).styled(style -> style.withBold(false)).append(Text.literal(String.format("%s: ", subcommand)).styled(style -> style.withColor(Formatting.WHITE))).append(s);
    }

    private MutableText longSetting(long actualID) {
        MutableText sett;
        if (actualID != 0) {
            sett = Text.literal(String.format("%d", actualID)).styled(style -> style.
                    withColor(Formatting.GREEN).
                    withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to edit!\nSet 0 to disable!"))));
        } else {
            sett = Text.literal("Not set!").styled(style -> style.
                    withColor(Formatting.RED).
                    withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to add!"))));
        }
        return sett;
    }

    private MutableText getBooleanSettingMessage(boolean isNew, String subcommand, boolean enabled) {
        MutableText s = this.booleanSetting(enabled);
        s.styled(style -> style.
                withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s %s %b", this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), subcommand, enabled))));
        return (isNew ? MarkEnum.TICK.appendMsg("Set ", Formatting.WHITE) : Text.literal("")).styled(style -> style.withBold(false)).append(Text.literal(String.format("%s: ", subcommand)).styled(style -> style.withColor(Formatting.WHITE))).append(s);
    }

    private MutableText booleanSetting(boolean enabled) {
        return Text.literal(String.format("%b", enabled)).styled(style -> style.
                withColor(enabled ? Formatting.GREEN : Formatting.RED).
                withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to modify!"))));
    }
}
