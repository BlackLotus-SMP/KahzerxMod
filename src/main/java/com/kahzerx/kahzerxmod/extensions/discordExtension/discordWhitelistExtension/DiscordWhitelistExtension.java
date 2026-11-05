package com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistExtension;

import net.minecraft.server.players.NameAndId;
import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.database.ServerQuery;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordGenericExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.commands.AddCommand;
import com.kahzerx.kahzerxmod.extensions.discordExtension.commands.InfoCommand;
import com.kahzerx.kahzerxmod.extensions.discordExtension.commands.ListCommand;
import com.kahzerx.kahzerxmod.extensions.discordExtension.commands.RemoveCommand;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension.DiscordExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.utils.DiscordChatUtils;
import com.kahzerx.kahzerxmod.extensions.discordExtension.utils.DiscordUtils;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static net.minecraft.commands.SharedSuggestionProvider.suggest;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class DiscordWhitelistExtension extends DiscordGenericExtension implements Extensions {
    private DiscordExtension discordExtension;
    private Connection conn;

    public static boolean isExtensionEnabled = false;

    private ExtensionManager em = null;

    private final AddCommand addCommand = new AddCommand();
    private final RemoveCommand removeCommand = new RemoveCommand();
    private final ListCommand listCommand = new ListCommand();
    private final InfoCommand infoCommand = new InfoCommand();

    public DiscordWhitelistExtension(HashMap<String, String> fileSettings) {
        super(new DiscordWhitelistSettings(fileSettings, "discordWhitelist", "Enables !list, !info, !add and !remove commands along with nPlayers that specifies how many minecraft players a discord user can add; There is also an optional discordRole that will be given to the discord user on !add and deleted on !remove."));
        this.addCommands(this.addCommand, this.removeCommand, this.listCommand, this.infoCommand);
    }

    @Override
    public void onExtensionsReady(ExtensionManager em) {
        this.em = em;
        this.discordExtension = (DiscordExtension) em.getExtensions().get("discord");
        if (this.extensionSettings().isEnabled() && !this.discordExtension.extensionSettings().isEnabled()) {
            this.extensionSettings().setEnabled(false);
            em.saveSettings();
        }
    }

    public DiscordExtension getDiscordExtension() {
        return discordExtension;
    }

    public ExtensionManager getEm() {
        return em;
    }

    @Override
    public void onCreateDatabase(Connection conn) {
        this.conn = conn;
        if (!this.getSettings().isEnabled()) {
            return;
        }
        if (!discordExtension.getSettings().isEnabled()) {
            return;
        }
        try {
            String createDiscordDatabase = "CREATE TABLE IF NOT EXISTS `discord`(" +
                    "`uuid` VARCHAR(50) UNIQUE NOT NULL," +
                    "`discordID` NUMERIC NOT NULL," +
                    "FOREIGN KEY(uuid) REFERENCES player(uuid)," +
                    "PRIMARY KEY (uuid, discordID));";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(createDiscordDatabase);
            stmt.close();

            String createBannedDiscordDatabase = "CREATE TABLE IF NOT EXISTS `discord_banned_v2`(" +
                    "`discordID` NUMERIC PRIMARY KEY NOT NULL);";
            Statement stmt_ = conn.createStatement();
            stmt_.executeUpdate(createBannedDiscordDatabase);
            stmt_.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServerRun(MinecraftServer minecraftServer) {
        if (!this.getSettings().isEnabled()) {
            return;
        }
        if (!discordExtension.getSettings().isEnabled()) {
            return;
        }
        isExtensionEnabled = true;
    }

    @Override
    public void onServerStarted(MinecraftServer minecraftServer) {
        this.getDiscordExtension().getBot().addExtensions(this);
    }

    public boolean isDiscordBanned(long discordID) {
        boolean banned = false;
        try {
            String getBan = "SELECT discordID FROM discord_banned_v2 WHERE discordID = ?;";
            PreparedStatement ps = conn.prepareStatement(getBan);
            ps.setLong(1, discordID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                banned = true;
            }
            rs.close();
            ps.close();
            return banned;
        } catch (SQLException e) {
            e.printStackTrace();
            return banned;
        }
    }

    public boolean canRemove(long discordID, String uuid) {
        boolean remove = false;
        try {
            String getPlayers = "SELECT discordID FROM discord WHERE uuid = ? AND discordID = ?;";
            PreparedStatement ps = conn.prepareStatement(getPlayers);
            ps.setString(1, uuid);
            ps.setLong(2, discordID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                remove = true;
            }
            rs.close();
            ps.close();
            return remove;
        } catch (SQLException e) {
            e.printStackTrace();
            return remove;
        }
    }

    public boolean alreadyAddedBySomeone(String uuid) {
        boolean added = false;
        try {
            String already = "SELECT * FROM discord WHERE uuid = ?;";
            PreparedStatement ps = conn.prepareStatement(already);
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                added = true;
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return added;
    }

    public boolean alreadyAddedBySomeone(UUID uuid) {
        return alreadyAddedBySomeone(uuid.toString());
    }

    public boolean userReachedMaxPlayers(long discordID) {
        boolean canAdd = false;
        try {
            String getPlayers = "SELECT COUNT(*) AS rows FROM discord WHERE discordID = ?;";
            PreparedStatement ps = conn.prepareStatement(getPlayers);
            ps.setLong(1, discordID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt("rows") < this.extensionSettings().getNPlayers()) {
                    canAdd = true;
                }
            } else {
                canAdd = true;
            }
            rs.close();
            ps.close();
            return !canAdd;
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean isPlayerBanned(String uuid) {
        try {
            boolean isBanned = false;
            String getPlayers = "SELECT discordID FROM discord WHERE uuid = ?;";
            PreparedStatement ps = conn.prepareStatement(getPlayers);
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long discordID = rs.getLong("discordID");
                isBanned = isDiscordBanned(discordID);
            }
            rs.close();
            ps.close();
            return isBanned;
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public long getDiscordID(String uuid) {
        long id = 0L;
        try {
            String getPlayers = "SELECT discordID FROM discord WHERE uuid = ?;";
            PreparedStatement ps = conn.prepareStatement(getPlayers);
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                id = rs.getLong("discordID");
            }
            rs.close();
            ps.close();
            return id;
        } catch (SQLException e) {
            e.printStackTrace();
            return id;
        }
    }

    public String getMinecraftNick(String uuid) {
        String name = "";
        try {
            String getName = "SELECT name  FROM player WHERE uuid = ?;";
            PreparedStatement ps = conn.prepareStatement(getName);
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                name = rs.getString("name");
            }
            rs.close();
            ps.close();
            return name;
        } catch (SQLException e) {
            e.printStackTrace();
            return name;
        }
    }

    public void banDiscord(long discordID) {
        try {
            String insertPlayer = "INSERT OR IGNORE INTO discord_banned_v2 (discordID) VALUES (?);";
            PreparedStatement ps = conn.prepareStatement(insertPlayer);
            ps.setLong(1, discordID);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void pardonDiscord(long discordID) {
        try {
            String insertPlayer = "DELETE FROM discord_banned_v2 WHERE discordID = ?;";
            PreparedStatement ps = conn.prepareStatement(insertPlayer);
            ps.setLong(1, discordID);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getWhitelistedPlayers(long discordID) {
        ArrayList<String> players = new ArrayList<>();
        try {
            String q = "SELECT uuid FROM discord WHERE discordID = ?;";
            PreparedStatement ps = this.conn.prepareStatement(q);
            ps.setLong(1, discordID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                players.add(rs.getString("uuid"));
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return players;
    }

    public ArrayList<Long> getDiscordIDs() {
        ArrayList<Long> IDs = new ArrayList<>();
        try {
            String q = "SELECT discordID FROM discord;";
            Statement stmt = this.conn.createStatement();
            ResultSet rs = stmt.executeQuery(q);
            while (rs.next()) {
                long newID = rs.getLong("discordID");
                if (!IDs.contains(newID)) {
                    IDs.add(newID);
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return IDs;
    }

    public void deletePlayer(long discordID, String uuid) {
        try {
            String delete = "DELETE FROM discord WHERE uuid = ? AND discordID = ?;";
            PreparedStatement ps = conn.prepareStatement(delete);
            ps.setString(1, uuid);
            ps.setLong(2, discordID);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addPlayer(long discordID, String uuid, String playerName) {
        try {
            ServerQuery q = new ServerQuery(conn);
            q.insertPlayerUUID(uuid, playerName);

            String insertPlayer = "INSERT OR IGNORE INTO discord (uuid, discordID) VALUES (?, ?);";
            PreparedStatement ps = conn.prepareStatement(insertPlayer);
            ps.setString(1, uuid);
            ps.setLong(2, discordID);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void tryVanillaBan(UserBanList banList, NameAndId profile, MinecraftServer server) {
        if (!banList.isBanned(profile)) {
            UserBanListEntry playerEntry = new UserBanListEntry(profile, null, "DiscordBan", null, null);
            banList.add(playerEntry);
            ServerPlayer serverPlayerEntity = server.getPlayerList().getPlayer(profile.id());
            if (serverPlayerEntity != null) {
                serverPlayerEntity.connection.disconnect(Component.literal("You have been banned :)"));
            }
        }
    }

    public void tryVanillaPardon(UserBanList banList, NameAndId profile) {
        if (banList.isBanned(profile)) {
            banList.remove(profile);
        }
    }

    public void tryVanillaWhitelistRemove(UserWhiteList whitelist, NameAndId profile, MinecraftServer server) {
        if (whitelist.isWhiteListed(profile)) {
            UserWhiteListEntry whitelistEntry = new UserWhiteListEntry(profile);
            whitelist.remove(whitelistEntry);
            ServerPlayer serverPlayerEntity = server.getPlayerList().getPlayer(profile.id());
            if (serverPlayerEntity != null) {
                serverPlayerEntity.connection.disconnect(Component.literal("Byee~"));
            }
        }
    }

    @Override
    public DiscordWhitelistSettings extensionSettings() {
        return (DiscordWhitelistSettings) this.getSettings();
    }

    @Override
    public void onExtensionEnabled(CommandSourceStack source) {
        this.onCreateDatabase(this.conn);
        isExtensionEnabled = true;
    }

    @Override
    public void onExtensionDisabled(CommandSourceStack source) {
        isExtensionEnabled = false;
    }

    @Override
    public boolean processCommands(MessageReceivedEvent event, String message, MinecraftServer server) {
        CommandFound commandFound = this.findValidCommand(event, message, this.extensionSettings().getWhitelistChats(), this.getDiscordExtension().extensionSettings().isShouldFeedback(), this.getDiscordExtension().extensionSettings().getPrefix());
        if (!commandFound.found()) {
            return false;
        }
        if (commandFound.command() != null) {
            commandFound.command().executeCommand(event, server, this.getEm());
            return true;
        }
        return false;
    }

    @Override
    public void settingsCommand(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.  // TODO Interact with the description and add functionality maybe other than just if its enabled and the description
                then(literal("discordRoleID").
                        then(argument("discordRole", LongArgumentType.longArg()).
                                suggests((c, b) -> suggest(new String[]{"0"}, b)).
                                executes(context -> {
                                    extensionSettings().setDiscordRoleID(LongArgumentType.getLong(context, "discordRole"));
                                    context.getSource().sendSuccess(() -> this.getLongSettingMessage(true, "discordRoleID", this.extensionSettings().getDiscordRole(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName()), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendSuccess(() -> Component.literal("\n" + this.extensionSettings().getName() + "/discordRoleID\n").withStyle(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("Role that gets added to every discord user that runs the !add command successfully\n", ChatFormatting.GRAY).withStyle(style -> style.withBold(false))).
                                    append(this.getLongSettingMessage(false, "discordRoleID", this.extensionSettings().getDiscordRole(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName())), false);
                            return 1;
                        })).
                then(literal("nPlayers").
                        then(argument("nPlayers", IntegerArgumentType.integer()).
                                suggests((c, b) -> suggest(new String[]{"0"}, b)).
                                executes(context -> {
                                    extensionSettings().setNPlayers(IntegerArgumentType.getInteger(context, "nPlayers"));
                                    context.getSource().sendSuccess(() -> this.getLongSettingMessage(true, "nPlayers", this.extensionSettings().getNPlayers(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName()), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendSuccess(() -> Component.literal("\n" + this.extensionSettings().getName() + "/nPlayers\n").withStyle(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("Amount of players a discord user can add to the whitelist\n", ChatFormatting.GRAY).withStyle(style -> style.withBold(false))).
                                    append(this.getLongSettingMessage(false, "nPlayers", this.extensionSettings().getNPlayers(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName())), false);
                            return 1;
                        })).
                then(literal("whitelistChats").
                        then(literal("add").
                                then(argument("chatID", LongArgumentType.longArg()).
                                        suggests((c, b) -> suggest(new String[]{"1234"}, b)).
                                        executes(context -> {
                                            long chat = LongArgumentType.getLong(context, "chatID");
                                            if (this.extensionSettings().getWhitelistChats().contains(chat)) {
                                                context.getSource().sendSuccess(() -> MarkEnum.CROSS.appendText(this.formatLongID("The chat ID ", chat, " was already on the list", true, false, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "whitelistChats")), false);
                                            } else {
                                                this.extensionSettings().addWhitelistChatID(chat);
                                                context.getSource().sendSuccess(() -> MarkEnum.TICK.appendText(this.formatLongID("The chat with ID ", chat, " has been", true, true, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "whitelistChats")), false);
                                                this.em.saveSettings();
                                            }
                                            return 1;
                                        }))).
                        then(literal("remove").
                                then(argument("chatID", LongArgumentType.longArg()).
                                        suggests((c, b) -> suggest(this.extensionSettings().getWhitelistChats().stream().map(Object::toString), b)).
                                        executes(context -> {
                                            long chat = LongArgumentType.getLong(context, "chatID");
                                            if (this.extensionSettings().getWhitelistChats().contains(chat)) {
                                                this.extensionSettings().removeWhitelistChatID(chat);
                                                context.getSource().sendSuccess(() -> MarkEnum.TICK.appendText(this.formatLongID("The chat with ID ", chat, " has been", false, true, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "whitelistChats")), false);
                                                this.em.saveSettings();
                                            } else {
                                                context.getSource().sendSuccess(() -> MarkEnum.CROSS.appendText(this.formatLongID("The chat ID ", chat, " does not exist!", false, false, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "whitelistChats")), false);
                                            }
                                            return 1;
                                        }))).
                        then(literal("list").
                                executes(context -> {
                                    MutableComponent chats = Component.literal("");
                                    int chatCount = this.extensionSettings().getWhitelistChats().size();
                                    if (chatCount == 0) {
                                        chats.append(Component.literal("Not set!").withStyle(style -> style.
                                                withColor(ChatFormatting.RED).
                                                withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to add!"))).
                                                withClickEvent(new ClickEvent.SuggestCommand(String.format("/%s %s whitelistChats add ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName())))));
                                    } else {
                                        chats.
                                                append(Component.literal("[+]").withStyle(style -> style.
                                                        withColor(ChatFormatting.GREEN).
                                                        withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to add!"))).
                                                        withClickEvent(new ClickEvent.SuggestCommand(String.format("/%s %s whitelistChats add ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName()))))).
                                                append(Component.literal(" ")).
                                                append(Component.literal("[-]\n").withStyle(style -> style.
                                                        withColor(ChatFormatting.RED).
                                                        withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to remove!"))).
                                                        withClickEvent(new ClickEvent.SuggestCommand(String.format("/%s %s whitelistChats remove ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName())))));
                                        for (int i = 0; i < chatCount; i++) {
                                            long chat = this.extensionSettings().getWhitelistChats().get(i);
                                            chats.
                                                    append(MarkEnum.DOT.appendText(Component.literal(String.format("%d", chat)).withStyle(style -> style.
                                                            withHoverEvent(new HoverEvent.ShowText(Component.literal(String.format("Click to copy %d", chat)))).
                                                            withBold(false).
                                                            withClickEvent(new ClickEvent.CopyToClipboard(String.format("%d", chat)))), ChatFormatting.GRAY)).
                                                    append(Component.literal(" ")).
                                                    append(MarkEnum.CROSS.getFormattedIdentifier().withStyle(style -> style.
                                                            withHoverEvent(new HoverEvent.ShowText(Component.literal(String.format("Click to delete %d", chat)))).
                                                            withClickEvent(new ClickEvent.RunCommand(String.format("/%s %s whitelistChats remove %d", this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), chat))))).
                                                    append(i == chatCount-1 ? Component.literal("") : Component.literal("\n"));
                                        }
                                    }
                                    context.getSource().sendSuccess(() -> Component.literal("\n" + this.extensionSettings().getName() + "/whitelistChats/list" + "\n").withStyle(style -> style.withBold(true)).
                                            append(chats), false);
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendSuccess(() -> Component.literal("\n" + this.extensionSettings().getName() + "/whitelistChats\n").withStyle(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("Chats where !info, !add, !remove and !list work\n", ChatFormatting.GRAY).withStyle(style -> style.withBold(false))).
                                    append(Component.literal("[Chats]").withStyle(style -> style.
                                            withColor(ChatFormatting.DARK_GRAY).
                                            withUnderlined(true).
                                            withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to display the already added whitelist chat IDs"))).
                                            withClickEvent(new ClickEvent.RunCommand(String.format("/%s %s whitelistChats list", this.em.getSettingsBaseCommand(), this.extensionSettings().getName()))))), false);
                            return 1;
                        }));
    }
}
