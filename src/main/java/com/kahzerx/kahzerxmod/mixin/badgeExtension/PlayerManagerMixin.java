package com.kahzerx.kahzerxmod.mixin.badgeExtension;

import com.kahzerx.kahzerxmod.extensions.badgeExtension.BadgeExtension;
import com.kahzerx.kahzerxmod.extensions.badgeExtension.BadgeInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {
    @Redirect(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;sendChatMessage(Lnet/minecraft/network/chat/OutgoingChatMessage;ZLnet/minecraft/network/chat/ChatType$Bound;)V"))
    private void onBroadcast(ServerPlayer instance, OutgoingChatMessage message, boolean filterMaskEnabled, ChatType.Bound params, PlayerChatMessage messageFunc, Predicate<ServerPlayer> shouldSendFiltered, @Nullable ServerPlayer sender, ChatType.Bound paramsFunc) {
        if (BadgeExtension.isExtensionEnabled && sender != null) {
            MutableComponent t = Component.literal("");
            List<BadgeInstance> badges = BadgeExtension.playerBadges.get(sender.getUUID().toString());
            if (!badges.isEmpty()) {
                t = Component.literal(" ");
                MutableComponent hover = Component.literal("");
                hover.append(Component.literal("Badges:").withStyle(style -> style.withColor(ChatFormatting.WHITE)));
                for (BadgeInstance badgeInstance : badges) {
                    hover.append(Component.literal("\n" + badgeInstance.getBadge()).withStyle(style -> style.withColor(badgeInstance.getTextColor())));
                    hover.append(Component.literal(String.format(": %s", badgeInstance.getDescription())).withStyle(style -> style.withColor(ChatFormatting.WHITE)));
                }
                MutableComponent bgs = Component.literal("");
                for (int i = badges.size() - 1; i >= 0; i--) {
                    BadgeInstance badge = badges.get(i);
                    bgs.append(Component.literal(badge.getBadge()).withStyle(style -> style.withColor(badge.getTextColor())));
                    bgs.append(Component.literal("").withStyle(style -> style.withColor(ChatFormatting.WHITE)));
                    if (i < badges.size() - 2 || i == 0) {
                        break;
                    }
                    bgs.append(Component.literal(" "));
                }
                t.append(bgs.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(hover))));
            }
            MutableComponent display = MutableComponent.create(sender.getDisplayName().getContents());
            display.setStyle(sender.getDisplayName().getStyle());
            MutableComponent m = Component.literal("");
            if (!sender.getDisplayName().getSiblings().isEmpty()) {
                display.append(sender.getDisplayName().getSiblings().get(0));
                display.append(sender.getDisplayName().getSiblings().get(1));
                m.append(sender.getDisplayName().getSiblings().get(2).copy());
            }
            m.append(t);
            display.append(m);
            ChatType.Bound par = ChatType.bind(ChatType.CHAT, sender.level().registryAccess(), display);
            instance.sendChatMessage(message, filterMaskEnabled, par);
        }
        else {
            instance.sendChatMessage(message, filterMaskEnabled, params);
        }
    }
}
