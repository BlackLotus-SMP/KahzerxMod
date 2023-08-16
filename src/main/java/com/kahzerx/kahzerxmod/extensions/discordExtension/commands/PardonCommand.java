package com.kahzerx.kahzerxmod.extensions.discordExtension.commands;

import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordPermission;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordAdminToolsExtension.DiscordAdminToolsExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistExtension.DiscordWhitelistExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.utils.DiscordChatUtils;
import com.mojang.authlib.GameProfile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.server.MinecraftServer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PardonCommand extends GenericCommand {
    public PardonCommand() {
        super("pardon", "pardon a previously banned player with /ban", DiscordPermission.ADMIN_CHAT);
    }

    @Override
    public void executeSlash(SlashCommandEvent event, MinecraftServer server, ExtensionManager extensionManager) {

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
            EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**Not banned!**"}, serverPrefix, true, Color.RED, true, feedback);
            if (embed != null) {
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
            return;
        }
        long discordID = extension.getDiscordID(profile.get().getId().toString());
        if (!extension.isPlayerBanned(profile.get().getId().toString())) {
            onPardonAction(extension, discordID, server);
            EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**Not banned.**"}, serverPrefix, true, Color.YELLOW, true, feedback);
            if (embed != null) {
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
            return;
        }
        extension.pardonDiscord(discordID);
        onPardonAction(extension, discordID, server);
        EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**Unbanned! :D**"}, serverPrefix, true, Color.GREEN, true, feedback);
        if (embed != null) {
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        }
    }

    private void onPardonAction(DiscordWhitelistExtension extension, long discordID, MinecraftServer server) {
        ArrayList<String> whitelistedPlayers = extension.getWhitelistedPlayers(discordID);
        for (String uuid : whitelistedPlayers) {
            Optional<GameProfile> p = server.getUserCache().getByUuid(UUID.fromString(uuid));
            if (p.isEmpty()) {
                continue;
            }
            extension.tryVanillaPardon(server.getPlayerManager().getUserBanList(), p.get());
            extension.deletePlayer(discordID, p.get().getId().toString());
        }
    }
}
