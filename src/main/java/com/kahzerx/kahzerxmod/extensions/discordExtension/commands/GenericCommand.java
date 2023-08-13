package com.kahzerx.kahzerxmod.extensions.discordExtension.commands;

import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordPermission;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordAdminToolsExtension.DiscordAdminToolsExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistExtension.DiscordWhitelistExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.utils.DiscordChatUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.server.MinecraftServer;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GenericCommand {
    private final String body;
    private final DiscordPermission permission;
    private String commandPrefix = "!";
    private final boolean needsPlayerParameter;

    public GenericCommand(String body, DiscordPermission permission) {
        this.body = body;
        this.permission = permission;
        this.needsPlayerParameter = true;
    }

    public GenericCommand(String body, DiscordPermission permission, boolean needsPlayerParameter) {
        this.body = body;
        this.permission = permission;
        this.needsPlayerParameter = needsPlayerParameter;
    }

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

    public void setPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    public DiscordPermission getPermission() {
        return permission;
    }

    public String getBody() {
        return body;
    }

    private String getHelpSuggestion() {
        return String.format("%s%s", this.getCommandPrefix(), this.getBody()) + (this.needsPlayerParameter ? " <playerName>" : "");
    }

    public void sendHelpCommand(String serverPrefix, MessageChannel channel, boolean should) {
        EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{this.getHelpSuggestion()}, serverPrefix, true, Color.CYAN, true, should);
        if (embed != null) {
            channel.sendMessageEmbeds(embed.build()).queue(m -> m.delete().queueAfter(2, TimeUnit.SECONDS));
        }
    }
}
