package com.kahzerx.kahzerxmod.mixin.villagersFollowEmeraldExtension;

import com.kahzerx.kahzerxmod.extensions.villagersFollowEmeraldExtension.VillagersFollowEmeraldExtension;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerEntityMixin extends AbstractVillager {
    @Unique
    private Player player = null;

    public VillagerEntityMixin(EntityType<? extends AbstractVillager> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    public void onTick(CallbackInfo ci) {
        if (VillagersFollowEmeraldExtension.isExtensionEnabled && !this.isNoAi()) {
            if (this.level().getGameTime() % 60 == 0) {
                player = this.level().getNearestPlayer(
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        10,
                        (player) ->
                                player instanceof Player
                                        && !player.isSpectator()
                                        && ((Player) player).isHolding(Items.EMERALD_BLOCK));
            }
            if (player != null) {
                getLookControl().setLookAt(player, getMaxHeadYRot(), getMaxHeadXRot());
                if (distanceToSqr(player) < 6.25D) {
                    getNavigation().stop();
                } else {
                    getNavigation().moveTo(player, 0.6);
                }
            }
        }
    }
}
