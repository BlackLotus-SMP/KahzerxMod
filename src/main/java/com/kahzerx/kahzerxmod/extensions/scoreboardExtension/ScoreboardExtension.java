package com.kahzerx.kahzerxmod.extensions.scoreboardExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.block.Block;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.level.ServerWorldProperties;

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
    public void onRegisterCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
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
        if (server.getTicks() == tickSet) {
            hideSidebar(server);
            tickSet = -100;
        }
    }

    @Override
    public void onServerStarted(MinecraftServer minecraftServer) {
        hideSidebar(minecraftServer);
    }

    @Override
    public void onExtensionDisabled(ServerCommandSource source) {
        Extensions.super.onExtensionDisabled(source);
        isExtensionEnabled = false;
    }

    @Override
    public void onExtensionEnabled(ServerCommandSource source) {
        Extensions.super.onExtensionEnabled(source);
        isExtensionEnabled = true;
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    private void hideSidebar(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        if (scoreboard.getObjectiveForSlot(1) != null) {
            scoreboard.setObjectiveSlot(1, null);
        }
    }

    public int hideSidebar(ServerCommandSource source) {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        Entity entity = source.getEntity();
        if (scoreboard.getObjectiveForSlot(1) == null) {
            source.sendFeedback(MarkEnum.CROSS.appendMessage("There is no scoreboard"), false);
            return 1;
        } else {
            scoreboard.setObjectiveSlot(1, null);
            assert entity != null;
            source.getServer().getPlayerManager().broadcast(MarkEnum.TICK.appendMsg(entity.getEntityName() + " removed the scoreboard."), false);
        }
        return 1;
    }

    public int startThreadedShowSideBar(ServerCommandSource source, ItemStackArgument item, String type, boolean persistent) {
        new Thread(() -> showSideBar(source, item, type, persistent)).start();
        return 1;
    }

    public int startThreadedKilledScoreboard(ServerCommandSource source, RegistryEntry.Reference<EntityType<?>> id, String type, boolean persistent) {
        new Thread(() -> showSideBar(source, id.registryKey().getValue(), type, persistent)).start();
        return 1;
    }

    public int startThreadedCommandScoreboard(String name, String sbName, String command, ServerCommandSource source, Identifier id, boolean persistent) {
        new Thread(() -> startCustomSB(name, sbName, command, source, id, persistent)).start();
        return 1;
    }

    public void startCustomSB(String name, String sbName, String command, ServerCommandSource source, Identifier id, boolean persistent) {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        if (scoreboard.getNullableObjective(name) == null) {
            source.getServer().getCommandManager().execute(source.getServer().getCommandManager().getDispatcher().parse(command, source.getServer().getCommandSource()), command);
            ScoreboardObjective scoreboardObjective = scoreboard.getNullableObjective(name);
            initCustom(source, scoreboardObjective, id);
            scoreboardObjective.setDisplayName(Text.literal(sbName).styled(style -> style.withColor(Formatting.GOLD)));
        }
        ScoreboardObjective scoreboardObjective = scoreboard.getNullableObjective(name);
        source.getServer().getPlayerManager().broadcast(display(scoreboard, scoreboardObjective, source.getServer().getTicks(), source.getEntity(), persistent), false);
    }

    public Text display(Scoreboard scoreboard, ScoreboardObjective scoreboardObjective, int tick, Entity entity, boolean persistent) {
        Text text;
        if (scoreboard.getObjectiveForSlot(1) == scoreboardObjective) {
            text = MarkEnum.CROSS.appendMsg("Already showing");
        } else {
            assert entity != null;
            scoreboard.setObjectiveSlot(1, scoreboardObjective);
            if (persistent) {
                tickSet = -100;
            } else {
                tickSet = tick + (20 * 20);
            }
            text = MarkEnum.TICK.appendText(Text.literal(Formatting.WHITE + entity.getEntityName() + " has selected " + Formatting.GOLD + "[" + scoreboardObjective.getDisplayName().getString() + "]"));
        }
        return text;
    }


    public void showSideBar(ServerCommandSource source, Identifier id, String type, boolean persistent) {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        Optional<EntityType<?>> optEntity = EntityType.get(id.toString());
        if (optEntity.isEmpty()) {
            source.getServer().getPlayerManager().broadcast(MarkEnum.CROSS.appendMsg("Error on get entity!"), false);
            return;
        }
        EntityType<?> entityType = optEntity.get();
        String objectiveName = type + "." + id;
        ScoreboardObjective scoreboardObjective = scoreboard.getNullableObjective(objectiveName);

        Entity entity = source.getEntity();
        Text text;
        if (scoreboardObjective != null) {
            text = display(scoreboard, scoreboardObjective, source.getServer().getTicks(), entity, persistent);
        } else {
            String criteriaName = "minecraft." + type + ":minecraft." + entityType.getUntranslatedName();
            String capitalize = type.substring(0, 1).toUpperCase() + type.substring(1);
            String displayName = capitalize + " " + entityType.getUntranslatedName().replaceAll("_", " ");
            Optional<ScoreboardCriterion> opCriteria = ScoreboardCriterion.getOrCreateStatCriterion(criteriaName);
            if (opCriteria.isEmpty()) {
                return;
            }
            ScoreboardCriterion criteria = opCriteria.get();
            scoreboard.addObjective(objectiveName, criteria, Text.literal(displayName).formatted(Formatting.GOLD), criteria.getDefaultRenderType());
            ScoreboardObjective newScoreboardObjective = scoreboardObjective = scoreboard.getNullableObjective(objectiveName);
            try {
                initScoreboard(source, newScoreboardObjective, entityType, type);
            } catch (Exception e) {
                scoreboard.removeObjective(newScoreboardObjective);
                text = MarkEnum.CROSS.appendMsg("Error on init scoreboard");
                assert entity != null;
                source.getServer().getPlayerManager().broadcast(text, false);

                return;
            }
            scoreboard.setObjectiveSlot(1, newScoreboardObjective);
            if (persistent) {
                tickSet = -100;
            } else {
                tickSet = source.getServer().getTicks() + (20 * 20);
            }
            assert entity != null;
            assert scoreboardObjective != null;
            text = MarkEnum.TICK.appendText(Text.literal(Formatting.WHITE + entity.getEntityName() + " has selected " + Formatting.GOLD + "[" + scoreboardObjective.getDisplayName().getString() + "]"));
        }
        source.getServer().getPlayerManager().broadcast(text, false);
    }

    public void showSideBar(ServerCommandSource source, ItemStackArgument item, String type, boolean persistent) {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        Item minecraftItem = item.getItem();
        String objectiveName = type + "." + Item.getRawId(minecraftItem);
        ScoreboardObjective scoreboardObjective = scoreboard.getNullableObjective(objectiveName);

        Entity entity = source.getEntity();
        Text text;

        if (scoreboardObjective != null) {
            text = display(scoreboard, scoreboardObjective, source.getServer().getTicks(), entity, persistent);
        } else {
            String criteriaName = "minecraft." + type + ":minecraft." + item.getItem().toString();
            String capitalize = type.substring(0, 1).toUpperCase() + type.substring(1);
            String displayName = capitalize + " " + minecraftItem.toString().replaceAll("_", " ");
            Optional<ScoreboardCriterion> opCriteria = ScoreboardCriterion.getOrCreateStatCriterion(criteriaName);
            if (opCriteria.isEmpty()) {
                return;
            }
            ScoreboardCriterion criteria = opCriteria.get();

            scoreboard.addObjective(objectiveName, criteria, Text.literal(displayName).formatted(Formatting.GOLD), criteria.getDefaultRenderType());

            ScoreboardObjective newScoreboardObjective = scoreboardObjective = scoreboard.getNullableObjective(objectiveName);
            try {
                initScoreboard(source, newScoreboardObjective, minecraftItem, type);
            } catch (Exception e) {
                scoreboard.removeObjective(newScoreboardObjective);
                text = MarkEnum.CROSS.appendMsg("Error.");
                assert entity != null;
                source.getServer().getPlayerManager().broadcast(text, false);

                return;
            }
            scoreboard.setObjectiveSlot(1, newScoreboardObjective);
            if (persistent) {
                tickSet = -100;
            } else {
                tickSet = source.getServer().getTicks() + (20 * 20);
            }
            assert entity != null;
            assert scoreboardObjective != null;
            text = MarkEnum.TICK.appendText(Text.literal(Formatting.WHITE + entity.getEntityName() + " has selected " + Formatting.GOLD + "[" + scoreboardObjective.getDisplayName().getString() + "]"));
        }
        source.getServer().getPlayerManager().broadcast(text, false);
    }

    public void initCustom(ServerCommandSource source, ScoreboardObjective scoreboardObjective, Identifier id) {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        MinecraftServer server = source.getServer();
        File file = new File(((ServerWorldProperties) server.getOverworld().getLevelProperties()).getLevelName(), "stats");
        File[] stats = file.listFiles();
        assert stats != null;
        for (File stat : stats) {
            String fileName = stat.getName();
            String uuidString = fileName.substring(0, fileName.lastIndexOf(".json"));
            UUID uuid = UUID.fromString(uuidString);
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
            int value;
            String playerName;
            Stat<?> finalStat = Stats.CUSTOM.getOrCreateStat(id);
            if (player != null) {
                value = player.getStatHandler().getStat(finalStat);
                playerName = player.getEntityName();
            } else {
                ServerStatHandler serverStatHandler = new ServerStatHandler(server, stat);
                value = serverStatHandler.getStat(finalStat);
                Optional<GameProfile> gameProfile = server.getUserCache().getByUuid(uuid);
                if (gameProfile.isEmpty()) {
                    continue;
                }
                playerName = gameProfile.get().getName();
            }
            if (value == 0) {
                continue;
            }
            ScoreboardPlayerScore scoreboardPlayerScore = scoreboard.getPlayerScore(playerName, scoreboardObjective);
            scoreboardPlayerScore.setScore(value);
        }
    }

    public void initScoreboard(ServerCommandSource source, ScoreboardObjective scoreboardObjective, Item item, String type) {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        MinecraftServer server = source.getServer();
        File file = new File(((ServerWorldProperties) server.getOverworld().getLevelProperties()).getLevelName(), "stats");
        File[] stats = file.listFiles();
        assert stats != null;
        for (File stat : stats) {
            String fileName = stat.getName();
            String uuidString = fileName.substring(0, fileName.lastIndexOf(".json"));
            UUID uuid = UUID.fromString(uuidString);
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
            Stat<?> finalStat = null;
            if (type.equalsIgnoreCase("broken")) {
                finalStat = Stats.BROKEN.getOrCreateStat(item);
            } else if (type.equalsIgnoreCase("crafted")) {
                finalStat = Stats.CRAFTED.getOrCreateStat(item);
            } else if (type.equalsIgnoreCase("mined")) {
                finalStat = Stats.MINED.getOrCreateStat(Block.getBlockFromItem(item));
            } else if (type.equalsIgnoreCase("used")) {
                finalStat = Stats.USED.getOrCreateStat(item);
            } else if (type.equalsIgnoreCase("picked_up")) {
                finalStat = Stats.PICKED_UP.getOrCreateStat(item);
            } else if (type.equalsIgnoreCase("dropped")) {
                finalStat = Stats.DROPPED.getOrCreateStat(item);
            }
            int value;
            String playerName;
            if (player != null) {
                value = player.getStatHandler().getStat(finalStat);
                playerName = player.getEntityName();
            } else {
                ServerStatHandler serverStatHandler = new ServerStatHandler(server, stat);
                value = serverStatHandler.getStat(finalStat);
                Optional<GameProfile> gameProfile = server.getUserCache().getByUuid(uuid);

                if (gameProfile.isEmpty()) {
                    continue;
                }
                playerName = gameProfile.get().getName();
            }
            if (value == 0) {
                continue;
            }
            ScoreboardPlayerScore scoreboardPlayerScore = scoreboard.getPlayerScore(playerName, scoreboardObjective);
            scoreboardPlayerScore.setScore(value);
        }
    }

    public void initScoreboard(ServerCommandSource source, ScoreboardObjective scoreboardObjective, EntityType<?> entityType, String type) {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        MinecraftServer server = source.getServer();
        File file = new File(((ServerWorldProperties) server.getOverworld().getLevelProperties()).getLevelName(), "stats");
        File[] stats = file.listFiles();
        assert stats != null;
        for (File stat : stats) {
            String fileName = stat.getName();
            String uuidString = fileName.substring(0, fileName.lastIndexOf(".json"));
            UUID uuid = UUID.fromString(uuidString);
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
            Stat<?> finalStat = null;
            if (type.equalsIgnoreCase("killed")) {
                finalStat = Stats.KILLED.getOrCreateStat(entityType);
            } else if (type.equalsIgnoreCase("killed_by")) {
                finalStat = Stats.KILLED_BY.getOrCreateStat(entityType);
            }
            int value;
            String playerName;
            if (player != null) {
                value = player.getStatHandler().getStat(finalStat);
                playerName = player.getEntityName();
            } else {
                ServerStatHandler serverStatHandler = new ServerStatHandler(server, stat);
                value = serverStatHandler.getStat(finalStat);
                Optional<GameProfile> gameProfile = server.getUserCache().getByUuid(uuid);

                if (gameProfile.isEmpty()) {
                    continue;
                }
                playerName = gameProfile.get().getName();
            }
            if (value == 0) {
                continue;
            }
            ScoreboardPlayerScore scoreboardPlayerScore = scoreboard.getPlayerScore(playerName, scoreboardObjective);
            scoreboardPlayerScore.setScore(value);
        }
    }
}
