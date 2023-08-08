package com.kahzerx.kahzerxmod.extensions.discordExtension;

import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public abstract class DiscordGenericExtension extends GenericExtension {
    public DiscordGenericExtension(ExtensionSettings settings) {
        super(settings);
    }

    protected MutableText getLongSettingMessage(boolean isNew, String subcommand, long actualID, String baseCommand, String extensionName) {
        MutableText s = this.longSetting(actualID);
        s.styled(style -> style.
                withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s %s %s", baseCommand, extensionName, subcommand, actualID != 0 ? String.format("%d", actualID) : ""))));
        return (isNew ? MarkEnum.TICK.appendMsg("New ", Formatting.WHITE) : Text.literal("")).styled(style -> style.withBold(false)).append(Text.literal(String.format("%s: ", subcommand)).styled(style -> style.withColor(Formatting.WHITE))).append(s);
    }

    protected MutableText longSetting(long actualID) {
        MutableText sett;
        if (actualID != 0) {
            sett = Text.literal(String.format("%d", actualID)).styled(style -> style.
                    withColor(Formatting.GREEN).
                    withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to edit!\nSet 0 to disable!"))));
        } else {
            sett = Text.literal("Not set!").styled(style -> style.
                    withColor(Formatting.RED).
                    withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to add!"))));
        }
        return sett;
    }
}
