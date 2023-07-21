package com.kahzerx.kahzerxmod.extensions.discordExtension.discordAdminToolsExtension;

import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DiscordAdminToolsSettings extends ExtensionSettings {
    private List<Long> adminChats;
    private boolean shouldFeedback;
    public DiscordAdminToolsSettings(HashMap<String, String> fileSettings, String name, String description) {
        super(fileSettings, name, description);
        DiscordAdminToolsSettings file = (DiscordAdminToolsSettings) this.processFileSettings(fileSettings.getOrDefault(name, null), this.getClass());
        this.adminChats = file != null && file.getAdminChats() != null ? file.getAdminChats() : new ArrayList<>();
        this.shouldFeedback = file != null && file.isShouldFeedback();;
    }

    public boolean isShouldFeedback() {
        return shouldFeedback;
    }

    public void setShouldFeedback(boolean shouldFeedback) {
        this.shouldFeedback = shouldFeedback;
    }

    public List<Long> getAdminChats() {
        return adminChats;
    }

    public void addAdminChatID(long chatID) {
        this.adminChats.add(chatID);
    }

    public void removeAdminChatID(long chatID) {
        this.adminChats.remove(chatID);
    }

    @Override
    public String toString() {
        return "config{" +
                "name='" + this.getName() + '\'' +
                ", enabled=" + this.isEnabled() +
                ", description='" + this.isEnabled() + '\'' +
                ", adminChats=" + adminChats +
                ", shouldFeedback=" + shouldFeedback +
                '}';
    }
}
