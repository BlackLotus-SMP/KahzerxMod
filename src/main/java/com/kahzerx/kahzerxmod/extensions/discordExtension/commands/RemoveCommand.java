package com.kahzerx.kahzerxmod.extensions.discordExtension.commands;

import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordPermission;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension.DiscordExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistExtension.DiscordWhitelistExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.utils.DiscordChatUtils;
import com.mojang.authlib.GameProfile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Whitelist;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class RemoveCommand extends GenericCommand {
    public RemoveCommand() {
        super("remove", "remove from whitelist a previously added player with /add", DiscordPermission.WHITELIST_CHAT);
    }

    @Override
    public void executeCommand(MessageReceivedEvent event, MinecraftServer server, ExtensionManager extensionManager) {
        DiscordWhitelistExtension discordWhitelistExtension = extensionManager.getDiscordWhitelistExtension();
        DiscordExtension discordExtension = extensionManager.getDiscordExtension();
        String serverPrefix = discordExtension.extensionSettings().getPrefix();
        boolean feedback = discordExtension.extensionSettings().isShouldFeedback();
        long id = event.getAuthor().getIdLong();
        if (discordWhitelistExtension.isDiscordBanned(id)) {
            EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**Looks like you are banned...**"}, serverPrefix, true, Color.RED, true, feedback);
            assert embed != null;
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
            return;
        }
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
        if (!whitelist.isAllowed(profile.get())) {
            EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**" + playerName + " is not whitelisted.**"}, serverPrefix, true, Color.YELLOW, true, feedback);
            if (embed != null) {
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
            return;
        }
        if (!discordWhitelistExtension.canRemove(id, profile.get().getId().toString())) {
            EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**You can't remove " + profile.get().getName() + ".**"}, serverPrefix, true, Color.RED, true, feedback);
            if (embed != null) {
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
            return;
        }
        WhitelistEntry whitelistEntry = new WhitelistEntry(profile.get());
        discordWhitelistExtension.deletePlayer(id, profile.get().getId().toString());
        whitelist.remove(whitelistEntry);
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(profile.get().getId());
        if (player != null) {
            player.networkHandler.disconnect(Text.literal("Byee~"));
        }

        if (discordWhitelistExtension.getWhitelistedPlayers(id).isEmpty()) {
            Guild guild = event.getGuild();
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

        EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**" + profile.get().getName() + " removed D:**"}, serverPrefix, true, Color.GREEN, true, feedback);
        if (embed != null) {
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        }
    }
}
