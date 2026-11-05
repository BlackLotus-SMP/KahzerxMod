package com.kahzerx.kahzerxmod.extensions.scoreboardExtension;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.registries.Registries;
import net.minecraft.stats.Stats;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ScoreboardCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandRegistryAccess, ScoreboardExtension scoreboard) {
        LiteralArgumentBuilder<CommandSourceStack> command = literal("sb").requires(server -> scoreboard.extensionSettings().isEnabled());
        getSubCommands(command, commandRegistryAccess, scoreboard, false);
        LiteralArgumentBuilder<CommandSourceStack> persistentSB = literal("persistent");
        getSubCommands(persistentSB, commandRegistryAccess, scoreboard, true);
        command.then(persistentSB);
        dispatcher.register(command);
    }

    public void getSubCommands(LiteralArgumentBuilder<CommandSourceStack> command, CommandBuildContext commandRegistryAccess, ScoreboardExtension scoreboard, boolean persistent) {
        command.
                then(literal("broken").
                        then(argument("item", ItemArgument.item(commandRegistryAccess)).
                                executes(context -> scoreboard.startThreadedShowSideBar(context.getSource(), ItemArgument.getItem(context, "item"), "broken", persistent)))).
                then(literal("crafted").
                        then(argument("item", ItemArgument.item(commandRegistryAccess)).
                                executes(context -> scoreboard.startThreadedShowSideBar(context.getSource(), ItemArgument.getItem(context, "item"), "crafted", persistent)))).
                then(literal("mined").
                        then(argument("item", ItemArgument.item(commandRegistryAccess)).
                                executes(context -> scoreboard.startThreadedShowSideBar(context.getSource(), ItemArgument.getItem(context, "item"), "mined", persistent)))).
                then(literal("used").
                        then(argument("item", ItemArgument.item(commandRegistryAccess)).
                                executes(context -> scoreboard.startThreadedShowSideBar(context.getSource(), ItemArgument.getItem(context, "item"), "used", persistent)))).
                then(literal("picked_up").
                        then(argument("item", ItemArgument.item(commandRegistryAccess)).
                                executes(context -> scoreboard.startThreadedShowSideBar(context.getSource(), ItemArgument.getItem(context, "item"), "picked_up", persistent)))).
                then(literal("dropped").
                        then(argument("item", ItemArgument.item(commandRegistryAccess)).
                                executes(context -> scoreboard.startThreadedShowSideBar(context.getSource(), ItemArgument.getItem(context, "item"), "dropped", persistent)))).
                then(literal("killed").
                        then(argument("entity", ResourceArgument.resource(commandRegistryAccess, Registries.ENTITY_TYPE)).
                                suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES)).
                                executes(context -> scoreboard.startThreadedKilledScoreboard(context.getSource(), ResourceArgument.getSummonableEntityType(context, "entity"), "killed", persistent)))).
                then(literal("killed_by").
                        then(argument("entity", ResourceArgument.resource(commandRegistryAccess, Registries.ENTITY_TYPE)).
                                suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES)).
                                executes(context -> scoreboard.startThreadedKilledScoreboard(context.getSource(), ResourceArgument.getSummonableEntityType(context, "entity"), "killed_by", persistent)))).
                then(literal("deaths").
                        executes(context -> scoreboard.startThreadedCommandScoreboard("K.deaths", "deaths", "scoreboard objectives add K.deaths deathCount", context.getSource(), Stats.DEATHS, persistent))).
                then(literal("killed_mobs").
                        executes(context -> scoreboard.startThreadedCommandScoreboard("K.killed_mobs", "killed mobs", "scoreboard objectives add K.killed_mobs minecraft.custom:minecraft.mob_kills", context.getSource(), Stats.MOB_KILLS, persistent))).
                then(literal("cm_aviated").
                        executes(context -> scoreboard.startThreadedCommandScoreboard("K.cm_aviated", "cm aviated", "scoreboard objectives add K.cm_aviated minecraft.custom:minecraft.aviate_one_cm", context.getSource(), Stats.AVIATE_ONE_CM, persistent))).
                then(literal("trades").
                        executes(context -> scoreboard.startThreadedCommandScoreboard("K.villager_trades", "villager trades", "scoreboard objectives add K.villager_trades minecraft.custom:minecraft.traded_with_villager", context.getSource(), Stats.TRADED_WITH_VILLAGER, persistent))).
                then(literal("cm_pig").
                        executes(context -> scoreboard.startThreadedCommandScoreboard("K.cm_pig", "cm pig", "scoreboard objectives add K.cm_pig minecraft.custom:minecraft.pig_one_cm", context.getSource(), Stats.PIG_ONE_CM, persistent))).
                then(literal("cm_swim").
                        executes(context -> scoreboard.startThreadedCommandScoreboard("K.cm_swim", "cm swim", "scoreboard objectives add K.cm_swim minecraft.custom:minecraft.swim_one_cm", context.getSource(), Stats.SWIM_ONE_CM, persistent))).
                then(literal("jump").
                        executes(context -> scoreboard.startThreadedCommandScoreboard("K.jump", "jump", "scoreboard objectives add K.jump minecraft.custom:minecraft.jump", context.getSource(), Stats.JUMP, persistent))).
                then(literal("pot_flower").
                        executes(context -> scoreboard.startThreadedCommandScoreboard("K.pot_flower", "potted flowers", "scoreboard objectives add K.pot_flower minecraft.custom:minecraft.pot_flower", context.getSource(), Stats.POT_FLOWER, persistent))).
                then(literal("bell_ring").
                        executes(context -> scoreboard.startThreadedCommandScoreboard("K.bell_ring", "bell ring", "scoreboard objectives add K.bell_ring minecraft.custom:minecraft.bell_ring", context.getSource(), Stats.BELL_RING, persistent))).
                then(literal("remove").
                        executes(context -> scoreboard.hideSidebar(context.getSource())));
    }
}
