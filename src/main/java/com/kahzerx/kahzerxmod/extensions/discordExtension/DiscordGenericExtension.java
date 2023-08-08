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

    protected MutableText formatLongID(String prefix, long id, String suffix, boolean add, boolean applied, String baseCommand, String extensionName, String subcommand) {
        return Text.literal(prefix).styled(style -> style.withColor(Formatting.WHITE)).
                append(Text.literal(String.format("%d", id)).styled(style -> style.
                        withColor(Formatting.DARK_GREEN).
                        withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to copy ID to clipboard"))).
                        withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.format("%d", id))))).
                append(Text.literal(suffix).styled(style -> style.withColor(Formatting.WHITE))).
                append(Text.literal(" ")).
                append(applied ?
                        (add ?
                                Text.literal("added").styled(style -> style.
                                        withColor(Formatting.GREEN).
                                        withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(String.format("Click to remove %d", id)))).
                                        withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %s %s %s %d", baseCommand, extensionName, subcommand, "remove", id)))) :
                                Text.literal("removed").styled(style -> style.
                                        withColor(Formatting.RED).
                                        withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(String.format("Click to add %d", id)))).
                                        withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %s %s %s %d", baseCommand, extensionName, subcommand, "add", id))))) :
                        Text.literal(""));
    }

    protected MutableText getLongSettingMessage(boolean isNew, String subcommand, long actualID, String baseCommand, String extensionName) {
        MutableText s = this.longSetting(actualID);
        s.styled(style -> style.
                withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s %s %s", baseCommand, extensionName, subcommand, actualID != 0 ? String.format("%d", actualID) : ""))));
        return (isNew ? MarkEnum.TICK.appendMsg("New ", Formatting.WHITE) : Text.literal("")).styled(style -> style.withBold(false)).append(Text.literal(String.format("%s: ", subcommand)).styled(style -> style.withColor(Formatting.WHITE))).append(s);
    }

    private MutableText longSetting(long actualID) {
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

    protected MutableText getStringSettingMessage(boolean isNew, String subcommand, String key, String baseCommand, String extensionName) {
        MutableText s = this.stringSetting(key);
        s.styled(style -> style.
                withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s %s %s", baseCommand, extensionName, subcommand, key))));
        return (isNew ? MarkEnum.TICK.appendMsg("New ", Formatting.WHITE) : Text.literal("")).styled(style -> style.withBold(false)).append(Text.literal(String.format("%s: ", subcommand)).styled(style -> style.withColor(Formatting.WHITE))).append(s);
    }

    private MutableText stringSetting(String key) {
        MutableText sett;
        if (key.equals("")) {
            sett = Text.literal("Not set!").styled(style -> style.
                    withColor(Formatting.RED).
                    withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to add!"))));
        } else {
            sett = Text.literal(key).styled(style -> style.
                    withColor(Formatting.GRAY).
                    withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to edit!\nSet 0 to disable!"))));
        }
        return sett;
    }

    protected MutableText getAggressiveBooleanSettingMessage(boolean isNew, boolean enabled, String baseCommand, String extensionName, String subcommand) {
        MutableText s = this.booleanSetting(enabled);
        s.styled(style -> style.
                withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s %s %b", baseCommand, extensionName, subcommand, enabled))));
        return (isNew ? MarkEnum.TICK.appendMsg("Set ", Formatting.WHITE) : Text.literal("")).styled(style -> style.withBold(false)).append(Text.literal(String.format("%s: ", subcommand)).styled(style -> style.withColor(Formatting.WHITE))).append(s);
    }

    private MutableText booleanSetting(boolean enabled) {
        return Text.literal(String.format("%b", enabled)).styled(style -> style.
                withColor(enabled ? Formatting.GREEN : Formatting.RED).
                withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to modify!"))));
    }
}
