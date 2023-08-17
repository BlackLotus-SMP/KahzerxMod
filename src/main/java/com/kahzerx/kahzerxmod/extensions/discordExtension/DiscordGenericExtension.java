package com.kahzerx.kahzerxmod.extensions.discordExtension;

import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.commands.GenericCommand;
import com.kahzerx.kahzerxmod.extensions.discordExtension.utils.DiscordChatUtils;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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

    protected MutableText formatLongID(String prefix, long id, String suffix, boolean add, boolean applied, String baseCommand, String extensionName, String subcommand) {
        return Text.literal(prefix).styled(style -> style.withColor(Formatting.WHITE)).
                append(Text.literal(String.format("%d", id)).styled(style -> style.
                        withColor(Formatting.DARK_GREEN).
                        withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to copy ID to clipboard"))).
                        withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.format("%d", id))))).
                append(Text.literal(suffix).styled(style -> style.withColor(Formatting.WHITE))).
                append(Text.literal(" ")).
                append(applied ?
                        (add ?
                                Text.literal("added").styled(style -> style.
                                        withColor(Formatting.GREEN).
                                        withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(String.format("Click to remove %d", id)))).
                                        withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %s %s %s %d", baseCommand, extensionName, subcommand, "remove", id)))) :
                                Text.literal("removed").styled(style -> style.
                                        withColor(Formatting.RED).
                                        withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(String.format("Click to add %d", id)))).
                                        withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %s %s %s %d", baseCommand, extensionName, subcommand, "add", id))))) :
                        Text.literal(""));
    }

    protected MutableText getLongSettingMessage(boolean isNew, String subcommand, long actualID, String baseCommand, String extensionName) {
        MutableText s = this.longSetting(actualID);
        s.styled(style -> style.
                withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s %s %s", baseCommand, extensionName, subcommand, actualID != 0 ? String.format("%d", actualID) : ""))));
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

    protected MutableText getStringSettingMessage(boolean isNew, String subcommand, String key, String baseCommand, String extensionName) {
        MutableText s = this.stringSetting(key);
        s.styled(style -> style.
                withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s %s %s", baseCommand, extensionName, subcommand, key))));
        return (isNew ? MarkEnum.TICK.appendMsg("New ", Formatting.WHITE) : Text.literal("")).styled(style -> style.withBold(false)).append(Text.literal(String.format("%s: ", subcommand)).styled(style -> style.withColor(Formatting.WHITE))).append(s);
    }

    private MutableText stringSetting(String key) {
        MutableText sett;
        if (key.equals("")) {
            sett = Text.literal("Not set!").styled(style -> style.
                    withColor(Formatting.RED).
                    withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to add!"))));
        } else {
            sett = Text.literal(key).styled(style -> style.
                    withColor(Formatting.GREEN).
                    withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to edit!\nSet 0 to disable!"))));
        }
        return sett;
    }

    protected MutableText getBooleanSettingMessage(boolean isNew, boolean enabled, String baseCommand, String extensionName, String subcommand) {
        MutableText s = this.booleanSetting(enabled);
        s.styled(style -> style.
                withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s %s %b", baseCommand, extensionName, subcommand, !enabled))));
        return (isNew ? MarkEnum.TICK.appendMsg("Set ", Formatting.WHITE) : Text.literal("")).styled(style -> style.withBold(false)).append(Text.literal(String.format("%s: ", subcommand)).styled(style -> style.withColor(Formatting.WHITE))).append(s);
    }

    private MutableText booleanSetting(boolean enabled) {
        return Text.literal(String.format("%b", enabled)).styled(style -> style.
                withColor(enabled ? Formatting.GREEN : Formatting.RED).
                withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to modify!"))));
    }

    protected record CommandFound(GenericCommand command, boolean found) { }
}
