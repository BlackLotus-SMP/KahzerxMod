package com.kahzerx.kahzerxmod.extensions.scoreboardExtension;

import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.players.NameAndId;
import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import java.io.File;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;


// TODO There is a MASSIVE refactor to be done here...

public class ScoreboardExtension extends GenericExtension implements Extensions {
    public static boolean isExtensionEnabled = false;
    private int tickSet = -100;

    public ScoreboardExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "scoreboard", "Enables /sb command."));
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess) {
        new ScoreboardCommand().register(dispatcher, commandRegistryAccess, this);
    }

    @Override
    public void onServerRun(MinecraftServer minecraftServer) {
        isExtensionEnabled = this.getSettings().isEnabled();
    }

    @Override
    public void onTick(MinecraftServer server) {
        if (tickSet == -100) {
            return;
        }
        if (server.getTickCount() == tickSet) {
            hideSidebar(server);
            tickSet = -100;
        }
    }

    @Override
    public void onServerStarted(MinecraftServer minecraftServer) {
        hideSidebar(minecraftServer);
    }

    @Override
    public void onExtensionDisabled(CommandSourceStack source) {
        Extensions.super.onExtensionDisabled(source);
        isExtensionEnabled = false;
    }

    @Override
    public void onExtensionEnabled(CommandSourceStack source) {
        Extensions.super.onExtensionEnabled(source);
        isExtensionEnabled = true;
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    private void hideSidebar(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        if (scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) != null) {
            scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, null);
        }
    }

    public int hideSidebar(CommandSourceStack source) {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        Entity entity = source.getEntity();
        if (scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) == null) {
            source.sendSuccess(MarkEnum.CROSS.appendMessage("There is no scoreboard"), false);
            return 1;
        } else {
            scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, null);
            assert entity != null;
            source.getServer().getPlayerList().broadcastSystemMessage(MarkEnum.TICK.appendMsg(entity.getName().getString() + " removed the scoreboard."), false);
        }
        return 1;
    }

    public int startThreadedShowSideBar(CommandSourceStack source, ItemInput item, String type, boolean persistent) {
        new Thread(() -> showSideBar(source, item, type, persistent)).start();
        return 1;
    }

    public int startThreadedKilledScoreboard(CommandSourceStack source, Holder.Reference<EntityType<?>> id, String type, boolean persistent) {
        new Thread(() -> showSideBar(source, id.key().identifier(), type, persistent)).start();
        return 1;
    }

    public int startThreadedCommandScoreboard(String name, String sbName, String command, CommandSourceStack source, Identifier id, boolean persistent) {
        new Thread(() -> startCustomSB(name, sbName, command, source, id, persistent)).start();
        return 1;
    }

    public void startCustomSB(String name, String sbName, String command, CommandSourceStack source, Identifier id, boolean persistent) {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        if (scoreboard.getObjective(name) == null) {
            source.getServer().getCommands().performCommand(source.getServer().getCommands().getDispatcher().parse(command, source.getServer().createCommandSourceStack()), command);
            Objective scoreboardObjective = scoreboard.getObjective(name);
            initCustom(source, scoreboardObjective, id);
            scoreboardObjective.setDisplayName(Component.literal(sbName).withStyle(style -> style.withColor(ChatFormatting.GOLD)));
        }
        Objective scoreboardObjective = scoreboard.getObjective(name);
        source.getServer().getPlayerList().broadcastSystemMessage(display(scoreboard, scoreboardObjective, source.getServer().getTickCount(), source.getEntity(), persistent), false);
    }

    public Component display(Scoreboard scoreboard, Objective scoreboardObjective, int tick, Entity entity, boolean persistent) {
        Component text;
        if (scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) == scoreboardObjective) {
            text = MarkEnum.CROSS.appendMsg("Already showing");
        } else {
            assert entity != null;
            scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, scoreboardObjective);
            if (persistent) {
                tickSet = -100;
            } else {
                tickSet = tick + (20 * 20);
            }
            text = MarkEnum.TICK.appendText(Component.literal(ChatFormatting.WHITE + entity.getName().getString() + " has selected " + ChatFormatting.GOLD + "[" + scoreboardObjective.getDisplayName().getString() + "]"));
        }
        return text;
    }


    public void showSideBar(CommandSourceStack source, Identifier id, String type, boolean persistent) {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        Optional<EntityType<?>> optEntity = BuiltInRegistries.ENTITY_TYPE.getOptional(id);
        if (optEntity.isEmpty()) {
            source.getServer().getPlayerList().broadcastSystemMessage(MarkEnum.CROSS.appendMsg("Error on get entity!"), false);
            return;
        }
        EntityType<?> entityType = optEntity.get();
        String objectiveName = type + "." + id;
        Objective scoreboardObjective = scoreboard.getObjective(objectiveName);

        Entity entity = source.getEntity();
        Component text;
        if (scoreboardObjective != null) {
            text = display(scoreboard, scoreboardObjective, source.getServer().getTickCount(), entity, persistent);
        } else {
            String criteriaName = "minecraft." + type + ":minecraft." + entityType.toShortString();
            String capitalize = type.substring(0, 1).toUpperCase() + type.substring(1);
            String displayName = capitalize + " " + entityType.toShortString().replaceAll("_", " ");
            Optional<ObjectiveCriteria> opCriteria = ObjectiveCriteria.byName(criteriaName);
            if (opCriteria.isEmpty()) {
                return;
            }
            ObjectiveCriteria criteria = opCriteria.get();
            scoreboard.addObjective(objectiveName, criteria, Component.literal(displayName).withStyle(ChatFormatting.GOLD), criteria.getDefaultRenderType(), true, new StyledFormat(Style.EMPTY));
            Objective newScoreboardObjective = scoreboardObjective = scoreboard.getObjective(objectiveName);
            try {
                initScoreboard(source, newScoreboardObjective, entityType, type);
            } catch (Exception e) {
                scoreboard.removeObjective(newScoreboardObjective);
                text = MarkEnum.CROSS.appendMsg("Error on init scoreboard");
                assert entity != null;
                source.getServer().getPlayerList().broadcastSystemMessage(text, false);

                return;
            }
            scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, newScoreboardObjective);
            if (persistent) {
                tickSet = -100;
            } else {
                tickSet = source.getServer().getTickCount() + (20 * 20);
            }
            assert entity != null;
            assert scoreboardObjective != null;
            text = MarkEnum.TICK.appendText(Component.literal(ChatFormatting.WHITE + entity.getName().getString() + " has selected " + ChatFormatting.GOLD + "[" + scoreboardObjective.getDisplayName().getString() + "]"));
        }
        source.getServer().getPlayerList().broadcastSystemMessage(text, false);
    }

    public void showSideBar(CommandSourceStack source, ItemInput item, String type, boolean persistent) {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        Item minecraftItem = item.item().value();
        String objectiveName = type + "." + Item.getId(minecraftItem);
        Objective scoreboardObjective = scoreboard.getObjective(objectiveName);

        Entity entity = source.getEntity();
        Component text;

        if (scoreboardObjective != null) {
            text = display(scoreboard, scoreboardObjective, source.getServer().getTickCount(), entity, persistent);
        } else {
            String itemPath = BuiltInRegistries.ITEM.getKey(minecraftItem).getPath();
            String criteriaName = "minecraft." + type + ":minecraft." + itemPath;
            String capitalize = type.substring(0, 1).toUpperCase() + type.substring(1);
            String displayName = capitalize + " " + itemPath.replaceAll("_", " ");
            Optional<ObjectiveCriteria> opCriteria = ObjectiveCriteria.byName(criteriaName);
            if (opCriteria.isEmpty()) {
                return;
            }
            ObjectiveCriteria criteria = opCriteria.get();
            scoreboard.addObjective(objectiveName, criteria, Component.literal(displayName).withStyle(ChatFormatting.GOLD), criteria.getDefaultRenderType(), true, new StyledFormat(Style.EMPTY));

            Objective newScoreboardObjective = scoreboardObjective = scoreboard.getObjective(objectiveName);
            try {
                initScoreboard(source, newScoreboardObjective, minecraftItem, type);
            } catch (Exception e) {
                scoreboard.removeObjective(newScoreboardObjective);
                text = MarkEnum.CROSS.appendMsg("Error.");
                assert entity != null;
                source.getServer().getPlayerList().broadcastSystemMessage(text, false);

                return;
            }
            scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, newScoreboardObjective);
            if (persistent) {
                tickSet = -100;
            } else {
                tickSet = source.getServer().getTickCount() + (20 * 20);
            }
            assert entity != null;
            assert scoreboardObjective != null;
            text = MarkEnum.TICK.appendText(Component.literal(ChatFormatting.WHITE + entity.getName().getString() + " has selected " + ChatFormatting.GOLD + "[" + scoreboardObjective.getDisplayName().getString() + "]"));
        }
        source.getServer().getPlayerList().broadcastSystemMessage(text, false);
    }

    public void initCustom(CommandSourceStack source, Objective scoreboardObjective, Identifier id) {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        MinecraftServer server = source.getServer();
        File file = server.getWorldPath(LevelResource.PLAYER_STATS_DIR).toFile();
        File[] stats = file.listFiles();
        assert stats != null;
        for (File stat : stats) {
            String fileName = stat.getName();
            String uuidString = fileName.substring(0, fileName.lastIndexOf(".json"));
            UUID uuid = UUID.fromString(uuidString);
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            int value;
            Stat<?> finalStat = Stats.CUSTOM.get(id);
            if (player != null) {
                value = player.getStats().getValue(finalStat);
            } else {
                ServerStatsCounter serverStatHandler = new ServerStatsCounter(server, stat.toPath());
                value = serverStatHandler.getValue(finalStat);
                Optional<NameAndId> gameProfile = server.services().nameToIdCache().get(uuid);
                if (gameProfile.isEmpty()) {
                    continue;
                }
            }
            if (value == 0) {
                continue;
            }
            ScoreAccess scoreboardPlayerScore = scoreboard.getOrCreatePlayerScore(player, scoreboardObjective);
            scoreboardPlayerScore.set(value);
        }
    }

    public void initScoreboard(CommandSourceStack source, Objective scoreboardObjective, Item item, String type) {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        MinecraftServer server = source.getServer();
        File file = server.getWorldPath(LevelResource.PLAYER_STATS_DIR).toFile();
        File[] stats = file.listFiles();
        assert stats != null;
        for (File stat : stats) {
            String fileName = stat.getName();
            String uuidString = fileName.substring(0, fileName.lastIndexOf(".json"));
            UUID uuid = UUID.fromString(uuidString);
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            Stat<?> finalStat = null;
            if (type.equalsIgnoreCase("broken")) {
                finalStat = Stats.ITEM_BROKEN.get(item);
            } else if (type.equalsIgnoreCase("crafted")) {
                finalStat = Stats.ITEM_CRAFTED.get(item);
            } else if (type.equalsIgnoreCase("mined")) {
                finalStat = Stats.BLOCK_MINED.get(Block.byItem(item));
            } else if (type.equalsIgnoreCase("used")) {
                finalStat = Stats.ITEM_USED.get(item);
            } else if (type.equalsIgnoreCase("picked_up")) {
                finalStat = Stats.ITEM_PICKED_UP.get(item);
            } else if (type.equalsIgnoreCase("dropped")) {
                finalStat = Stats.ITEM_DROPPED.get(item);
            }
            int value;
            if (player != null) {
                value = player.getStats().getValue(finalStat);
            } else {
                ServerStatsCounter serverStatHandler = new ServerStatsCounter(server, stat.toPath());
                value = serverStatHandler.getValue(finalStat);
                Optional<NameAndId> gameProfile = server.services().nameToIdCache().get(uuid);

                if (gameProfile.isEmpty()) {
                    continue;
                }
            }
            if (value == 0) {
                continue;
            }
            ScoreAccess scoreboardPlayerScore = scoreboard.getOrCreatePlayerScore(player, scoreboardObjective);
            scoreboardPlayerScore.set(value);
        }
    }

    public void initScoreboard(CommandSourceStack source, Objective scoreboardObjective, EntityType<?> entityType, String type) {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        MinecraftServer server = source.getServer();
        File file = server.getWorldPath(LevelResource.PLAYER_STATS_DIR).toFile();
        File[] stats = file.listFiles();
        assert stats != null;
        for (File stat : stats) {
            String fileName = stat.getName();
            String uuidString = fileName.substring(0, fileName.lastIndexOf(".json"));
            UUID uuid = UUID.fromString(uuidString);
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            Stat<?> finalStat = null;
            if (type.equalsIgnoreCase("killed")) {
                finalStat = Stats.ENTITY_KILLED.get(entityType);
            } else if (type.equalsIgnoreCase("killed_by")) {
                finalStat = Stats.ENTITY_KILLED_BY.get(entityType);
            }
            int value;
            if (player != null) {
                value = player.getStats().getValue(finalStat);
            } else {
                ServerStatsCounter serverStatHandler = new ServerStatsCounter(server, stat.toPath());
                value = serverStatHandler.getValue(finalStat);
                Optional<NameAndId> gameProfile = server.services().nameToIdCache().get(uuid);

                if (gameProfile.isEmpty()) {
                    continue;
                }
            }
            if (value == 0) {
                continue;
            }
            ScoreAccess scoreboardPlayerScore = scoreboard.getOrCreatePlayerScore(player, scoreboardObjective);
            scoreboardPlayerScore.set(value);
        }
    }
}
