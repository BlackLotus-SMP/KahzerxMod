package com.kahzerx.kahzerxmod.mixin.endermanNoGriefExtension;

import com.kahzerx.kahzerxmod.extensions.endermanNoGriefExtension.EndermanNoGriefExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net.minecraft.world.entity.monster.Enderman$EndermanTakeBlockGoal", "net.minecraft.world.entity.monster.Enderman$EndermanLeaveBlockGoal"})
public class EndermanEntityMixin {
    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void onEndermanAction(CallbackInfoReturnable<Boolean> cir) {
        if (EndermanNoGriefExtension.isExtensionEnabled) {
            cir.setReturnValue(false);
        }
    }
}
