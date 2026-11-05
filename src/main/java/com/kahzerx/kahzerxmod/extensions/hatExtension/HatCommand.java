package com.kahzerx.kahzerxmod.extensions.hatExtension;

import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import static net.minecraft.commands.Commands.literal;

public class HatCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, HatExtension hat) {
        dispatcher.register(literal("hat").
                requires(server -> hat.extensionSettings().isEnabled()).
                executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player.getMainHandItem().isEmpty()) {
                        context.getSource().sendSuccess(MarkEnum.CROSS.appendMessage("Hold something on your main hand"), false);
                        return 1;
                    }
                    int mainHandStack = player.getInventory().findSlotMatchingItem(player.getMainHandItem());
                    ItemStack mainHand = player.getInventory().getItem(mainHandStack);
                    ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
                    if (EnchantmentHelper.has(head, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
                        context.getSource().sendSuccess(MarkEnum.CROSS.appendMessage("Can't use it with binding curse on your head :("), false);
                        return 1;
                    }
                    player.setItemSlot(EquipmentSlot.HEAD, mainHand);
                    player.setItemInHand(InteractionHand.MAIN_HAND, head);
                    return 1;
                }));
    }
}
