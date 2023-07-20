package com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DiscordSettings extends ExtensionSettings {
    private String token;
    private boolean crossServerChat;
    private String prefix;
    private boolean running;
    private long chatChannelID;
    private List<Long> allowedChats;
    private boolean shouldFeedback;
    public DiscordSettings(HashMap<String, String> fileSettings, String name, String description) {
        super(fileSettings, name, description);
        DiscordSettings file = (DiscordSettings) this.processFileSettings(fileSettings.getOrDefault(name, null), this.getClass());
        this.token = file != null && file.getToken() != null ? file.getToken() : "";
        this.crossServerChat = file != null && file.isCrossServerChat();
        this.prefix = file != null && file.getPrefix() != null ? file.getPrefix() : "";  // TODO old ver: replaceAll(" ", "_") why
        this.running = file != null && file.isRunning();
        this.chatChannelID = file != null ? file.getChatChannelID() : 0L;
        this.allowedChats = file != null && file.getAllowedChats() != null ? file.getAllowedChats() : new ArrayList<>();
        this.shouldFeedback = file != null && file.isShouldFeedback();
    }

    public boolean isShouldFeedback() {
        return shouldFeedback;
    }

    public void setShouldFeedback(boolean shouldFeedback) {
        this.shouldFeedback = shouldFeedback;
    }

    public String getToken() {
        return token;
    }

    public boolean isCrossServerChat() {
        return crossServerChat;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isRunning() {
        return running;
    }

    public long getChatChannelID() {
        return chatChannelID;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public List<Long> getAllowedChats() {
        return allowedChats;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setChatChannelID(long chatChannelID) {
        this.chatChannelID = chatChannelID;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setCrossServerChat(boolean crossServerChat) {
        this.crossServerChat = crossServerChat;
    }

    public void addAllowedChatID(long chatID) {
        this.allowedChats.add(chatID);
    }

    public void removeAllowedChatID(long chatID) {
        this.allowedChats.remove(chatID);
    }

    @Override
    public String toString() {
        return "DiscordSettings{" +
                "name='" + this.getName() + '\'' +
                ", enabled=" + this.isEnabled() +
                ", description='" + this.isEnabled() + '\'' +
                ", token='" + token + '\'' +
                ", crossServerChat=" + crossServerChat +
                ", prefix='" + prefix + '\'' +
                ", running=" + running +
                ", chatChannelID=" + chatChannelID +
                ", allowedChats=" + allowedChats +
                ", shouldFeedback=" + shouldFeedback +
                '}';
    }
}
