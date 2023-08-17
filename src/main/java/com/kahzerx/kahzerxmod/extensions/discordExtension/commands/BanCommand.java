package com.kahzerx.kahzerxmod.extensions.discordExtension.commands;

import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordPermission;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordAdminToolsExtension.DiscordAdminToolsExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension.DiscordExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistExtension.DiscordWhitelistExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.utils.DiscordChatUtils;
import com.mojang.authlib.GameProfile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
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
    public void executeCommand(MessageReceivedEvent event, MinecraftServer server, ExtensionManager extensionManager) {
        DiscordWhitelistExtension discordWhitelistExtension = extensionManager.getDiscordWhitelistExtension();
        DiscordExtension discordExtension = extensionManager.getDiscordExtension();
        DiscordAdminToolsExtension discordAdminToolsExtension = extensionManager.getDiscordAdminToolsExtension();
        String serverPrefix = discordExtension.extensionSettings().getPrefix();
        boolean feedback = discordAdminToolsExtension.extensionSettings().isShouldFeedback();
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
        if (!discordWhitelistExtension.alreadyAddedBySomeone(profile.get().getId().toString())) {
            EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**Not added.**"}, serverPrefix, true, Color.RED, true, feedback);
            if (embed != null) {
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
            return;
        }
        if (discordWhitelistExtension.canRemove(69420L, profile.get().getId().toString())) {
            EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**Added with !exadd... do !exremove.**"}, serverPrefix, true, Color.RED, true, feedback);
            if (embed != null) {
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
            return;
        }
        long discordID = discordWhitelistExtension.getDiscordID(profile.get().getId().toString());
        if (discordWhitelistExtension.isPlayerBanned(profile.get().getId().toString())) {
            onBanAction(discordWhitelistExtension, discordID, server);
            EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**Already banned!**"}, serverPrefix, true, Color.YELLOW, true, feedback);
            if (embed != null) {
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
            return;
        }
        discordWhitelistExtension.banDiscord(discordID);
        onBanAction(discordWhitelistExtension, discordID, server);
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
