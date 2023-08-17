package com.kahzerx.kahzerxmod.extensions.discordExtension.commands;

import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordPermission;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordAdminToolsExtension.DiscordAdminToolsExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension.DiscordExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistExtension.DiscordWhitelistExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.utils.DiscordChatUtils;
import com.mojang.authlib.GameProfile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Whitelist;
import net.minecraft.server.WhitelistEntry;

import java.awt.*;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ExaddCommand extends GenericCommand {
    public ExaddCommand() {
        super("exadd", "add a mc player that is not on the discord server, stands for exception add", DiscordPermission.ADMIN_CHAT);
    }

    @Override
    public void executeSlash(SlashCommandEvent event, MinecraftServer server, ExtensionManager extensionManager) {
        DiscordExtension discordExtension = extensionManager.getDiscordExtension();
        DiscordWhitelistExtension discordWhitelistExtension = extensionManager.getDiscordWhitelistExtension();
        boolean feedback = discordExtension.extensionSettings().isShouldFeedback();
        String prefix = discordExtension.extensionSettings().getPrefix();
        String playerName = this.getPlayer(event);
        if (playerName == null) {
            this.replyMessage(event, feedback, String.format("You need to add the player name!\n%s", this.getHelpSuggestion()), prefix, false);
            return;
        }
        Optional<GameProfile> profile = server.getUserCache().findByName(playerName);
        if (profile.isEmpty()) {
            this.replyMessage(event, feedback, String.format("The player %s is not premium", playerName), prefix, false);
            return;
        }
        GameProfile playerProfile = profile.get();
        if (discordWhitelistExtension.isPlayerBanned(playerProfile.getId().toString())) {
            this.replyMessage(event, feedback, String.format("The player %s is banned", playerProfile.getName()), prefix, false);
            return;
        }
        Whitelist whitelist = server.getPlayerManager().getWhitelist();
        if (whitelist.isAllowed(playerProfile)) {
            this.replyMessage(event, feedback, String.format("The player %s is already whitelisted", playerProfile.getName()), prefix, false);
            return;
        }
        if (discordWhitelistExtension.alreadyAddedBySomeone(playerProfile.getId().toString())) {
            this.replyMessage(event, feedback, String.format("The player %s is already whitelisted by someone else", playerProfile.getName()), prefix, false);
            return;
        }
        WhitelistEntry whitelistEntry = new WhitelistEntry(playerProfile);
        discordWhitelistExtension.addPlayer(69420L, playerProfile.getId().toString(), playerProfile.getName());
        whitelist.add(whitelistEntry);
        this.replyMessage(event, feedback, String.format("%s has been added to the whitelist!", playerProfile.getName()), prefix, false, true);
    }

    @Override
    public void execute(MessageReceivedEvent event, MinecraftServer server, String serverPrefix, DiscordWhitelistExtension extension, DiscordAdminToolsExtension adminExtension) {
        boolean feedback = adminExtension.extensionSettings().isShouldFeedback();
        String[] req = event.getMessage().getContentRaw().split(" ");
        String playerName = req[1];
        if (req.length != 2) {
            event.getMessage().delete().queueAfter(2, TimeUnit.SECONDS);
            this.sendHelpCommand(serverPrefix, event.getChannel(), feedback);
            return;
        }
        Optional<GameProfile> profile = server.getUserCache().findByName(playerName);
        if (profile.isEmpty()) {
            EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**Not premium.**"}, serverPrefix, true, Color.RED, true, feedback);
            if (embed != null) {
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
            return;
        }
        Whitelist whitelist = server.getPlayerManager().getWhitelist();
        if (whitelist.isAllowed(profile.get())) {
            EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**" + playerName + " already whitelisted.**"}, serverPrefix, true, Color.YELLOW, true, feedback);
            if (embed != null) {
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
            return;
        }
        WhitelistEntry whitelistEntry = new WhitelistEntry(profile.get());
        if (extension.isPlayerBanned(profile.get().getId().toString())) {
            EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**Looks like that player is banned.**"}, serverPrefix, true, Color.RED, true, feedback);
            if (embed != null) {
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
            return;
        }
        if (extension.alreadyAddedBySomeone(profile.get().getId().toString())) {
            EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**Already whitelisted by someone else.**"}, serverPrefix, true, Color.RED, true, feedback);
            if (embed != null) {
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
            return;
        }
        extension.addPlayer(69420L, profile.get().getId().toString(), profile.get().getName());
        whitelist.add(whitelistEntry);
        EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**" + profile.get().getName() + " added :D**"}, serverPrefix, true, Color.GREEN, true, feedback);
        if (embed != null) {
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        }
    }
}
