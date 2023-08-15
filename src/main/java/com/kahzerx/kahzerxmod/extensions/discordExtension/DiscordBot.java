package com.kahzerx.kahzerxmod.extensions.discordExtension;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.kahzerx.kahzerxmod.extensions.discordExtension.commands.GenericCommand;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension.DiscordExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.utils.DiscordChatUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

public class DiscordBot extends ListenerAdapter implements DiscordBotInterface {
    private final MinecraftServer server;
    private final DiscordExtension discordExtension;
    private String prefix = "!";  // TODO custom?
    private JDA jda = null;
    private WebhookClient whc;
    private final List<DiscordGenericExtension> discordExtensions = new ArrayList<>();

    public DiscordBot(MinecraftServer server, DiscordExtension discordExtension) {  // TODO slash commands?
        this.server = server;
        this.discordExtension = discordExtension;
    }

    public EventResponse start() {
        try {
            this.initJDA();
        } catch (InterruptedException | LoginException e) {
            this.jda = null;
            e.printStackTrace();
            return new EventResponse(false, "Unable to start the discord bot, check the logs for more information, the token does not seem to be valid!");
        }
        try {
            return this.updateWebHooks(true);
        } catch (Exception e) {
            e.printStackTrace();
            return new EventResponse(false, "Unable to start the discord bot, check the logs for more information, the chat channel ID does not seem to be valid!");
        }
    }

    private void initJDA() throws InterruptedException, LoginException {
        this.stop();
        this.jda = JDABuilder.createDefault(discordExtension.extensionSettings().getToken()).addEventListeners(this).build();
        this.jda.awaitReady();
    }

    public void updateCommandPrefix(String prefix) {  // TODO update prefix on command
        this.prefix = prefix;
        for (DiscordGenericExtension extension : this.discordExtensions) {
            for (GenericCommand command : extension.getCommands()) {
                command.setCommandPrefix(prefix);
            }
        }
    }

    public EventResponse onUpdateChannelID() {
        try {
            return this.updateWebHooks(false);
        } catch (Exception e) {
            e.printStackTrace();
            return new EventResponse(false, "There has been an error trying to use this chat channel ID, check the console for more information");
        }
    }

    private EventResponse updateWebHooks(boolean justStartedBot) throws LoginException, InterruptedException {
        if (!justStartedBot) {
            this.initJDA();
        }
        TextChannel channel = this.jda.getTextChannelById(this.discordExtension.extensionSettings().getChatChannelID());
        if (channel == null) {
            return new EventResponse(false, "The extension has an invalid Chat Channel ID!");
        }
        List<Webhook> webhookList = channel.retrieveWebhooks().complete();
        boolean webhookFlag = false;
        for (Webhook webdook : webhookList) {
            if (webdook.getName().equals("ChatBridge")) {
//                channel.deleteWebhookById(webdook.getId());  // FIXME if another webhook with the same name already exists, the `WebhookClient.withUrl` logic crashes
                webhookFlag = true;
                this.whc = WebhookClient.withUrl(webdook.getUrl());
                break;
            }
        }
        if (!webhookFlag) {
            Webhook webook = this.jda.getTextChannelById(this.discordExtension.extensionSettings().getChatChannelID()).createWebhook("ChatBridge").complete();
            this.whc = WebhookClient.withUrl(webook.getUrl());
        }
        return new EventResponse(true, "");
    }

    public void addExtensions(DiscordGenericExtension... extensions) {
        for (DiscordGenericExtension e : extensions) {
            if (!this.discordExtensions.contains(e)) {
                this.discordExtensions.add(e);
            }
            for (GenericCommand command : e.getCommands()) {
                command.setCommandPrefix(prefix);
            }
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
        for (DiscordGenericExtension extension : this.discordExtensions) {  // TODO slash commands?
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
        if (!this.isReady()) {
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
        if (!this.isReady()) {
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

    public boolean isReady() {
        return this.jda != null && this.jda.getStatus().isInit();
    }

    public Guild getGuild(long id) {
        return this.jda.getGuildById(id);
    }
}


