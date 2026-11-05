package com.kahzerx.kahzerxmod.extensions.spoofExtension;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.core.NonNullList;
import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.HashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;

public class SpoofExtension extends GenericExtension implements Extensions {
    public SpoofExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "spoof", "Enables /spoof command that allows to see other connected players enderchest."));
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        new SpoofCommand().register(dispatcher, this);
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    public int spoofEC(CommandSourceStack source, String playerE) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayer();
        ServerPlayer player2 = source.getServer().getPlayerList().getPlayerByName(playerE);
        if (player2 != null) {
            PlayerEnderChestContainer enderChestInventory = player2.getEnderChestInventory();
            // Generar la pantalla de enderChest.
            player.openMenu(new SimpleMenuProvider((i, playerInventory, playerEntity) ->
                    ChestMenu.threeRows(i, playerInventory, enderChestInventory),
                    Component.literal(String.format("%s stop hax >:(", player.getName().getString()))));
        } else {
            source.sendSuccess(() -> Component.literal("player offline"), false);
        }
        return 1;
    }

    public int spoofInv(CommandSourceStack source, String playerE) throws CommandSyntaxException {
        int invSize = 54;
        int hotBarSize = 9;
        int hotBarStartPos = 27;
        int invStartPos = 9;
        Container inventory = new SimpleContainer(invSize);
        ServerPlayer player = source.getPlayer();
        ServerPlayer player2 = source.getServer().getPlayerList().getPlayerByName(playerE);
        assert player2 != null;

        NonNullList<ItemStack> items = player2.getInventory().getNonEquipmentItems();
        for (int i = 0; i < items.size(); i++) {
            if (i < hotBarSize) {
                inventory.setItem(i + hotBarStartPos, items.get(i));
            } else {
                inventory.setItem(i - invStartPos, items.get(i));
            }
        }

        // Mismo orden que la antigua lista Inventory#armor: pies, piernas, pecho, cabeza.
        int armorSlotStartPos = 45;
        EquipmentSlot[] armorSlots = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
        for (int j = 0; j < armorSlots.length; j++) {
            inventory.setItem(j + armorSlotStartPos, player2.getItemBySlot(armorSlots[j]));
        }

        int offHandSlotPos = 36;
        inventory.setItem(offHandSlotPos, player2.getItemBySlot(EquipmentSlot.OFFHAND));

        ContainerListener listener = new ContainerListener() {
            @Override
            public void slotChanged(AbstractContainerMenu handler, int slotId, ItemStack stack) {
                // source.getServer().getPlayerManager().saveAllPlayerData();
            }

            @Override
            public void dataChanged(AbstractContainerMenu handler, int property, int value) {

            }
        };

        player.openMenu(
                new SimpleMenuProvider(
                        (i, playerInventory, playerEntity) -> {
                            ChestMenu invCont = ChestMenu.sixRows(i, playerInventory, inventory);
                            invCont.addSlotListener(listener);
                            return invCont;
                        }, Component.literal(String.format("%s stop hax >:(", player.getName().getString()))));
        return 1;
    }
}
