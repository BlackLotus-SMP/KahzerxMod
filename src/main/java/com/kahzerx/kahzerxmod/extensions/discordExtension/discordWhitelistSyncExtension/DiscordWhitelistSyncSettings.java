package com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistSyncExtension;

import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DiscordWhitelistSyncSettings extends ExtensionSettings {
    private long notifyChannelID;
    private List<Long> validRoles;
    private long groupID;
    private boolean aggressive;
    public DiscordWhitelistSyncSettings(HashMap<String, String> fileSettings, String name, String description) {
        super(fileSettings, name, description);
        DiscordWhitelistSyncSettings file = (DiscordWhitelistSyncSettings) this.processFileSettings(fileSettings.getOrDefault(name, null), this.getClass());
        this.notifyChannelID = file != null ? file.getNotifyChannelID() : 0L;
        this.validRoles = file != null && file.getValidRoles() != null ? file.getValidRoles() : new ArrayList<>();
        this.groupID = file != null ? file.getGroupID() : 0L;
        this.aggressive = file != null && file.isAggressive();
    }

    public List<Long> getValidRoles() {
        return validRoles;
    }

    public long getNotifyChannelID() {
        return notifyChannelID;
    }

    public long getGroupID() {
        return groupID;
    }

    public boolean isAggressive() {
        return aggressive;
    }

    public void setNotifyChannelID(long notifyChannelID) {
        this.notifyChannelID = notifyChannelID;
    }

    public void setGroupID(long groupID) {
        this.groupID = groupID;
    }

    public void setAggressive(boolean aggressive) {
        this.aggressive = aggressive;
    }

    public void addValidRoleID(long chatID) {
        this.validRoles.add(chatID);
    }

    public void removeValidRoleID(long chatID) {
        this.validRoles.remove(chatID);
    }

    @Override
    public String toString() {
        return "config{" +
                "name='" + this.getName() + '\'' +
                ", enabled=" + this.isEnabled() +
                ", description='" + this.isEnabled() + '\'' +
                ", notifyChannelID=" + notifyChannelID +
                ", validRoles=" + validRoles +
                ", groupID=" + groupID +
                ", aggressive=" + aggressive +
                '}';
    }
}
