package com.kahzerx.kahzerxmod.extensions.discordExtension;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.kahzerx.kahzerxmod.extensions.discordExtension.commands.OnlineCommand;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension.DiscordExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.utils.DiscordChatUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

// TODO refactor
public class DiscordBot extends ListenerAdapter implements DiscordBotInterface {
    private final MinecraftServer server;
    private final DiscordExtension discordExtension;
    private final String PREFIX = "!";  // TODO custom?
    private final OnlineCommand onlineCommand = new OnlineCommand(PREFIX);
    private JDA jda = null;
    private WebhookClient whc;
    private final List<DiscordCommandsExtension> discordExtensions = new ArrayList<>();

    public DiscordBot(MinecraftServer server, DiscordExtension discordExtension) {  // TODO slash commands?
        this.server = server;
        this.discordExtension = discordExtension;
    }

    public boolean start() {
        try {
            this.jda = JDABuilder.createDefault(discordExtension.extensionSettings().getToken()).addEventListeners(this).build();
            this.jda.awaitReady();
            this.updateWebHooks();
            return true;
        } catch (LoginException | InterruptedException e) {
            this.jda = null;
            e.printStackTrace();
            return false;
        }
    }

    public boolean onUpdateChannelID() {
        try {
            this.updateWebHooks();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateWebHooks() {
        if (!this.discordExtension.extensionSettings().isEnabled()) {
            return;
        }
        List<Webhook> webhookList = this.jda.getTextChannelById(this.discordExtension.extensionSettings().getChatChannelID()).retrieveWebhooks().complete();
        boolean webhookFlag = false;
        for (Webhook webdook : webhookList) {
            if (webdook.getName().equals("ChatBridge")) {
                webhookFlag = true;
                this.whc = WebhookClient.withUrl(webdook.getUrl());
                break;
            }
        }
        if (!webhookFlag) {
            Webhook webook = jda.getTextChannelById(this.discordExtension.extensionSettings().getChatChannelID()).createWebhook("ChatBridge").complete();
            this.whc = WebhookClient.withUrl(webook.getUrl());
        }
    }

    public void addExtensions(DiscordCommandsExtension... extensions) {
        for (DiscordCommandsExtension e : extensions) {
            if (!this.discordExtensions.contains(e)) {
                this.discordExtensions.add(e);
            }
        }
    }

    public void removeExtensions(DiscordCommandsExtension... extensions) {
        for (DiscordCommandsExtension e : extensions) {
            this.discordExtensions.remove(e);
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getContentDisplay().equals("")) {
            return;
        }
        if (event.getMessage().getContentRaw().equals("")) {
            return;
        }
        if (event.getAuthor().isBot()) {
            if (!this.discordExtension.extensionSettings().isCrossServerChat()) {
                return;
            }
        }

        String message = event.getMessage().getContentRaw();
        if (message.equals(this.PREFIX + onlineCommand.getBody())) {
            onlineCommand.execute(event, server, this.discordExtension.extensionSettings().getPrefix(), this.discordExtension.extensionSettings().getAllowedChats());
            return;
        }

        for (DiscordCommandsExtension extension : this.discordExtensions) {  // TODO slash commands?
            if (extension.processCommands(event, message, server)) {
                return;
            }
        }

        if (event.getChannel().getIdLong() == this.discordExtension.extensionSettings().getChatChannelID()) {
            if (event.getAuthor().isBot()) {
                if (this.discordExtension.extensionSettings().isCrossServerChat()) {
                    DiscordChatUtils.sendMessageCrossServer(event, server, this.discordExtension.extensionSettings().getPrefix());
                }
            } else {
                DiscordChatUtils.sendMessage(event, server);
            }
        }
    }

    public void sendChatMessage(ServerPlayerEntity player, String msg, String prefix) {
        if (this.jda == null || !this.jda.getStatus().isInit()) {
            return;
        }
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        String playerName = player.getName().getString();
        if (prefix.equals("")) {
            builder.setUsername(String.format("%s", playerName));
        } else {
            builder.setUsername(String.format("[%s] %s", prefix, playerName));
        }
        String uuid = player.getUuid().toString();
        builder.setAvatarUrl(String.format("https://crafatar.com/avatars/%s?overlay", uuid));
        builder.setContent(msg);
        this.whc.send(builder.build());
    }

    public void sendSysMessage(String msg, String prefix) {
        if (this.jda == null || !this.jda.getStatus().isInit()) {
            return;
        }
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        if (prefix.equals("")) {
            builder.setUsername("System :D");
        } else {
            builder.setUsername(String.format("[%s] System :D", prefix));
        }
        builder.setAvatarUrl("https://crafatar.com/avatars/749126bc-4467-41b4-be12-d24f4496cfad?overlay");
        builder.setContent(msg);
        this.whc.send(builder.build());
    }

    public void stop() {
        if (this.jda != null) {
            this.jda.shutdownNow();
        }
    }
}


