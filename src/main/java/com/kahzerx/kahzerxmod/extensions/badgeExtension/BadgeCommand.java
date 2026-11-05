package com.kahzerx.kahzerxmod.extensions.badgeExtension;

import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsLevels;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.TeamColorArgument;
import net.minecraft.commands.arguments.MessageArgument;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class BadgeCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, BadgeExtension badge) {
        dispatcher.register(literal("badge").
                requires(serverCommandSource -> {
                    if (badge.extensionSettings().isEnabled() && badge.permsExtension.extensionSettings().isEnabled()) {
                        return badge.permsExtension.hasLevel(serverCommandSource, PermsLevels.HELPER);
                    }
                    return false;
                }).
                then(literal("create").
                        then(argument("badge", MessageArgument.message()).
                                executes(context -> {
                                    badge.insertBadge(context.getSource(), MessageArgument.getMessage(context, "badge").getString());
                                    return 1;
                                }))).
                then(literal("remove").
                        then(argument("badge", MessageArgument.message()).
                                suggests((context, builder) -> SharedSuggestionProvider.suggest(badge.getAllBadges(), builder)).
                                executes(context -> {
                                    badge.removeBadge(context.getSource(), MessageArgument.getMessage(context, "badge").getString());
                                    return 1;
                                }))).
                then(literal("id").
                        then(argument("badge", MessageArgument.message()).
                                suggests((context, builder) -> SharedSuggestionProvider.suggest(badge.getAllBadges(), builder)).
                                executes(context -> {
                                    badge.getID(context.getSource(), MessageArgument.getMessage(context, "badge").getString());
                                    return 1;
                                }))).
                then(literal("reload").
                        executes(context -> {
                            badge.reload();
                            return 1;
                        })).
                then(literal("modify").
                        then(argument("badge_id", IntegerArgumentType.integer()).
                                suggests((context, builder) -> SharedSuggestionProvider.suggest(badge.getAllIDs(), builder)).
                                then(literal("color").
                                        then(argument("color", TeamColorArgument.teamColor()).
                                                executes(context -> {
                                                    badge.modifyBadgeColor(context.getSource(), IntegerArgumentType.getInteger(context, "badge_id"), TeamColorArgument.getTeamColor(context, "color"));
                                                    return 1;
                                                }))).
                                then(literal("description").
                                        then(argument("desc", MessageArgument.message()).
                                                executes(context -> {
                                                    badge.modifyBadgeDesc(context.getSource(), IntegerArgumentType.getInteger(context, "badge_id"), MessageArgument.getMessage(context, "desc").getString());
                                                    return 1;
                                                }))))).
                then(literal("add").
                        then(argument("player", StringArgumentType.word()).
                                suggests((context, builder) -> SharedSuggestionProvider.suggest(badge.getAllPlayers(), builder)).
                                then(argument("badge", MessageArgument.message()).
                                        suggests((context, builder) -> SharedSuggestionProvider.suggest(badge.getNotPlayerBadges(StringArgumentType.getString(context, "player")), builder)).
                                        executes(context -> {
                                            badge.addBadge(context.getSource(), MessageArgument.getMessage(context, "badge").getString(), StringArgumentType.getString(context, "player"));
                                            return 1;
                                        })))).
                then(literal("delete").
                        then(argument("player", StringArgumentType.word()).
                                suggests((context, builder) -> SharedSuggestionProvider.suggest(badge.getAllPlayers(), builder)).
                                then(argument("badge", MessageArgument.message()).
                                        suggests((context, builder) -> SharedSuggestionProvider.suggest(badge.getPlayerBadges(StringArgumentType.getString(context, "player")), builder)).
                                        executes(context -> {
                                            badge.deleteBadge(context.getSource(), MessageArgument.getMessage(context, "badge").getString(), StringArgumentType.getString(context, "player"));
                                            return 1;
                                        })))));
    }
}
