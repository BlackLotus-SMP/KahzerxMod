package com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistExtension;

import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.database.ServerQuery;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordCommandsExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordGenericExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.DiscordListener;
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
import net.minecraft.server.*;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static net.minecraft.command.CommandSource.suggestMatching;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DiscordWhitelistExtension extends DiscordGenericExtension implements Extensions, DiscordCommandsExtension {
    private DiscordExtension discordExtension;
    private Connection conn;

    public static boolean isExtensionEnabled = false;

    private ExtensionManager em = null;

    private final AddCommand addCommand = new AddCommand(DiscordListener.commandPrefix);
    private final RemoveCommand removeCommand = new RemoveCommand(DiscordListener.commandPrefix);
    private final ListCommand listCommand = new ListCommand(DiscordListener.commandPrefix);
    private final InfoCommand infoCommand = new InfoCommand(DiscordListener.commandPrefix);

    public DiscordWhitelistExtension(HashMap<String, String> fileSettings) {
        super(new DiscordWhitelistSettings(fileSettings, "discordWhitelist", "Enables !list, !add and !remove commands along with nPlayers that specifies how many minecraft players a discord user can add; There is also an optional discordRole that will be given to the discord user on !add and deleted on !remove."));
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
        DiscordListener.discordExtensions.add(this);
        isExtensionEnabled = true;
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
            return canAdd;
        } catch (SQLException e) {
            e.printStackTrace();
            return canAdd;
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

    public void tryVanillaBan(BannedPlayerList banList, GameProfile profile, MinecraftServer server) {
        if (!banList.contains(profile)) {
            BannedPlayerEntry playerEntry = new BannedPlayerEntry(profile, null, "DiscordBan", null, null);
            banList.add(playerEntry);
            ServerPlayerEntity serverPlayerEntity = server.getPlayerManager().getPlayer(profile.getId());
            if (serverPlayerEntity != null) {
                serverPlayerEntity.networkHandler.disconnect(Text.literal("You have been banned :)"));
            }
        }
    }

    public void tryVanillaPardon(BannedPlayerList banList, GameProfile profile) {
        if (banList.contains(profile)) {
            banList.remove(profile);
        }
    }

    public void tryVanillaWhitelistRemove(Whitelist whitelist, GameProfile profile, MinecraftServer server) {
        if (whitelist.isAllowed(profile)) {
            WhitelistEntry whitelistEntry = new WhitelistEntry(profile);
            whitelist.remove(whitelistEntry);
            ServerPlayerEntity serverPlayerEntity = server.getPlayerManager().getPlayer(profile.getId());
            if (serverPlayerEntity != null) {
                serverPlayerEntity.networkHandler.disconnect(Text.literal("Byee~"));
            }
        }
    }

    @Override
    public DiscordWhitelistSettings extensionSettings() {
        return (DiscordWhitelistSettings) this.getSettings();
    }

    @Override
    public void onExtensionEnabled() {
        if (!DiscordListener.discordExtensions.contains(this)) {
            DiscordListener.discordExtensions.add(this);
        }
        this.onCreateDatabase(this.conn);
        isExtensionEnabled = true;
    }

    @Override
    public void onExtensionDisabled() {
        DiscordListener.discordExtensions.remove(this);
        isExtensionEnabled = false;
    }

    @Override
    public boolean processCommands(MessageReceivedEvent event, String message, MinecraftServer server) {
        if (!this.getSettings().isEnabled()) {
            return false;
        }
        if (!discordExtension.getSettings().isEnabled()) {
            return false;
        }
        if (!DiscordUtils.isAllowed(event.getChannel().getIdLong(), this.extensionSettings().getWhitelistChats())) {
            if (message.startsWith(DiscordListener.commandPrefix + addCommand.getBody())
                    || message.startsWith(DiscordListener.commandPrefix + removeCommand.getBody())
                    || message.startsWith(DiscordListener.commandPrefix + listCommand.getBody())
                    || message.startsWith(DiscordListener.commandPrefix + infoCommand.getBody())) {
                EmbedBuilder embed = DiscordChatUtils.generateEmbed(new String[]{"**This is not the channel!!! >:(**"}, discordExtension.extensionSettings().getPrefix(), true, Color.RED, true, getDiscordExtension().extensionSettings().isShouldFeedback());
                if (embed != null) {
                    event.getMessage().delete().queueAfter(2, TimeUnit.SECONDS);
                    MessageAction embedSent = event.getChannel().sendMessageEmbeds(embed.build());
                    embedSent.queue(m -> m.delete().queueAfter(2, TimeUnit.SECONDS));
                }
                return true;
            }
        }
        if (message.startsWith(DiscordListener.commandPrefix + addCommand.getBody() + " ")) {
            addCommand.execute(event, server, discordExtension.extensionSettings().getPrefix(), this);
            return true;
        } else if (message.startsWith(DiscordListener.commandPrefix + removeCommand.getBody() + " ")) {
            removeCommand.execute(event, server, discordExtension.extensionSettings().getPrefix(), this);
            return true;
        } else if (message.equals(DiscordListener.commandPrefix + listCommand.getBody())) {
            listCommand.execute(event, server, discordExtension.extensionSettings().getPrefix(), this);
            return true;
        } else if (message.startsWith(DiscordListener.commandPrefix + infoCommand.getBody())) {
            infoCommand.execute(event, server, discordExtension.extensionSettings().getPrefix(), this);
            return true;
        }
        return false;
    }

    @Override
    public void settingsCommand(LiteralArgumentBuilder<ServerCommandSource> builder) {
        builder.  // TODO Interact with the description and add functionality maybe other than just if its enabled and the description
                then(literal("discordRoleID").
                        then(argument("discordRole", LongArgumentType.longArg()).
                                suggests((c, b) -> suggestMatching(new String[]{"0"}, b)).
                                executes(context -> {
                                    extensionSettings().setDiscordRoleID(LongArgumentType.getLong(context, "discordRole"));
                                    context.getSource().sendFeedback(() -> this.getLongSettingMessage(true, "discordRoleID", this.extensionSettings().getDiscordRole(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName()), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal("\n" + this.extensionSettings().getName() + "/discordRoleID\n").styled(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("Role that gets added to every discord user that runs the !add command successfully\n", Formatting.GRAY).styled(style -> style.withBold(false))).
                                    append(this.getLongSettingMessage(false, "discordRoleID", this.extensionSettings().getDiscordRole(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName())), false);
                            return 1;
                        })).
                then(literal("nPlayers").
                        then(argument("nPlayers", IntegerArgumentType.integer()).
                                suggests((c, b) -> suggestMatching(new String[]{"0"}, b)).
                                executes(context -> {
                                    extensionSettings().setNPlayers(IntegerArgumentType.getInteger(context, "nPlayers"));
                                    context.getSource().sendFeedback(() -> this.getLongSettingMessage(true, "nPlayers", this.extensionSettings().getNPlayers(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName()), false);
                                    this.em.saveSettings();
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal("\n" + this.extensionSettings().getName() + "/nPlayers\n").styled(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("Amount of players a discord user can add to the whitelist\n", Formatting.GRAY).styled(style -> style.withBold(false))).
                                    append(this.getLongSettingMessage(false, "nPlayers", this.extensionSettings().getNPlayers(), this.em.getSettingsBaseCommand(), this.extensionSettings().getName())), false);
                            return 1;
                        })).
                then(literal("whitelistChats").
                        then(literal("add").
                                then(argument("chatID", LongArgumentType.longArg()).
                                        suggests((c, b) -> suggestMatching(new String[]{"1234"}, b)).
                                        executes(context -> {
                                            long chat = LongArgumentType.getLong(context, "chatID");
                                            if (extensionSettings().getWhitelistChats().contains(chat)) {
                                                context.getSource().sendFeedback(() -> MarkEnum.CROSS.appendText(this.formatLongID("The chat ID ", chat, " was already on the list", true, false, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "whitelistChats")), false);
                                            } else {
                                                extensionSettings().addWhitelistChatID(chat);
                                                context.getSource().sendFeedback(() -> MarkEnum.TICK.appendText(this.formatLongID("The chat with ID ", chat, " has been", true, true, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "whitelistChats")), false);
                                                this.em.saveSettings();
                                            }
                                            return 1;
                                        }))).
                        then(literal("remove").
                                then(argument("chatID", LongArgumentType.longArg()).
                                        suggests((c, b) -> suggestMatching(this.extensionSettings().getWhitelistChats().stream().map(Object::toString), b)).
                                        executes(context -> {
                                            long chat = LongArgumentType.getLong(context, "chatID");
                                            if (extensionSettings().getWhitelistChats().contains(chat)) {
                                                this.extensionSettings().removeWhitelistChatID(chat);
                                                context.getSource().sendFeedback(() -> MarkEnum.CROSS.appendText(this.formatLongID("The chat with ID ", chat, " has been", false, true, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "whitelistChats")), false);
                                                this.em.saveSettings();
                                            } else {
                                                context.getSource().sendFeedback(() -> MarkEnum.TICK.appendText(this.formatLongID("The chat ID ", chat, " does not exist!", false, false, this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), "whitelistChats")), false);
                                            }
                                            return 1;
                                        }))).
                        then(literal("list").
                                executes(context -> {
                                    MutableText chats = Text.literal("");
                                    int chatCount = this.extensionSettings().getWhitelistChats().size();
                                    if (chatCount == 0) {
                                        chats.append(Text.literal("Not set!").styled(style -> style.
                                                withColor(Formatting.RED).
                                                withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to add!"))).
                                                withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s whitelistChats add ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName())))));
                                    } else {
                                        chats.
                                                append(Text.literal("[+]").styled(style -> style.
                                                        withColor(Formatting.GREEN).
                                                        withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to add!"))).
                                                        withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s whitelistChats add ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName()))))).
                                                append(Text.literal(" ")).
                                                append(Text.literal("[-]\n").styled(style -> style.
                                                        withColor(Formatting.RED).
                                                        withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to remove!"))).
                                                        withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s whitelistChats remove ", this.em.getSettingsBaseCommand(), this.extensionSettings().getName())))));
                                        for (int i = 0; i < chatCount; i++) {
                                            long chat = this.extensionSettings().getWhitelistChats().get(i);
                                            chats.
                                                    append(MarkEnum.DOT.appendText(Text.literal(String.format("%d", chat)).styled(style -> style.
                                                            withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(String.format("Click to copy %d", chat)))).
                                                            withBold(false).
                                                            withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.format("%d", chat)))), Formatting.GRAY)).
                                                    append(Text.literal(" ")).
                                                    append(MarkEnum.CROSS.getFormattedIdentifier().styled(style -> style.
                                                            withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(String.format("Click to delete %d", chat)))).
                                                            withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %s whitelistChats remove %d", this.em.getSettingsBaseCommand(), this.extensionSettings().getName(), chat))))).
                                                    append(i == chatCount-1 ? Text.literal("") : Text.literal("\n"));
                                        }
                                    }
                                    context.getSource().sendFeedback(() -> Text.literal("\n" + this.extensionSettings().getName() + "/whitelistChats/list" + "\n").styled(style -> style.withBold(true)).
                                            append(chats), false);
                                    return 1;
                                })).
                        executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal("\n" + this.extensionSettings().getName() + "/whitelistChats\n").styled(style -> style.withBold(true)).
                                    append(MarkEnum.INFO.appendMsg("Chats where !info, !add, !remove y !list work\n", Formatting.GRAY).styled(style -> style.withBold(false))).
                                    append(Text.literal("[Chats]").styled(style -> style.
                                            withColor(Formatting.DARK_GRAY).
                                            withUnderline(true).
                                            withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to display the already added whitelist chat IDs"))).
                                            withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %s whitelistChats list", this.em.getSettingsBaseCommand(), this.extensionSettings().getName()))))), false);
                            return 1;
                        }));
    }
}
