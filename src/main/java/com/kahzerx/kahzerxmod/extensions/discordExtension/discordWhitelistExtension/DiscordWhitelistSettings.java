package com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistExtension;

import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DiscordWhitelistSettings extends ExtensionSettings {
    private List<Long> whitelistChats;
    private long discordRoleID;
    private int nPlayers;
    public DiscordWhitelistSettings(HashMap<String, String> fileSettings, String name, String description) {
        super(fileSettings, name, description);
        DiscordWhitelistSettings file = (DiscordWhitelistSettings) this.processFileSettings(fileSettings.getOrDefault(name, null), this.getClass());
        this.whitelistChats = file != null && file.getWhitelistChats() != null ? file.getWhitelistChats() : new ArrayList<>();
        this.discordRoleID = file != null ? file.getDiscordRole() : 0L;
        this.nPlayers = file != null ? file.getNPlayers() : 1;
    }

    public List<Long> getWhitelistChats() {
        return whitelistChats;
    }

    public long getDiscordRole() {
        return discordRoleID;
    }

    public int getNPlayers() {
        return nPlayers;
    }

    public void setDiscordRoleID(long discordRoleID) {
        this.discordRoleID = discordRoleID;
    }

    public void setNPlayers(int nPlayers) {
        this.nPlayers = nPlayers;
    }

    public void addWhitelistChatID(long chatID) {
        this.whitelistChats.add(chatID);
    }

    public void removeWhitelistChatID(long chatID) {
        this.whitelistChats.remove(chatID);
    }

    @Override
    public String toString() {
        return "config{" +
                "name='" + this.getName() + '\'' +
                ", enabled=" + this.isEnabled() +
                ", description='" + this.isEnabled() + '\'' +
                ", whitelistChats=" + whitelistChats +
                ", discordRoleID=" + discordRoleID +
                ", nPlayers=" + nPlayers +
                '}';
    }
}
