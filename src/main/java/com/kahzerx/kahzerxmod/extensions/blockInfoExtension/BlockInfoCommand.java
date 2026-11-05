package com.kahzerx.kahzerxmod.extensions.blockInfoExtension;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class BlockInfoCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, BlockInfoExtension blockInfo) {
        ArgumentBuilder<CommandSourceStack, RequiredArgumentBuilder<CommandSourceStack, Coordinates>> bi = argument("coords", BlockPosArgument.blockPos()).
                executes(context -> blockInfo.db.getQuery().threadedGetInfo(
                        context.getSource(), BlockPosArgument.getBlockPos(context, "coords"), 1)).
                then(argument("int", IntegerArgumentType.integer(1)).
                        executes(context -> blockInfo.db.getQuery().threadedGetInfo(
                                context.getSource(),
                                BlockPosArgument.getBlockPos(context, "coords"),
                                IntegerArgumentType.getInteger(context, "int"))));
        dispatcher.register(literal("blockInfo").
                requires(server -> blockInfo.extensionSettings().isEnabled()).
                then(bi));
        dispatcher.register(literal("bi").
                requires(server -> blockInfo.extensionSettings().isEnabled()).
                then(bi));
    }
}
