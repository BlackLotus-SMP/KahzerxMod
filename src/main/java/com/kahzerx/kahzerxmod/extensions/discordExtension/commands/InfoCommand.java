package com.kahzerx.kahzerxmod.extensions.discordExtension.commands;

import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordPermission;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension.DiscordExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistExtension.DiscordWhitelistExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.utils.DiscordChatUtils;
import com.mojang.authlib.GameProfile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class InfoCommand extends GenericCommand {
    public InfoCommand() {
        super("info", "show information about whitelisted players", DiscordPermission.WHITELIST_CHAT);
    }

    @Override
    public void executeCommand(MessageReceivedEvent event, MinecraftServer server, ExtensionManager extensionManager) {
        DiscordWhitelistExtension discordWhitelistExtension = extensionManager.getDiscordWhitelistExtension();
        DiscordExtension discordExtension = extensionManager.getDiscordExtension();
        String serverPrefix = discordExtension.extensionSettings().getPrefix();
        boolean feedback = discordExtension.extensionSettings().isShouldFeedback();
        String msg = event.getMessage().getContentRaw();
        if (msg.split(" ").length == 2) {
            String playerName = msg.split(" ")[1];
            Optional<GameProfile> profile = server.getUserCache().findByName(playerName);
            if (profile.isEmpty()) {
                EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**Not premium.**"}, serverPrefix, true, Color.RED, true, feedback);
                if (embed != null) {
                    event.getChannel().sendMessageEmbeds(embed.build()).queue();
                }
                return;
            }
            UUID playerUuid = profile.get().getId();
            if (discordWhitelistExtension.alreadyAddedBySomeone(playerUuid)) {
                try {
                    File skinPng = saveImage(playerUuid.toString(), server, serverPrefix, event, feedback);
                    PlayerData embeDitto = collectData(skinPng, playerUuid.toString(), discordWhitelistExtension, event.getGuild(), server);
                    EmbedBuilder embedFinal = new EmbedBuilder();
                    embedFinal.setTitle(embeDitto.mcNick);
                    embedFinal.setColor(new Color(0xABDED7));
                    embedFinal.setDescription(String.format("Added by: %s", embeDitto.dsNick));
                    embedFinal.setThumbnail("attachment://skin.png");
                    embedFinal.addField("Uuid: ", embeDitto.playerUuid, false);
                    embedFinal.addField("Server Role: ", embeDitto.serverRole, true);
                    embedFinal.addField("Server Status: ", embeDitto.status, true);
                    event.getChannel().sendMessageEmbeds(embedFinal.build()).addFile(embeDitto.skinPath, "skin.png").queue();
                } catch (IOException e) {
                    e.printStackTrace();
                    EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**There has been an error**"}, serverPrefix, true, Color.RED, true, feedback);
                    if (embed != null) {
                        event.getChannel().sendMessageEmbeds(embed.build()).queue();
                    }
                }
            } else {
                EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**Not in whitelist.**"}, serverPrefix, true, Color.RED, true, feedback);
                if (embed != null) {
                    event.getChannel().sendMessageEmbeds(embed.build()).queue();
                }
            }
        } else {
            long dsId = event.getAuthor().getIdLong();
            ArrayList<String> whiteList = discordWhitelistExtension.getWhitelistedPlayers(dsId);
            // TODO if there is no whitelisted players, display a message!
            for (String uuid: whiteList){
                try {
                    File skinPng = saveImage(uuid, server, serverPrefix, event, feedback);
                    PlayerData embeDitto = collectData(skinPng, uuid, discordWhitelistExtension, event.getGuild(), server);
                    EmbedBuilder embedFinal = new EmbedBuilder();
                    embedFinal.setTitle(embeDitto.mcNick);
                    embedFinal.setColor(new Color(0xABDED7));
                    embedFinal.setDescription(String.format("Added by: %s", embeDitto.dsNick));
                    embedFinal.setThumbnail("attachment://skin.png");
                    embedFinal.addField("Uuid: ", embeDitto.playerUuid, false);
                    embedFinal.addField("Server Role: ", embeDitto.serverRole, true);
                    embedFinal.addField("Server Status: ", embeDitto.status, true);
                    event.getChannel().sendMessageEmbeds(embedFinal.build()).addFile(embeDitto.skinPath, "skin.png").queue();

                } catch (IOException e) {
                    e.printStackTrace();
                    EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**There has been an error**"}, serverPrefix, true, Color.RED, true, feedback);
                    if (embed != null) {
                        event.getChannel().sendMessageEmbeds(embed.build()).queue();
                    }
                }
            }

        }

    }

    public File saveImage(String uuid, MinecraftServer server, String serverPrefix, MessageReceivedEvent event, Boolean feedback) throws IOException {
        URL url = new URL(String.format("https://crafatar.com/renders/body/%s?overlay", uuid));
        Path worldPath = server.getSavePath(WorldSavePath.ROOT);
        File finalPath = new File(worldPath.toFile().getAbsolutePath() + String.format("skins/%s.png", uuid));
        File directoryPath = finalPath.getParentFile();
        if (!directoryPath.exists()) {
            Files.createDirectories(directoryPath.toPath());
        }
        File[] skins = directoryPath.listFiles();
        if (skins == null) {
            EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**Could not list the files :c**"}, serverPrefix, true, Color.RED, true, feedback);
            if (embed != null) {
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
            return null;
        }
        boolean cuteFlag = false;
        for (File skin: skins) {
            String fileName = skin.getName();
            if (fileName.equals(String.format("%s.png", uuid))) {
                BasicFileAttributes attributes = Files.readAttributes(finalPath.toPath(), BasicFileAttributes.class);
                boolean dateFlag = checkTime(attributes.creationTime());
                if(dateFlag){
                    boolean delete = skin.delete();
                }else {
                    cuteFlag = true;
                }
                break;
            }
        }
        if (!cuteFlag) {
            InputStream input = url.openStream();
            OutputStream output = new FileOutputStream(finalPath);

            byte[] b = new byte[2048];
            int length;

            while ((length = input.read(b)) != -1) {
                output.write(b, 0, length);
            }
            input.close();
            output.close();
        }
        return finalPath;
    }
    public PlayerData collectData(File skinPng, String uuid, DiscordWhitelistExtension extension, Guild guild, MinecraftServer server) {
        long dsId = extension.getDiscordID(uuid);
        Member dsName = guild.retrieveMemberById(dsId).complete();
        String mcName = extension.getMinecraftNick(uuid);
        Team serverTeam = server.getScoreboard().getTeam(mcName);
        String playerRole = "**Has no role**";
        if (serverTeam != null) {
            String playerTeam = serverTeam.getName();
            playerRole = String.format("**%s**", playerTeam);

        }
        String status = "**ACTIVE**";
        if (extension.isPlayerBanned(uuid)) {
            status = "**BANNED**";
        }
        return new PlayerData(skinPng, mcName, dsName.getEffectiveName(), uuid, status, playerRole);
    }

    static class PlayerData{
        public File skinPath;
        public String mcNick;
        public String dsNick;
        public String playerUuid;
        public String status;
        public String serverRole;
        public PlayerData(File skinPath, String mcNick, String dsNick, String playerUuid, String status, String serverRole) {
            this.skinPath = skinPath;
            this.mcNick = mcNick;
            this.dsNick = dsNick;
            this.playerUuid = playerUuid;
            this.status = status;
            this.serverRole = serverRole;
        }
    }

    public boolean checkTime(FileTime creacionSkin) {
        LocalDateTime creacionDate = creacionSkin.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return LocalDateTime.now().isAfter(creacionDate.plusDays(1));
    }

}


