package com.kahzerx.kahzerxmod.extensions.discordExtension;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.kahzerx.kahzerxmod.extensions.discordExtension.commands.OnlineCommand;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension.DiscordExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension.DiscordSettings;
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

public class DiscordListener extends ListenerAdapter {
    public static List<DiscordCommandsExtension> discordExtensions = new ArrayList<>();
    public static final String commandPrefix = "!";

    public static JDA jda = null;
    public static WebhookClient webhookC = null;
    private static String channelId = "";
    private static String token = "";
    public static boolean chatbridge = false;
    private static DiscordSettings discordSettings = null;

    private final MinecraftServer server;

    private final OnlineCommand onlineCommand = new OnlineCommand(commandPrefix);

    public DiscordListener(MinecraftServer server) {
        this.server = server;
    }

    public static void start(MinecraftServer server, String t, String channel, DiscordExtension discordExtension) {
        channelId = channel;
        token = t;
        discordSettings = discordExtension.extensionSettings();
        try {
            discordExtension.extensionSettings().setRunning(false);
            chatbridge = false;
            jda = JDABuilder.createDefault(t).addEventListeners(new DiscordListener(server)).build();
            jda.awaitReady();
            List<Webhook> webhookList = jda.getTextChannelById(channelId).retrieveWebhooks().complete();
            boolean webhookFlag = false;
            for (Webhook webdook: webhookList){
                if (webdook.getName().equals("ChatBridge")){
                    webhookFlag = true;
                    String WebhookUrl = webdook.getUrl();
                    webhookC = WebhookClient.withUrl(WebhookUrl);
                    break;
                }
            }
            if (!webhookFlag){
                Webhook webook = jda.getTextChannelById(channelId).createWebhook("ChatBridge").complete();
                String WebhookUrl = webook.getUrl();
                webhookC = WebhookClient.withUrl(WebhookUrl);
            }
            discordExtension.extensionSettings().setRunning(true);
            chatbridge = true;
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
            System.err.println("REMEMBER TO SET A BOT CORRECTLY!");
        }
    }

    public static void stop() {
        if (jda != null) {
            jda.shutdownNow();
        }
        chatbridge = false;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!chatbridge) {
            return;
        }
        if (event.getMessage().getContentDisplay().equals("")) {
            return;
        }
        if (event.getMessage().getContentRaw().equals("")) {
            return;
        }
        if (event.getAuthor().isBot()) {
            if (!discordSettings.isCrossServerChat()) {
                return;
            }
        }

        String message = event.getMessage().getContentRaw();
        if (message.equals(commandPrefix + onlineCommand.getBody())) {
            onlineCommand.execute(event, server, discordSettings.getPrefix(), discordSettings.getAllowedChats());
            return;
        }

        for (DiscordCommandsExtension extension : discordExtensions) {
            if (extension.processCommands(event, message, server)) {
                return;
            }
        }

        if (event.getChannel().getIdLong() == discordSettings.getChatChannelID()) {
            if (event.getAuthor().isBot()) {
                if (discordSettings.isCrossServerChat()) {
                    DiscordChatUtils.sendMessageCrossServer(event, server, discordSettings.getPrefix());
                }
            } else {
                DiscordChatUtils.sendMessage(event, server);
            }
        }
    }

    public static void sendChatMessage(ServerPlayerEntity player, String msg, String prefix){
        if (!chatbridge){
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
        webhookC.send(builder.build());
    }

    public static void sendSysMessage(String msg, String prefix){
        if (!chatbridge){
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
        webhookC.send(builder.build());
    }
}


