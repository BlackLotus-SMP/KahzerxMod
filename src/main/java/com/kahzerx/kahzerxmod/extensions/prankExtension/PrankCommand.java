package com.kahzerx.kahzerxmod.extensions.prankExtension;

import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class PrankCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, PrankExtension prankExtension) {
        dispatcher.register(literal("prank").
                requires(server -> prankExtension.extensionSettings().isEnabled()).
                then(argument("level", IntegerArgumentType.integer(0, 5)).
                        suggests(((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"0", "1", "2", "3", "4", "5"}, builder))).
                        executes(context -> {
                            prankExtension.updateLevel(context.getSource().getPlayer(), PrankLevel.idToLevel(IntegerArgumentType.getInteger(context, "level")));
                            context.getSource().sendSuccess(MarkEnum.TICK.appendMessage(String.format("Prank level > %d", IntegerArgumentType.getInteger(context, "level"))), false);
                            return 1;
                        })).
                then(literal("info").
                        then(argument("level", IntegerArgumentType.integer(0, 5)).
                                suggests(((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"0", "1", "2", "3", "4", "5"}, builder))).
                                executes(context -> {
                                    PrankLevel level = PrankLevel.idToLevel(IntegerArgumentType.getInteger(context, "level"));
                                    context.getSource().sendSuccess(() -> MarkEnum.INFO.appendText(Component.literal(
                                            String.format("Nivel %d", level.getID())
                                    ).withStyle(style -> style.
                                            withBold(true).
                                            withColor(level.getFormatting())
                                    ).append(Component.literal(
                                            String.format(": %s", level.getDescription())
                                    ).withStyle(style -> style.
                                            withBold(false).
                                            withColor(ChatFormatting.WHITE)))), false);
                                    return 1;
                                }))));
    }
}
