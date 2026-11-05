package com.kahzerx.kahzerxmod.mixin.itemFormattedExtension;

import com.kahzerx.kahzerxmod.extensions.itemFormattedExtension.ItemFormattedExtension;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AnvilMenu.class)
public class AnvilScreenHandlerMixin {
    // result.set(CUSTOM_NAME, Component.literal(this.itemName)): único Component.literal de createResult.
    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"))
    private MutableComponent onSetName(String name) {
        if (ItemFormattedExtension.isExtensionEnabled) {
            return Component.literal(name.replace("%", "§"));
        }
        return Component.literal(name);
    }

    // ordinal 0 = inputSlots.getItem(0): se muestra el nombre con % para poder editarlo en el cliente.
    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;getItem(I)Lnet/minecraft/world/item/ItemStack;", ordinal = 0))
    private ItemStack onSet(Container instance, int i) {
        ItemStack itemStack = instance.getItem(i);
        if (ItemFormattedExtension.isExtensionEnabled && itemStack.has(DataComponents.CUSTOM_NAME)) {
            itemStack.set(DataComponents.CUSTOM_NAME, Component.literal(itemStack.getHoverName().getString().replace("§", "%")));
        }
        return itemStack;
    }

    // !this.itemName.equals(input.getHoverName().getString())
    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;getString()Ljava/lang/String;"))
    private String onCheck(Component instance) {
        if (ItemFormattedExtension.isExtensionEnabled) {
            return instance.getString().replace("%", "§");
        }
        return instance.getString();
    }
}
