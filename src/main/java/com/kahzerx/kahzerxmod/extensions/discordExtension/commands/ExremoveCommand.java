package com.kahzerx.kahzerxmod.extensions.discordExtension.commands;

import net.minecraft.server.players.NameAndId;
import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordPermission;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordAdminToolsExtension.DiscordAdminToolsExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension.DiscordExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistExtension.DiscordWhitelistExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.utils.DiscordChatUtils;
import com.mojang.authlib.GameProfile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;
import java.awt.*;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ExremoveCommand extends GenericCommand {
    public ExremoveCommand() {
        super("exremove", "remove a player previously added by the /exadd command", DiscordPermission.ADMIN_CHAT);
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
        Optional<NameAndId> profile = server.services().nameToIdCache().get(playerName);
        if (profile.isEmpty()) {
            EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**Not premium.**"}, serverPrefix, true, Color.RED, true, feedback);
            if (embed != null) {
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
            return;
        }
        if (!discordWhitelistExtension.canRemove(69420L, profile.get().id().toString())) {
            EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**You can't remove " + profile.get().name() + ".**"}, serverPrefix, true, Color.RED, true, feedback);
            if (embed != null) {
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
            return;
        }
        UserWhiteList whitelist = server.getPlayerList().getWhiteList();
        if (!whitelist.isWhiteListed(profile.get())) {
            EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**" + playerName + " is not whitelisted.**"}, serverPrefix, true, Color.YELLOW, true, feedback);
            if (embed != null) {
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
            return;
        }
        UserWhiteListEntry whitelistEntry = new UserWhiteListEntry(profile.get());
        discordWhitelistExtension.deletePlayer(69420L, profile.get().id().toString());
        whitelist.remove(whitelistEntry);
        ServerPlayer player = server.getPlayerList().getPlayer(profile.get().id());
        if (player != null) {
            player.connection.disconnect(Component.literal("Byee~"));
        }
        EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**" + profile.get().name() + " removed D:**"}, serverPrefix, true, Color.GREEN, true, feedback);
        if (embed != null) {
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        }
    }
}
