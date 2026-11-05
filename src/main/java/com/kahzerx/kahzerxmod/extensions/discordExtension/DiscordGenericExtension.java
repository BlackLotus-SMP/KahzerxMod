package com.kahzerx.kahzerxmod.extensions.discordExtension;

import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.commands.GenericCommand;
import com.kahzerx.kahzerxmod.extensions.discordExtension.utils.DiscordChatUtils;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class DiscordGenericExtension extends GenericExtension {
    private final ArrayList<GenericCommand> commands = new ArrayList<>();
    public DiscordGenericExtension(ExtensionSettings settings) {
        super(settings);
    }

    protected abstract boolean processCommands(MessageReceivedEvent event, String message, MinecraftServer server);

    protected CommandFound findValidCommand(MessageReceivedEvent event, String message, List<Long> allowedChats, boolean shouldFeedback, String serverPrefix) {
        for (GenericCommand command : this.getCommands()) {
            if (!message.startsWith(command.getCommand())) {
                continue;
            }
            if (!this.getSettings().isEnabled()) {
                EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"The extension is not enabled"}, serverPrefix, true, Color.RED, true, shouldFeedback);
                this.sendEmbed(event, embed);
                return new CommandFound(null, true);
            }
            if (allowedChats.size() == 0) {
                EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"There are no chats available for this command"}, serverPrefix, true, Color.RED, true, shouldFeedback);
                this.sendEmbed(event, embed);
                return new CommandFound(null, true);
            }
            if (!allowedChats.contains(event.getChannel().getIdLong())) {
                EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"This is not the right channel for this command"}, serverPrefix, true, Color.RED, true, shouldFeedback);
                this.sendEmbed(event, embed);
                return new CommandFound(null, true);
            }
            return new CommandFound(command, true);
        }
        return new CommandFound(null, false);
    }

    private void sendEmbed(MessageReceivedEvent event, EmbedBuilder embed) {
        if (embed != null) {
            event.getMessage().delete().queueAfter(2, TimeUnit.SECONDS);
            MessageAction embedSent = event.getChannel().sendMessageEmbeds(embed.build());
            embedSent.queue(m -> m.delete().queueAfter(3, TimeUnit.SECONDS));
        }
    }

    protected void addCommands(GenericCommand... c) {
        this.commands.addAll(Arrays.asList(c));
    }

    public ArrayList<GenericCommand> getCommands() {
        return commands;
    }

    protected MutableComponent formatLongID(String prefix, long id, String suffix, boolean add, boolean applied, String baseCommand, String extensionName, String subcommand) {
        return Component.literal(prefix).withStyle(style -> style.withColor(ChatFormatting.WHITE)).
                append(Component.literal(String.format("%d", id)).withStyle(style -> style.
                        withColor(ChatFormatting.DARK_GREEN).
                        withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to copy ID to clipboard"))).
                        withClickEvent(new ClickEvent.CopyToClipboard(String.format("%d", id))))).
                append(Component.literal(suffix).withStyle(style -> style.withColor(ChatFormatting.WHITE))).
                append(Component.literal(" ")).
                append(applied ?
                        (add ?
                                Component.literal("added").withStyle(style -> style.
                                        withColor(ChatFormatting.GREEN).
                                        withHoverEvent(new HoverEvent.ShowText(Component.literal(String.format("Click to remove %d", id)))).
                                        withClickEvent(new ClickEvent.RunCommand(String.format("/%s %s %s %s %d", baseCommand, extensionName, subcommand, "remove", id)))) :
                                Component.literal("removed").withStyle(style -> style.
                                        withColor(ChatFormatting.RED).
                                        withHoverEvent(new HoverEvent.ShowText(Component.literal(String.format("Click to add %d", id)))).
                                        withClickEvent(new ClickEvent.RunCommand(String.format("/%s %s %s %s %d", baseCommand, extensionName, subcommand, "add", id))))) :
                        Component.literal(""));
    }

    protected MutableComponent getLongSettingMessage(boolean isNew, String subcommand, long actualID, String baseCommand, String extensionName) {
        MutableComponent s = this.longSetting(actualID);
        s.withStyle(style -> style.
                withClickEvent(new ClickEvent.SuggestCommand(String.format("/%s %s %s %s", baseCommand, extensionName, subcommand, actualID != 0 ? String.format("%d", actualID) : ""))));
        return (isNew ? MarkEnum.TICK.appendMsg("New ", ChatFormatting.WHITE) : Component.literal("")).withStyle(style -> style.withBold(false)).append(Component.literal(String.format("%s: ", subcommand)).withStyle(style -> style.withColor(ChatFormatting.WHITE))).append(s);
    }

    private MutableComponent longSetting(long actualID) {
        MutableComponent sett;
        if (actualID != 0) {
            sett = Component.literal(String.format("%d", actualID)).withStyle(style -> style.
                    withColor(ChatFormatting.GREEN).
                    withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to edit!\nSet 0 to disable!"))));
        } else {
            sett = Component.literal("Not set!").withStyle(style -> style.
                    withColor(ChatFormatting.RED).
                    withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to add!"))));
        }
        return sett;
    }

    protected MutableComponent getStringSettingMessage(boolean isNew, String subcommand, String key, String baseCommand, String extensionName) {
        MutableComponent s = this.stringSetting(key);
        s.withStyle(style -> style.
                withClickEvent(new ClickEvent.SuggestCommand(String.format("/%s %s %s %s", baseCommand, extensionName, subcommand, key))));
        return (isNew ? MarkEnum.TICK.appendMsg("New ", ChatFormatting.WHITE) : Component.literal("")).withStyle(style -> style.withBold(false)).append(Component.literal(String.format("%s: ", subcommand)).withStyle(style -> style.withColor(ChatFormatting.WHITE))).append(s);
    }

    private MutableComponent stringSetting(String key) {
        MutableComponent sett;
        if (key.equals("")) {
            sett = Component.literal("Not set!").withStyle(style -> style.
                    withColor(ChatFormatting.RED).
                    withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to add!"))));
        } else {
            sett = Component.literal(key).withStyle(style -> style.
                    withColor(ChatFormatting.GREEN).
                    withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to edit!\nSet 0 to disable!"))));
        }
        return sett;
    }

    protected MutableComponent getBooleanSettingMessage(boolean isNew, boolean enabled, String baseCommand, String extensionName, String subcommand) {
        MutableComponent s = this.booleanSetting(enabled);
        s.withStyle(style -> style.
                withClickEvent(new ClickEvent.SuggestCommand(String.format("/%s %s %s %b", baseCommand, extensionName, subcommand, !enabled))));
        return (isNew ? MarkEnum.TICK.appendMsg("Set ", ChatFormatting.WHITE) : Component.literal("")).withStyle(style -> style.withBold(false)).append(Component.literal(String.format("%s: ", subcommand)).withStyle(style -> style.withColor(ChatFormatting.WHITE))).append(s);
    }

    private MutableComponent booleanSetting(boolean enabled) {
        return Component.literal(String.format("%b", enabled)).withStyle(style -> style.
                withColor(enabled ? ChatFormatting.GREEN : ChatFormatting.RED).
                withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to modify!"))));
    }

    protected record CommandFound(GenericCommand command, boolean found) { }
}
