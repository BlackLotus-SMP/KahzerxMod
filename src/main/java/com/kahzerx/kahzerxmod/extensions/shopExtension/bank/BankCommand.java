package com.kahzerx.kahzerxmod.extensions.shopExtension.bank;

import com.kahzerx.kahzerxmod.extensions.shopExtension.ShopExtension;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static net.minecraft.command.CommandSource.suggestMatching;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BankCommand {
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, ShopExtension extension) {
        dispatcher.register(literal("bank").
                requires(server -> extension.extensionSettings().isEnabled()).
                then(literal("balance").
                        executes(context -> {
                            context.getSource().sendFeedback(new LiteralText("Balance: ").append(MarkEnum.OTAKU_COIN.appendMessage(String.valueOf(extension.getBalance(context.getSource().getPlayer())))), false);
                            return 1;
                        })).
                then(literal("transfer").
                        then(argument("player", StringArgumentType.string()).
                                suggests((c, b) -> suggestMatching(extension.getPlayers(), b)).
                                then(argument("amount", IntegerArgumentType.integer(1)).
                                        executes(context -> {
                                            String playerName = StringArgumentType.getString(context, "player");
                                            String playerUUID = extension.getPlayerUUID(playerName);
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            if (playerUUID == null) {
                                                context.getSource().sendFeedback(MarkEnum.CROSS.appendMessage("Este jugador no existe!"), false);
                                                return 1;
                                            }
                                            if (extension.getBalance(context.getSource().getPlayer()) < amount) {
                                                context.getSource().sendFeedback(MarkEnum.CROSS.appendMessage("No tienes balance suficiente!"), false);
                                                return 1;
                                            }
                                            extension.updateFounds(playerUUID, amount);
                                            extension.updateFounds(context.getSource().getPlayer(), amount * -1);
                                            context.getSource().sendFeedback(MarkEnum.TICK.appendMessage("Transferencia de ").append(MarkEnum.OTAKU_COIN.appendMessage(String.format("%d a %s completada!", amount, playerName))), false);
                                            return 1;
                                        })))));
    }
}