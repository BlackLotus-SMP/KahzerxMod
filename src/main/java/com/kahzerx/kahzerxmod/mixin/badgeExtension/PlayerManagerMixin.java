package com.kahzerx.kahzerxmod.mixin.badgeExtension;

import com.kahzerx.kahzerxmod.extensions.badgeExtension.BadgeExtension;
import com.kahzerx.kahzerxmod.extensions.badgeExtension.BadgeInstance;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Predicate;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Redirect(method = "broadcast(Lnet/minecraft/network/message/SignedMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;sendChatMessage(Lnet/minecraft/network/message/SentMessage;ZLnet/minecraft/network/message/MessageType$Parameters;)V"))
    private void onBroadcast(ServerPlayerEntity instance, SentMessage message, boolean filterMaskEnabled, MessageType.Parameters params, SignedMessage messageFunc, Predicate<ServerPlayerEntity> shouldSendFiltered, @Nullable ServerPlayerEntity sender, MessageType.Parameters paramsFunc) {
        if (BadgeExtension.isExtensionEnabled && sender != null) {
            MutableText t = Text.literal("");
            List<BadgeInstance> badges = BadgeExtension.playerBadges.get(sender.getUuid().toString());
            if (badges.size() != 0) {
                t = Text.literal(" ");
                MutableText hover = Text.literal("");
                hover.append(Text.literal("Badges:").styled(style -> style.withColor(Formatting.WHITE)));
                for (BadgeInstance badgeInstance : badges) {
                    hover.append(Text.literal("\n" + badgeInstance.getBadge()).styled(style -> style.withColor(Formatting.byColorIndex(badgeInstance.getColorIndex()))));
                    hover.append(Text.literal(String.format(": %s", badgeInstance.getDescription())).styled(style -> style.withColor(Formatting.WHITE)));
                }
                MutableText bgs = Text.literal("");
                for (int i = badges.size() - 1; i >= 0; i--) {
                    BadgeInstance badge = badges.get(i);
                    bgs.append(Text.literal(badge.getBadge()).styled(style -> style.withColor(Formatting.byColorIndex(badge.getColorIndex()))));
                    bgs.append(Text.literal("").styled(style -> style.withColor(Formatting.WHITE)));
                    if (i < badges.size() - 2 || i == 0) {
                        break;
                    }
                    bgs.append(Text.literal(" "));
                }
                t.append(bgs.styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))));
            }
            MutableText display = MutableText.of(sender.getDisplayName().getContent());
            display.setStyle(sender.getDisplayName().getStyle());
            MutableText m = Text.literal("");
            if (sender.getDisplayName().getSiblings().size() != 0) {
                display.append(sender.getDisplayName().getSiblings().get(0));
                display.append(sender.getDisplayName().getSiblings().get(1));
                m.append(sender.getDisplayName().getSiblings().get(2).copy());
            }
            m.append(t);
            display.append(m);
            MessageType.Parameters par = MessageType.params(MessageType.CHAT, sender.world.getRegistryManager(), display);
            instance.sendChatMessage(message, filterMaskEnabled, par);
        }
        else {
            instance.sendChatMessage(message, filterMaskEnabled, params);
        }
    }
}
