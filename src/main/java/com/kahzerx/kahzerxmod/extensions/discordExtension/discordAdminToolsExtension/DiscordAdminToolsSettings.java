package com.kahzerx.kahzerxmod.extensions.discordExtension.discordAdminToolsExtension;

import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;

import java.util.HashMap;
import java.util.List;

public class DiscordAdminToolsSettings extends ExtensionSettings {
    private List<Long> adminChats;
    private boolean shouldFeedback;
    public DiscordAdminToolsSettings(HashMap<String, Boolean> config, String name, String description, List<Long> adminChats, boolean shouldFeedback) {
        super(config, name, description);
        this.adminChats = adminChats;
        this.shouldFeedback = shouldFeedback;
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
}
