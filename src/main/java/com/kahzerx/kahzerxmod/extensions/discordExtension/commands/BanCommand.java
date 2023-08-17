package com.kahzerx.kahzerxmod.extensions.discordExtension.commands;

import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordPermission;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordAdminToolsExtension.DiscordAdminToolsExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension.DiscordExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistExtension.DiscordWhitelistExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.utils.DiscordChatUtils;
import com.mojang.authlib.GameProfile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.minecraft.server.MinecraftServer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BanCommand extends GenericCommand {
    public BanCommand() {
        super("ban", "ban a whitelisted player from the mc server and makes the discord user not able to add anymore", DiscordPermission.ADMIN_CHAT);
    }

    @Override
    public void executeSlash(SlashCommandEvent event, MinecraftServer server, ExtensionManager extensionManager) {
        DiscordExtension discordExtension = extensionManager.getDiscordExtension();
        DiscordWhitelistExtension discordWhitelistExtension = extensionManager.getDiscordWhitelistExtension();
        DiscordAdminToolsExtension discordAdminToolsExtension = extensionManager.getDiscordAdminToolsExtension();
        boolean feedback = discordAdminToolsExtension.extensionSettings().isShouldFeedback();
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
        if (!discordWhitelistExtension.alreadyAddedBySomeone(playerProfile.getId().toString())) {
            this.replyMessage(event, feedback, String.format("The player %s is not whitelisted", playerName), prefix, false);
            return;
        }
        if (discordWhitelistExtension.canRemove(69420L, playerProfile.getId().toString())) {
            this.replyMessage(event, feedback, String.format("The player %s was added with the /exadd command, you need to use /exremove", playerName), prefix, false);
            return;
        }
        long discordID = discordWhitelistExtension.getDiscordID(playerProfile.getId().toString());
        if (discordWhitelistExtension.isPlayerBanned(playerProfile.getId().toString())) {
            onBanAction(discordWhitelistExtension, discordID, server);
            this.replyMessage(event, feedback, String.format("The player %s is already banned", playerName), prefix, false);
            return;
        }
        discordWhitelistExtension.banDiscord(discordID);
        onBanAction(discordWhitelistExtension, discordID, server);
        this.replyMessage(event, feedback, String.format("The player %s has been banned", playerName), prefix, false, true);
        Guild guild = event.getGuild();
        if (guild == null) {
            return;
        }
        Role role = guild.getRoleById(discordWhitelistExtension.extensionSettings().getDiscordRole());
        Member member = event.getMember();
        if (role != null && member != null) {
            try {
                guild.removeRoleFromMember(member, role).queue();
            } catch (HierarchyException exception) {
                exception.printStackTrace();
            }
        }
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
        if (!extension.alreadyAddedBySomeone(profile.get().getId().toString())) {
            EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**Not added.**"}, serverPrefix, true, Color.RED, true, feedback);
            if (embed != null) {
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
            return;
        }
        if (extension.canRemove(69420L, profile.get().getId().toString())) {
            EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**Added with !exadd... do !exremove.**"}, serverPrefix, true, Color.RED, true, feedback);
            if (embed != null) {
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
            return;
        }
        long discordID = extension.getDiscordID(profile.get().getId().toString());
        if (extension.isPlayerBanned(profile.get().getId().toString())) {
            onBanAction(extension, discordID, server);
            EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**Already banned!**"}, serverPrefix, true, Color.YELLOW, true, feedback);
            if (embed != null) {
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
            return;
        }
        extension.banDiscord(discordID);
        onBanAction(extension, discordID, server);
        EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**Banned! :D**"}, serverPrefix, true, Color.GREEN, true, feedback);
        if (embed != null) {
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        }
    }

    private void onBanAction(DiscordWhitelistExtension extension, long discordID, MinecraftServer server) {
        ArrayList<String> whitelistedPlayers = extension.getWhitelistedPlayers(discordID);
        for (String uuid : whitelistedPlayers) {
            Optional<GameProfile> p = server.getUserCache().getByUuid(UUID.fromString(uuid));
            if (p.isEmpty()) {
                continue;
            }
            extension.tryVanillaBan(server.getPlayerManager().getUserBanList(), p.get(), server);
            extension.tryVanillaWhitelistRemove(server.getPlayerManager().getWhitelist(), p.get(), server);
        }
    }
}
