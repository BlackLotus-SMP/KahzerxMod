package com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension;

import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DiscordSettings extends ExtensionSettings {
    private String token;
    private boolean crossServerChat;
    private String prefix;
    private String commandPrefix;
    private long chatChannelID;
    private final List<Long> allowedChats;
    private boolean shouldFeedback;
    public DiscordSettings(HashMap<String, String> fileSettings, String name, String description) {
        super(fileSettings, name, description);
        DiscordSettings file = (DiscordSettings) this.processFileSettings(fileSettings.getOrDefault(name, null), this.getClass());
        this.token = file != null && file.getToken() != null ? file.getToken() : "";
        this.crossServerChat = file != null && file.isCrossServerChat();
        this.prefix = file != null && file.getPrefix() != null ? file.getPrefix() : "";  // TODO old ver: replaceAll(" ", "_") why
        this.commandPrefix = file != null && file.getCommandPrefix() != null ? file.getCommandPrefix() : "!";
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

    public String getCommandPrefix() {
        return commandPrefix;
    }

    public long getChatChannelID() {
        return chatChannelID;
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

    public void setCommandPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
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
        return "config{" +
                "name='" + this.getName() + '\'' +
                ", enabled=" + this.isEnabled() +
                ", description='" + this.isEnabled() + '\'' +
                ", token='" + token + '\'' +
                ", crossServerChat=" + crossServerChat +
                ", prefix='" + prefix + '\'' +
                ", chatChannelID=" + chatChannelID +
                ", allowedChats=" + allowedChats +
                ", shouldFeedback=" + shouldFeedback +
                '}';
    }
}
