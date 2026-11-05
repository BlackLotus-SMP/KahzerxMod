package com.kahzerx.kahzerxmod.mixin.playerDropsSkullExtension;

import com.kahzerx.kahzerxmod.extensions.playerDropsSkullExtension.PlayerDropsSkullExtension;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;Z)V", at = @At("TAIL"))
    private void onDropFromLootTable(ServerLevel level, DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayer player)) {
            return;
        }
        if (PlayerDropsSkullExtension.isExtensionEnabled && source.is(DamageTypes.LIGHTNING_BOLT)) {
            if ((causedByPlayer && player.getRandom().nextDouble() < 0.12D) || (!causedByPlayer && player.getRandom().nextDouble() < 0.30D)) {
                ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
                stack.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved(player.getName().getString()));
                double x = player.getX();
                double y = player.getY();
                double z = player.getZ();
                // Se suelta con retraso para que el propio rayo no queme la cabeza; el drop se hace en el hilo del servidor.
                new Thread(() -> {
                    try {
                        Thread.sleep(500L);
                        level.getServer().execute(() -> Containers.dropItemStack(level, x, y, z, stack));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }
}
