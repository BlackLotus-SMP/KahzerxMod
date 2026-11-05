package com.kahzerx.kahzerxmod.mixin.renewableElytraExtension;

import com.kahzerx.kahzerxmod.extensions.renewableElytraExtension.RenewableElytraExtension;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(LivingEntity.class)
public abstract class PhantomEntityMixin {
    @Inject(method = "dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;Z)V", at = @At("TAIL"))
    private void onDropFromLootTable(ServerLevel level, DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if ((Object) this instanceof Phantom phantom && RenewableElytraExtension.isExtensionEnabled && source.getEntity() instanceof Shulker && new Random().nextDouble() < 0.25D) {
            phantom.spawnAtLocation(level, new ItemStack(Items.ELYTRA));
        }
    }
}
