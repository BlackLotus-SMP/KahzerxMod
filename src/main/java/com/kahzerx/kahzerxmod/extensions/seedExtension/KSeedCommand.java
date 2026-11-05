package com.kahzerx.kahzerxmod.extensions.seedExtension;

import com.kahzerx.kahzerxmod.utils.PermUtils;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;

import static net.minecraft.commands.Commands.literal;

public class KSeedCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, SeedExtension extension) {
        dispatcher.register(literal("seed").
                requires(serverCommandSource -> PermUtils.hasPermission(serverCommandSource, 2) || extension.getSettings().isEnabled()).
                executes(context -> {
                    long l = context.getSource().getLevel().getSeed();
                    Component text = ComponentUtils.wrapInSquareBrackets((Component.literal(String.valueOf(l))).withStyle((style) -> style.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent.CopyToClipboard(String.valueOf(l))).withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.copy.click"))).withInsertion(String.valueOf(l))));
                    context.getSource().sendSuccess(() -> Component.translatable("commands.seed.success", text), false);
                    return (int)l;
                }));
    }
}
