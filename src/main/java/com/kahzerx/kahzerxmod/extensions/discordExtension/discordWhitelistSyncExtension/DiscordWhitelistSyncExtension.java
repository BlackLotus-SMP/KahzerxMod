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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import java.util.HashMap;
import java.util.Timer;

import static net.minecraft.commands.SharedSuggestionProvider.suggest;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

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
    public void onExtensionEnabled(CommandSourceStack source) { }

    @Override
    public void onExtensionDisabled(CommandSourceStack source) { }

    @Override
    public void settingsCommand(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.  // TODO Interact with the description and add functionality maybe other than just if its enabled and the description
                then(literal("notifyChatID").
                        then(argument("chatID", LongArgumentType.longArg()).
                                suggests((c, b) -> suggest(new String[]{"0"}, b)).
                                executes(context -> {
                                    this.extensionSettings().setNotifyChannelID(LongArgumentType.getLong(context, "chatID"));
                                    context.getSource().sendSuccess(() -> this.getLongSettingMessage(true, "notifyChatID", this.extensionSettings().getNotifyChannelID(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName()), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendSuccess(() -> Component.literal("\n" + this.extensionSettings().getName() + "/notifyChatID\n").withStyle(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("Channel where you get notified when someone gets removed from the whitelist\n", ChatFormatting.GRAY).withStyle(style -> style.withBold(false))).
                                    append(this.getLongSettingMessage(false, "notifyChatID", this.extensionSettings().getNotifyChannelID(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName())), false);
                            return 1;
                        })).
                then(literal("groupID").
                        then(argument("groupID", LongArgumentType.longArg()).
                                suggests((c, b) -> suggest(new String[]{"0"}, b)).
                                executes(context -> {
                                    extensionSettings().setGroupID(LongArgumentType.getLong(context, "groupID"));
                                    context.getSource().sendSuccess(() -> this.getLongSettingMessage(true, "groupID", this.extensionSettings().getGroupID(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName()), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendSuccess(() -> Component.literal("\n" + this.extensionSettings().getName() + "/groupID\n").withStyle(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("ServerID, to know where to check members\n", ChatFormatting.GRAY).withStyle(style -> style.withBold(false))).
                                    append(this.getLongSettingMessage(false, "groupID", this.extensionSettings().getGroupID(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName())), false);
                            return 1;
                        })).
                then(literal("aggressive").
                        then(argument("aggressive", BoolArgumentType.bool()).
                                executes(context -> {
                                    extensionSettings().setAggressive(BoolArgumentType.getBool(context, "aggressive"));
                                    context.getSource().sendSuccess(() -> this.getBooleanSettingMessage(true, this.extensionSettings().isAggressive(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "aggressive"), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendSuccess(() -> Component.literal("\n" + this.extensionSettings().getName() + "/aggressive\n").withStyle(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("Full whitelist/database sync\n", ChatFormatting.GRAY).withStyle(style -> style.withBold(false))).
                                    append(this.getBooleanSettingMessage(false, this.extensionSettings().isAggressive(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "aggressive")), false);
                            return 1;
                        })).
                then(literal("validRoles").
                        then(literal("add").
                                then(argument("roleID", LongArgumentType.longArg()).
                                        suggests((c, b) -> suggest(new String[]{"1234"}, b)).
                                        executes(context -> {
                                            long role = LongArgumentType.getLong(context, "roleID");
                                            if (extensionSettings().getValidRoles().contains(role)) {
                                                context.getSource().sendSuccess(() -> MarkEnum.CROSS.appendText(this.formatLongID("The role ID ", role, " was already on the list", true, false, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "validRoles")), false);
                                            } else {
                                                extensionSettings().addValidRoleID(role);
                                                context.getSource().sendSuccess(() -> MarkEnum.TICK.appendText(this.formatLongID("The role with ID ", role, " has been", true, true, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "validRoles")), false);
                                                this.em.saveSettings();
                                            }
                                            return 1;
                                        }))).
                        then(literal("remove").
                                then(argument("roleID", LongArgumentType.longArg()).
                                        suggests((c, b) -> suggest(this.extensionSettings().getValidRoles().stream().map(Object::toString), b)).
                                        executes(context -> {
                                            long role = LongArgumentType.getLong(context, "roleID");
                                            if (extensionSettings().getValidRoles().contains(role)) {
                                                extensionSettings().removeValidRoleID(role);
                                                context.getSource().sendSuccess(() -> MarkEnum.TICK.appendText(this.formatLongID("The role with ID ", role, " has been", false, true, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "validRoles")), false);
                                                this.em.saveSettings();
                                            } else {
                                                context.getSource().sendSuccess(() -> MarkEnum.CROSS.appendText(this.formatLongID("The role ID ", role," does not exist!", false, false, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "validRoles")), false);
                                            }
                                            return 1;
                                        }))).
                        then(literal("list").
                                executes(context -> {
                                    MutableComponent roles = Component.literal("");
                                    int roleCount = this.extensionSettings().getValidRoles().size();
                                    if (roleCount == 0) {
                                        roles.append(Component.literal("Not set!").withStyle(style -> style.
                                                withColor(ChatFormatting.RED).
                                                withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to add!"))).
                                                withClickEvent(new ClickEvent.SuggestCommand(String.format("/%s %s validRoles add ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName())))));
                                    } else {
                                        roles.
                                                append(Component.literal("[+]").withStyle(style -> style.
                                                        withColor(ChatFormatting.GREEN).
                                                        withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to add!"))).
                                                        withClickEvent(new ClickEvent.SuggestCommand(String.format("/%s %s validRoles add ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName()))))).
                                                append(Component.literal(" ")).
                                                append(Component.literal("[-]\n").withStyle(style -> style.
                                                        withColor(ChatFormatting.RED).
                                                        withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to remove!"))).
                                                        withClickEvent(new ClickEvent.SuggestCommand(String.format("/%s %s validRoles remove ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName())))));
                                        for (int i = 0; i < roleCount; i++) {
                                            long role = this.extensionSettings().getValidRoles().get(i);
                                            roles.
                                                    append(MarkEnum.DOT.appendText(Component.literal(String.format("%d", role)).withStyle(style -> style.
                                                            withBold(false).
                                                            withHoverEvent(new HoverEvent.ShowText(Component.literal(String.format("Click to copy %d", role)))).
                                                            withClickEvent(new ClickEvent.CopyToClipboard(String.format("%d", role)))), ChatFormatting.GRAY)).
                                                    append(Component.literal(" ")).
                                                    append(MarkEnum.CROSS.getFormattedIdentifier().withStyle(style -> style.
                                                            withHoverEvent(new HoverEvent.ShowText(Component.literal(String.format("Click to delete %d", role)))).
                                                            withClickEvent(new ClickEvent.RunCommand(String.format("/%s %s validRoles remove %d", this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), role))))).
                                                    append(i == roleCount-1 ? Component.literal("") : Component.literal("\n"));
                                        }
                                    }
                                    context.getSource().sendSuccess(() -> Component.literal("\n" + this.extensionSettings().getName() + "/validRoles/list" + "\n").withStyle(style -> style.withBold(true)).
                                            append(roles), false);
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendSuccess(() -> Component.literal("\n" + this.extensionSettings().getName() + "/validRoles\n").withStyle(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("Role list that a member needs to have (at least one) so dont get kicked from the whitelist (ex: sub role)\n", ChatFormatting.GRAY).withStyle(style -> style.withBold(false))).
                                    append(Component.literal("[Roles]").withStyle(style -> style.
                                            withColor(ChatFormatting.DARK_GRAY).
                                            withUnderlined(true).
                                            withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to display the already added valid role IDs"))).
                                            withClickEvent(new ClickEvent.RunCommand(String.format("/%s %s validRoles list", this.em.getSettingsBaseCommand(), this.extensionSettings().getName()))))), false);
                            return 1;
                        }));
    }

    @Override
    protected boolean processCommands(MessageReceivedEvent event, String message, MinecraftServer server) {
        return false;
    }
}
