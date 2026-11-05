package com.kahzerx.kahzerxmod.extensions.spawnExtension;

import java.util.Set;
import com.kahzerx.kahzerxmod.utils.PermUtils;
import com.kahzerx.kahzerxmod.utils.DimUtils;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class SpawnCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, SpawnExtension spawnExtension) {
        dispatcher.register(literal("spawn").
                requires(server -> spawnExtension.extensionSettings().isEnabled()).
                executes(context -> {
                    SpawnPos pos = spawnExtension.getSpawnPos();
                    if (pos.isValidPos()) {
                        ServerPlayer player = context.getSource().getPlayer();
                        player.teleportTo(DimUtils.getWorld(pos.getDim(), context.getSource().getServer()), pos.getX(), pos.getY(), pos.getZ(), Set.of(), player.getYRot(), player.getXRot(), true);
                    } else {
                        context.getSource().sendSuccess(() -> Component.literal("Not configured yet..."), false);
                    }
                    return 1;
                }));
        dispatcher.register(literal("setSpawn").
                requires(server -> spawnExtension.extensionSettings().isEnabled() && PermUtils.hasPermission(server, 2)).
                executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    BlockPos pos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());
                    spawnExtension.updateSpawnPos(pos, context.getSource());
                    return 1;
                }).
                then(argument("coords", BlockPosArgument.blockPos()).
                        executes(context -> spawnExtension.updateSpawnPos(BlockPosArgument.getBlockPos(context, "coords"), context.getSource()))));
    }
}
