package com.kahzerx.kahzerxmod.mixin.solExtension;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityMixin {
    // ordinal 1 = daño a las entidades cercanas (ordinal 0 es la entidad a la que va pegado el cohete).
    @Redirect(method = "dealExplosionDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z", ordinal = 1))
    private boolean collide(LivingEntity instance, ServerLevel level, DamageSource source, float amount) {
        ItemStack rocketItem = ((FireworkRocketEntity) (Object) this).getItem();
        if (rocketItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBooleanOr("Sol", false)) {
            return false;
        }
        return instance.hurtServer(level, source, amount);
    }
}
