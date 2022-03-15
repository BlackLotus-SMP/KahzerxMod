package com.kahzerx.kahzerxmod.mixin.opOnWhitelistExtension;

import com.google.gson.Gson;
import com.kahzerx.kahzerxmod.extensions.opOnWhitelistExtension.OpOnWhitelistExtension;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.ServerConfigEntry;
import net.minecraft.server.ServerConfigList;
import net.minecraft.server.WhitelistEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(ServerConfigList.class)
public class ServerConfigMixin<K, V extends ServerConfigEntry<K>> {
    private static final Logger LOGGER = LogManager.getLogger();
    @Inject(method = "add", at = @At("RETURN"))
    public void onWhitelistAdd(V entry, CallbackInfo ci) {
        if (OpOnWhitelistExtension.isExtensionEnabled && OpOnWhitelistExtension.server != null && entry instanceof WhitelistEntry) {
            try {
                LOGGER.info(entry);
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(new Gson().toJson(entry));
                LOGGER.info(obj);
                LOGGER.info(obj.get("key"));
                Optional<GameProfile> profile = OpOnWhitelistExtension.server.getUserCache().getByUuid(UUID.fromString((String) ((JSONObject) obj.get("key")).get("id")));
                if (profile.isEmpty()) {
                    return;
                }
                GameProfile gameProfile = profile.get();
                if (!OpOnWhitelistExtension.server.getPlayerManager().isOperator(gameProfile)) {
                    OpOnWhitelistExtension.server.getPlayerManager().addToOperators(gameProfile);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Inject(method = "remove(Lnet/minecraft/server/ServerConfigEntry;)V", at = @At("RETURN"))
    public void onWhitelistRemove(ServerConfigEntry<K> entry, CallbackInfo ci) {
        if (OpOnWhitelistExtension.isExtensionEnabled && OpOnWhitelistExtension.server != null && entry instanceof WhitelistEntry) {
            try {
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(new Gson().toJson(entry));
                Optional<GameProfile> profile = OpOnWhitelistExtension.server.getUserCache().getByUuid(UUID.fromString((String) ((JSONObject) obj.get("key")).get("id")));
                if (profile.isEmpty()) {
                    return;
                }
                GameProfile gameProfile = profile.get();
                if (OpOnWhitelistExtension.server.getPlayerManager().isOperator(gameProfile)) {
                    OpOnWhitelistExtension.server.getPlayerManager().removeFromOperators(gameProfile);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
