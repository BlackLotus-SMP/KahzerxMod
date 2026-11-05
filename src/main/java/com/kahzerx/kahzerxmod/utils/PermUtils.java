package com.kahzerx.kahzerxmod.utils;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

public class PermUtils {
    // Equivalente al antiguo CommandSourceStack#hasPermission(int) (niveles de op 0-4).
    public static boolean hasPermission(CommandSourceStack source, int level) {
        return source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(level)));
    }
}
