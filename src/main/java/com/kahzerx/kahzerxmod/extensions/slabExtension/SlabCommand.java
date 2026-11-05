package com.kahzerx.kahzerxmod.extensions.slabExtension;

import net.minecraft.core.registries.Registries;
import com.kahzerx.kahzerxmod.extensions.slabExtension.utils.SlabUtils;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantments;

import static net.minecraft.commands.Commands.literal;

public class SlabCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, SlabExtension slab) {
        dispatcher.register(literal("slab").
                requires(server -> slab.extensionSettings().isEnabled()).
                executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (!SlabUtils.isSlab(player.getMainHandItem().getItem())) {
                        context.getSource().sendSuccess(MarkEnum.CROSS.appendMessage("You need a slab on your main hand"), false);
                        return 1;
                    }
                    if (player.getMainHandItem().isEnchanted()) {
                        context.getSource().sendSuccess(MarkEnum.CROSS.appendMessage("Slab is already enchanted"), false);
                        return 1;
                    }
                    player.getMainHandItem().enchant(context.getSource().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.POWER), 1);
                    context.getSource().sendSuccess(MarkEnum.TICK.appendMessage("Slab ready!"), false);
                    return 1;
                }));
    }
}
