package com.kahzerx.kahzerxmod.extensions.badgeExtension;

import net.minecraft.network.chat.TextColor;
import net.minecraft.world.scores.TeamColor;

public class BadgeInstance {
    private final String badge;
    private final int colorIndex;
    private final String description;
    public BadgeInstance(String badge, int colorIndex, String description) {
        this.badge = badge;
        this.colorIndex = colorIndex;
        this.description = description;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    // En BD se guarda el id del color (0-15), que coincide con el orden de TeamColor.
    public TextColor getTextColor() {
        if (colorIndex < 0 || colorIndex >= TeamColor.VALUES.size()) {
            return TeamColor.WHITE.textColor();
        }
        return TeamColor.VALUES.get(colorIndex).textColor();
    }

    public String getBadge() {
        return badge;
    }

    public String getDescription() {
        return description;
    }
}
