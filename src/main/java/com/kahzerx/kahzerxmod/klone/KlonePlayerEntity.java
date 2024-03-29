package com.kahzerx.kahzerxmod.klone;

import com.kahzerx.kahzerxmod.extensions.kloneExtension.KloneExtension;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;

import java.time.LocalDateTime;
import java.util.Objects;

public class KlonePlayerEntity extends ServerPlayerEntity {
    private final LocalDateTime timeout;
    private final KloneExtension kloneExtension;

    // TODO doesn't seem to work anymore?
    public KlonePlayerEntity(MinecraftServer server, ServerWorld world, GameProfile profile, SyncedClientOptions clientOptions, KloneExtension kloneExtension) {
        super(server, world, profile, clientOptions);
        this.timeout = LocalDateTime.now().plusDays(1);  // TODO this has to be customizable
        this.kloneExtension = kloneExtension;
    }

    public boolean isTimeout() {
        return LocalDateTime.now().isAfter(this.timeout);
    }

    public static KlonePlayerEntity createKlone(MinecraftServer server, ServerPlayerEntity player, KloneExtension kloneExtension) {
        ServerWorld world = player.getServerWorld();
        GameProfile profile = player.getGameProfile();

        server.getPlayerManager().saveAllPlayerData();
        server.getPlayerManager().remove(player);
//        server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.REMOVE_PLAYER, player));
        player.networkHandler.disconnect(Text.literal("A clone has been created.\nThe clone will leave once you rejoin.\nHappy AFK!"));

        KlonePlayerEntity klonedPlayer = new KlonePlayerEntity(server, world, profile, player.getClientOptions(), kloneExtension);
//        KlonePlayerEntity klonedPlayer = new KlonePlayerEntity(server, world, profile);

        klonedPlayer.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
        server.getPlayerManager().onPlayerConnect(new KloneNetworkManager(NetworkSide.SERVERBOUND), klonedPlayer, new ConnectedClientData(profile, 0, klonedPlayer.getClientOptions()));
        klonedPlayer.teleport(world, player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
        klonedPlayer.setHealth(player.getHealth());
        klonedPlayer.unsetRemoved();
        klonedPlayer.setStepHeight(0.6F);
        klonedPlayer.interactionManager.changeGameMode(player.interactionManager.getGameMode());

        server.getPlayerManager().sendToDimension(new EntitySetHeadYawS2CPacket(klonedPlayer, (byte) (player.headYaw * 256 / 360)), klonedPlayer.getWorld().getRegistryKey());
        server.getPlayerManager().sendToDimension(new EntityPositionS2CPacket(klonedPlayer), klonedPlayer.getWorld().getRegistryKey());
        server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, klonedPlayer));

        player.getServerWorld().getChunkManager().updatePosition(klonedPlayer);

        klonedPlayer.dataTracker.set(PLAYER_MODEL_PARTS, (byte) 0x7f);
        klonedPlayer.getAbilities().flying = player.getAbilities().flying;
        return klonedPlayer;
    }

    private void getOut() {
        if (this.getVehicle() instanceof PlayerEntity) {
            this.stopRiding();
        }
        this.getPassengersDeep().forEach(entity -> {
            if (entity instanceof PlayerEntity) {
                entity.stopRiding();
            }
        });
    }

    public void kill(Text reason) {
        this.getOut();
        this.server.send(new ServerTask(this.server.getTicks(), () -> this.networkHandler.onDisconnected(reason)));
    }

    @Override
    public void kill() {
        this.kloneExtension.removeKlone(this);
        this.kill(Text.literal("Killed"));
    }

    @Override
    public void tick() {
        if (Objects.requireNonNull(this.getServer()).getTicks() % 10 == 0) {
            this.networkHandler.syncWithPlayerPosition();
            this.getServerWorld().getChunkManager().updatePosition(this);
            this.onTeleportationDone();
        }
        try {
            super.tick();
            this.playerTick();
        } catch (NullPointerException ignored) { }
    }

    @Override
    public void onDeath(DamageSource source) {
        this.getOut();
        this.setHealth(20);
        this.hungerManager = new HungerManager();
        Text text = this.getDamageTracker().getDeathMessage();
        if (this.getWorld().getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES)) {
            this.networkHandler.send(new DeathMessageS2CPacket(this.getId(), text), PacketCallbacks.of(() -> {
                String string = text.asTruncatedString(256);
                Text text2 = Text.translatable("death.attack.message_too_long", Text.literal(string).formatted(Formatting.YELLOW));
                Text text3 = Text.translatable("death.attack.even_more_magic", this.getDisplayName()).styled((style) -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, text2)));
                return new DeathMessageS2CPacket(this.getId(), text3);
            }));
        }
        this.kill(text);
    }

    @Override
    public String getIp() {
        return "127.0.0.1";
    }
}
