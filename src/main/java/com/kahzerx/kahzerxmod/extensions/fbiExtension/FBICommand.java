package com.kahzerx.kahzerxmod.extensions.fbiExtension;

import com.kahzerx.kahzerxmod.utils.PermUtils;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import java.util.List;

import static com.kahzerx.kahzerxmod.extensions.fbiExtension.FBIExtension.getHiddenPlayers;
import static net.minecraft.commands.Commands.literal;

public class FBICommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, FBIExtension fbi) {
        dispatcher.register(literal("fbi").
                requires(player -> fbi.extensionSettings().isEnabled() && PermUtils.hasPermission(player, 2)).
                executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) {
                        return 1;
                    }
                    MinecraftServer server = player.level().getServer();
                    if (server == null) {
                        return 1;
                    }
                    if (getHiddenPlayers().contains(player)) {
                        getHiddenPlayers().remove(player);
                        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                            if (p.equals(player)) {
                                continue;
                            }
                            p.connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, player));
                        }
                        server.getPlayerList().broadcastSystemMessage(Component.translatable("multiplayer.player.joined", new Object[]{player.getDisplayName()}).withStyle(ChatFormatting.YELLOW), false);
                    } else {
                        getHiddenPlayers().add(player);
                        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                            if (p.equals(player)) {
                                continue;
                            }
                            p.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(player.getGameProfile().id())));
                            p.connection.send(new ClientboundRemoveEntitiesPacket(player.getId()));
                        }
                        player.setGameMode(GameType.SPECTATOR);
//                        server.getPlayerManager().broadcast(Text.translatable("multiplayer.player.joined", new Object[]{player.getDisplayName()}).formatted(Formatting.YELLOW), MessageType.SYSTEM);
                        server.getPlayerList().broadcastSystemMessage(Component.translatable("multiplayer.player.left", player.getDisplayName()).withStyle(ChatFormatting.YELLOW), false);
                    }
                    return 1;
                }));
    }
}
