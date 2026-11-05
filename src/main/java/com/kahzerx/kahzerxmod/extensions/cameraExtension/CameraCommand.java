package com.kahzerx.kahzerxmod.extensions.cameraExtension;

import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsLevels;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.literal;

public class CameraCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CameraExtension camera) {
        dispatcher.register(literal("c").
                requires(serverCommandSource -> {
                    if (camera.extensionSettings().isEnabled() && camera.permsExtension.extensionSettings().isEnabled()) {
                        return camera.permsExtension.hasLevel(serverCommandSource, PermsLevels.MOD);
                    }
                    return false;
                }).
                executes(context -> camera.setCameraMode(context.getSource())));
    }
}
