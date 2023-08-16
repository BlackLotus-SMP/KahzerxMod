package com.kahzerx.kahzerxmod.extensions.discordExtension.commands;

import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordPermission;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordAdminToolsExtension.DiscordAdminToolsExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistExtension.DiscordWhitelistExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.utils.DiscordChatUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import net.minecraft.server.MinecraftServer;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class GenericCommand {
    private final String command;
    private final String description;
    private final DiscordPermission permission;
    private String commandPrefix = "";
    private final boolean needsPlayerParameter;

    public GenericCommand(String command, String description, DiscordPermission permission) {
        this.command = command;
        this.description = description;
        this.permission = permission;
        this.needsPlayerParameter = true;
    }

    public GenericCommand(String command, String description, DiscordPermission permission, boolean needsPlayerParameter) {
        this.command = command;
        this.description = description;
        this.permission = permission;
        this.needsPlayerParameter = needsPlayerParameter;
    }

    public abstract void executeSlash(SlashCommandEvent event, MinecraftServer server, ExtensionManager extensionManager);

    public void execute(MessageReceivedEvent event, MinecraftServer server, String serverPrefix) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void execute(MessageReceivedEvent event, MinecraftServer server, String serverPrefix, DiscordWhitelistExtension extension) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void execute(MessageReceivedEvent event, MinecraftServer server, String serverPrefix, List<Long> validChannels) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void execute(MessageReceivedEvent event, MinecraftServer server, String serverPrefix, DiscordWhitelistExtension extension, DiscordAdminToolsExtension adminExtension) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }

    public void setCommandPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    public DiscordPermission getPermission() {
        return permission;
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public boolean isNeedsPlayerParameter() {
        return needsPlayerParameter;
    }

    protected String getHelpSuggestion() {
        return String.format("%s%s", this.getCommandPrefix(), this.getCommand()) + (this.needsPlayerParameter ? " <playerName>" : "");
    }

    protected String getPlayer(SlashCommandEvent event) {
        OptionMapping option = event.getOption("player");
        if (option != null) {
            return option.getAsString();
        }
        return null;
    }

    protected void replyMessage(SlashCommandEvent event, boolean feedback, String message, String prefix) {
        this.replyMessage(event, feedback, message, prefix, true);
    }

    protected void replyMessage(SlashCommandEvent event, boolean feedback, String message, String prefix, boolean ephemeral) {
        ReplyAction action = event.reply(String.format("%s%s", prefix, message));
        if (feedback) {
            action.setEphemeral(ephemeral).queue();
        } else {
            action.setEphemeral(false).queue(m -> m.deleteOriginal().queueAfter(2, TimeUnit.SECONDS));
        }
    }

    public void sendHelpCommand(String serverPrefix, MessageChannel channel, boolean should) {
        EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{this.getHelpSuggestion()}, serverPrefix, true, Color.CYAN, true, should);
        if (embed != null) {
            channel.sendMessageEmbeds(embed.build()).queue(m -> m.delete().queueAfter(2, TimeUnit.SECONDS));
        }
    }
}
