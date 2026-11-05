package com.kahzerx.kahzerxmod.extensions.sitExtension;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

public class SitCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, SitExtension extension) {
        dispatcher.register(literal("sit").
                requires(server -> extension.extensionSettings().isEnabled()).
                executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) {
                        return 1;
                    }
                    SitEntity sitEntity = new SitEntity(player.level(), player.getX(), player.getY() - 0.16, player.getZ());
                    if (!player.onGround()) {
                        return 1;
                    }
                    sitEntity.setSitEntity(true);
                    player.level().addFreshEntity(sitEntity);
                    player.setShiftKeyDown(false);
                    player.startRiding(sitEntity);
                    player.connection.send(new ClientboundSetEntityDataPacket(player.getId(), player.getEntityData().getNonDefaultValues()));
                    return 1;
                }));
    }
}
