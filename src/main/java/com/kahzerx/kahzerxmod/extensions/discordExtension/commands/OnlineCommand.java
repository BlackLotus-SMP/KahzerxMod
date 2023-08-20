package com.kahzerx.kahzerxmod.extensions.discordExtension.commands;

import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordPermission;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension.DiscordExtension;
import com.kahzerx.kahzerxmod.klone.KlonePlayerEntity;
import com.kahzerx.kahzerxmod.extensions.discordExtension.utils.DiscordChatUtils;
import com.kahzerx.kahzerxmod.extensions.discordExtension.utils.DiscordUtils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.Objects;

import static com.kahzerx.kahzerxmod.extensions.fbiExtension.FBIExtension.getHiddenPlayers;

public class OnlineCommand extends GenericCommand {
    public OnlineCommand() {
        super("online", "list online players", DiscordPermission.ALLOWED_CHAT, false);
    }

    @Override
    public void executeCommand(MessageReceivedEvent event, MinecraftServer server, ExtensionManager extensionManager) {
        DiscordExtension discordExtension = extensionManager.getDiscordExtension();
        String serverPrefix = discordExtension.extensionSettings().getPrefix();
        boolean feedback = discordExtension.extensionSettings().isShouldFeedback();
        StringBuilder msg = new StringBuilder();
        int n = server.getPlayerManager().getPlayerList().size();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (getHiddenPlayers().contains(player)) {
                continue;
            }
            boolean isBot = player.getClass() == KlonePlayerEntity.class;
            msg.append(player.getName().getString().replace("_", "\\_")).append(isBot ? " [Bot]" : "").append("\n");
        }
        if (feedback) {
            event.getChannel().sendMessageEmbeds(Objects.requireNonNull(DiscordChatUtils.generateEmbed(msg, n, serverPrefix)).build()).queue();
        }
    }
}
