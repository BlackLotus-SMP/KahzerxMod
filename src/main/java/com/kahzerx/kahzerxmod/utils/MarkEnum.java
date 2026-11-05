package com.kahzerx.kahzerxmod.utils;

import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum MarkEnum {
    TICK("✔", ChatFormatting.GREEN),
    CROSS("✘", ChatFormatting.DARK_RED),
    INFO("ⓘ", ChatFormatting.GOLD),
    WARNING("⚠", ChatFormatting.GOLD),
    RIP("☠", ChatFormatting.DARK_RED),
    SUN("☀", ChatFormatting.GOLD),
    QUESTION("?", ChatFormatting.YELLOW),
    OTAKU_COIN("\uD83D\uDD25", ChatFormatting.RED),
    SLEEP("zZz", ChatFormatting.BLUE),
    DIAMOND_LIKE("✧", ChatFormatting.AQUA),
    NETHERITE_LIKE("✦", ChatFormatting.GRAY),
    DOT("•", ChatFormatting.WHITE);

    private final String identifier;
    private final ChatFormatting formatting;

    MarkEnum(String identifier, ChatFormatting formatting) {
        this.identifier = identifier;
        this.formatting = formatting;
    }

    public ChatFormatting getFormatting() {
        return formatting;
    }

    public String getIdentifier() {
        return identifier;
    }

    public MutableComponent getFormattedIdentifier() {
        return Component.literal(identifier).withStyle(style -> style.withColor(formatting).withBold(false).withClickEvent(new ClickEvent.CopyToClipboard(identifier)));
    }

    public MutableComponent getFormattedIdentifierBold() {
        return Component.literal(identifier).withStyle(style -> style.withColor(formatting).withBold(true).withClickEvent(new ClickEvent.CopyToClipboard(identifier)));
    }

    public Supplier<Component> appendMessage(String message, ChatFormatting color) {
        return () -> getFormattedIdentifier().append(Component.literal(" " + message).withStyle(style -> style.withColor(color)));
    }

    public Supplier<Component> appendMessage(String message) {
        return appendMessage(message, ChatFormatting.WHITE);
    }

    public MutableComponent appendMsg(String message, ChatFormatting color) {
        return getFormattedIdentifier().append(Component.literal(" " + message).withStyle(style -> style.withColor(color)));
    }

    public MutableComponent appendMsg(String message) {
        return appendMsg(message, ChatFormatting.WHITE);
    }

    public MutableComponent appendText(MutableComponent t, ChatFormatting color) {
        return getFormattedIdentifier().append(Component.literal(" ").append(t.withStyle(style -> style.withColor(color))));
    }

    public MutableComponent appendText(MutableComponent t) {
        return appendText(t, ChatFormatting.WHITE);
    }

    public MutableComponent boldAppendMessage(String message, ChatFormatting color) {
        return getFormattedIdentifierBold().append(Component.literal(" " + message).withStyle(style -> style.withColor(color).withBold(false)));
    }

    public MutableComponent appendSuffix(String message, ChatFormatting color) {
        return Component.literal(message + " ").withStyle(style -> style.withColor(color).withBold(false)).append(getFormattedIdentifier());
    }

    public MutableComponent appendSuffix(String message) {
        return appendSuffix(message, ChatFormatting.WHITE);
    }

    public MutableComponent boldAppendMessage(String message) {
        return boldAppendMessage(message, ChatFormatting.WHITE);
    }
}
