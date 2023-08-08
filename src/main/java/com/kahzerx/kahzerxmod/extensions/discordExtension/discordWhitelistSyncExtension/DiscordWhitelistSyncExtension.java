package com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistSyncExtension;

import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordGenericExtension;
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

import static net.minecraft.command.CommandSource.suggestMatching;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DiscordWhitelistSyncExtension extends DiscordGenericExtension implements Extensions {
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
        builder.  // TODO Interact with the description and add functionality maybe other than just if its enabled and the description
                then(literal("notifyChatID").
                        then(argument("chatID", LongArgumentType.longArg()).
                                suggests((c, b) -> suggestMatching(new String[]{"0"}, b)).
                                executes(context -> {
                                    this.extensionSettings().setNotifyChannelID(LongArgumentType.getLong(context, "chatID"));
                                    context.getSource().sendFeedback(() -> this.getLongSettingMessage(true, "notifyChatID", this.extensionSettings().getNotifyChannelID(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName()), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal("\n" + this.extensionSettings().getName() + "/notifyChatID\n").styled(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("Channel where you get notified when someone gets removed from the whitelist\n", Formatting.GRAY).styled(style -> style.withBold(false))).
                                    append(this.getLongSettingMessage(false, "notifyChatID", this.extensionSettings().getNotifyChannelID(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName())), false);
                            return 1;
                        })).
                then(literal("groupID").
                        then(argument("groupID", LongArgumentType.longArg()).
                                suggests((c, b) -> suggestMatching(new String[]{"0"}, b)).
                                executes(context -> {
                                    extensionSettings().setGroupID(LongArgumentType.getLong(context, "groupID"));
                                    context.getSource().sendFeedback(() -> this.getLongSettingMessage(true, "groupID", this.extensionSettings().getGroupID(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName()), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal("\n" + this.extensionSettings().getName() + "/groupID\n").styled(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("ServerID, to know where to check members\n", Formatting.GRAY).styled(style -> style.withBold(false))).
                                    append(this.getLongSettingMessage(false, "groupID", this.extensionSettings().getGroupID(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName())), false);
                            return 1;
                        })).
                then(literal("aggressive").
                        then(argument("aggressive", BoolArgumentType.bool()).
                                executes(context -> {
                                    extensionSettings().setAggressive(BoolArgumentType.getBool(context, "aggressive"));
                                    context.getSource().sendFeedback(() -> this.getAggressiveBooleanSettingMessage(true, this.extensionSettings().isAggressive(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "aggressive"), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal("\n" + this.extensionSettings().getName() + "/aggressive\n").styled(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("Full whitelist/database sync\n", Formatting.GRAY).styled(style -> style.withBold(false))).
                                    append(this.getAggressiveBooleanSettingMessage(false, this.extensionSettings().isAggressive(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "aggressive")), false);
                            return 1;
                        })).
                then(literal("validRoles").
                        then(literal("add").
                                then(argument("roleID", LongArgumentType.longArg()).
                                        suggests((c, b) -> suggestMatching(new String[]{"1234"}, b)).
                                        executes(context -> {
                                            long role = LongArgumentType.getLong(context, "roleID");
                                            if (extensionSettings().getValidRoles().contains(role)) {
                                                context.getSource().sendFeedback(() -> MarkEnum.CROSS.appendText(this.formatLongID("The role ID ", role, " was already on the list", true, false, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "validRoles")), false);
                                            } else {
                                                extensionSettings().addValidRoleID(role);
                                                context.getSource().sendFeedback(() -> MarkEnum.TICK.appendText(this.formatLongID("The role with ID ", role, " has been", true, true, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "validRoles")), false);
                                                this.em.saveSettings();
                                            }
                                            return 1;
                                        }))).
                        then(literal("remove").
                                then(argument("roleID", LongArgumentType.longArg()).
                                        suggests((c, b) -> suggestMatching(this.extensionSettings().getValidRoles().stream().map(Object::toString), b)).
                                        executes(context -> {
                                            long role = LongArgumentType.getLong(context, "roleID");
                                            if (extensionSettings().getValidRoles().contains(role)) {
                                                extensionSettings().removeValidRoleID(role);
                                                context.getSource().sendFeedback(() -> MarkEnum.TICK.appendText(this.formatLongID("The role with ID ", role, " has been", false, true, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "validRoles")), false);
                                                this.em.saveSettings();
                                            } else {
                                                context.getSource().sendFeedback(() -> MarkEnum.CROSS.appendText(this.formatLongID("The role ID ", role," does not exist!", false, false, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "validRoles")), false);
                                            }
                                            return 1;
                                        }))).
                        then(literal("list").
                                executes(context -> {
                                    MutableText roles = Text.literal("");
                                    int roleCount = this.extensionSettings().getValidRoles().size();
                                    if (roleCount == 0) {
                                        roles.append(Text.literal("Not set!").styled(style -> style.
                                                withColor(Formatting.RED).
                                                withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to add!"))).
                                                withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s validRoles add ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName())))));
                                    } else {
                                        roles.
                                                append(Text.literal("[+]").styled(style -> style.
                                                        withColor(Formatting.GREEN).
                                                        withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to add!"))).
                                                        withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s validRoles add ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName()))))).
                                                append(Text.literal(" ")).
                                                append(Text.literal("[-]\n").styled(style -> style.
                                                        withColor(Formatting.RED).
                                                        withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to remove!"))).
                                                        withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s validRoles remove ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName())))));
                                        for (int i = 0; i < roleCount; i++) {
                                            long role = this.extensionSettings().getValidRoles().get(i);
                                            roles.
                                                    append(MarkEnum.DOT.appendText(Text.literal(String.format("%d", role)).styled(style -> style.
                                                            withBold(false).
                                                            withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(String.format("Click to copy %d", role)))).
                                                            withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.format("%d", role)))), Formatting.GRAY)).
                                                    append(Text.literal(" ")).
                                                    append(MarkEnum.CROSS.getFormattedIdentifier().styled(style -> style.
                                                            withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(String.format("Click to delete %d", role)))).
                                                            withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %s validRoles remove %d", this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), role))))).
                                                    append(i == roleCount-1 ? Text.literal("") : Text.literal("\n"));
                                        }
                                    }
                                    context.getSource().sendFeedback(() -> Text.literal("\n" + this.extensionSettings().getName() + "/validRoles/list" + "\n").styled(style -> style.withBold(true)).
                                            append(roles), false);
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal("\n" + this.extensionSettings().getName() + "/validRoles\n").styled(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("Role list that a member needs to have (at least one) so dont get kicked from the whitelist (ex: sub role)\n", Formatting.GRAY).styled(style -> style.withBold(false))).
                                    append(Text.literal("[Roles]").styled(style -> style.
                                            withColor(Formatting.DARK_GRAY).
                                            withUnderline(true).
                                            withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to display the already added valid role IDs"))).
                                            withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %s validRoles list", this.em.getSettingsBaseCommand(), this.extensionSettings().getName()))))), false);
                            return 1;
                        }));
    }
}
