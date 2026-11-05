package com.kahzerx.kahzerxmod.mixin.opOnWhitelistExtension;

import com.kahzerx.kahzerxmod.extensions.opOnWhitelistExtension.OpOnWhitelistExtension;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.server.players.StoredUserList;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StoredUserList.class)
public class ServerConfigMixin<K, V extends StoredUserEntry<K>> {
    // UserWhiteList#add(UserWhiteListEntry) llama a super.add, así que pasa por aquí.
    @Inject(method = "add", at = @At("RETURN"))
    public void onWhitelistAdd(V entry, CallbackInfoReturnable<Boolean> cir) {
        if (OpOnWhitelistExtension.isExtensionEnabled && OpOnWhitelistExtension.server != null && entry instanceof UserWhiteListEntry whitelistEntry) {
            NameAndId user = whitelistEntry.getUser();
            if (user == null) {
                return;
            }
            PlayerList playerList = OpOnWhitelistExtension.server.getPlayerList();
            if (!playerList.isOp(user)) {
                playerList.op(user);
            }
        }
    }

    // Tanto UserWhiteList#remove(NameAndId) como StoredUserList#remove(StoredUserEntry) acaban en remove(K).
    @Inject(method = "remove(Ljava/lang/Object;)Z", at = @At("RETURN"))
    public void onWhitelistRemove(K user, CallbackInfoReturnable<Boolean> cir) {
        if (OpOnWhitelistExtension.isExtensionEnabled && OpOnWhitelistExtension.server != null && (Object) this instanceof UserWhiteList && user instanceof NameAndId nameAndId) {
            PlayerList playerList = OpOnWhitelistExtension.server.getPlayerList();
            if (playerList.isOp(nameAndId)) {
                playerList.deop(nameAndId);
            }
        }
    }
}
