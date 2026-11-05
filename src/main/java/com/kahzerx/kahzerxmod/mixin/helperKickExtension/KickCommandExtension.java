package com.kahzerx.kahzerxmod.mixin.helperKickExtension;

import com.kahzerx.kahzerxmod.extensions.helperKickExtension.HelperKickExtension;
import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsLevels;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.KickCommand;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(KickCommand.class)
public class KickCommandExtension {
    // En 26.x /kick usa .requires(Commands.hasPermission(Commands.LEVEL_ADMINS)); se amplía para que también puedan los helpers.
    @ModifyArg(method = "register", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;requires(Ljava/util/function/Predicate;)Lcom/mojang/brigadier/builder/ArgumentBuilder;"))
    private static Predicate<CommandSourceStack> permissionLevel(Predicate<CommandSourceStack> original) {
        return source -> original.test(source) || kahzerx$isHelper(source);
    }

    @Unique
    private static boolean kahzerx$isHelper(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        return player != null &&
                HelperKickExtension.isExtensionEnabled &&
                HelperKickExtension.permsExtension != null &&
                HelperKickExtension.permsExtension.extensionSettings().isEnabled() &&
                HelperKickExtension.permsExtension.getDBPlayerPerms(player.getStringUUID()).getId() >= PermsLevels.HELPER.getId();
    }
}
