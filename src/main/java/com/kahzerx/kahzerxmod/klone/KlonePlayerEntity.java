package com.kahzerx.kahzerxmod.klone;

import com.kahzerx.kahzerxmod.extensions.kloneExtension.KloneExtension;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class KlonePlayerEntity extends ServerPlayer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final LocalDateTime timeout;
    private final KloneExtension kloneExtension;

    public KlonePlayerEntity(MinecraftServer server, ServerLevel world, GameProfile profile, ClientInformation clientOptions, KloneExtension kloneExtension) {
        super(server, world, profile, clientOptions);
        this.timeout = LocalDateTime.now().plusDays(1);  // TODO this has to be customizable
        this.kloneExtension = kloneExtension;
    }

    public boolean isTimeout() {
        return LocalDateTime.now().isAfter(this.timeout);
    }

    public static KlonePlayerEntity createKlone(MinecraftServer server, ServerPlayer player, KloneExtension kloneExtension) {
        ServerLevel world = player.level();
        GameProfile profile = player.getGameProfile();

        server.getPlayerList().saveAll();
        server.getPlayerList().remove(player);
        player.connection.disconnect(Component.literal("A clone has been created.\nThe clone will leave once you rejoin.\nHappy AFK!"));

        KlonePlayerEntity klonedPlayer = new KlonePlayerEntity(server, world, profile, player.clientInformation(), kloneExtension);

        // En 26.x placeNewPlayer ya no carga los datos del jugador: se hace igual que PrepareSpawnTask#spawn.
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(klonedPlayer.problemPath(), LOGGER)) {
            Optional<ValueInput> input = server.getPlayerList()
                    .loadPlayerData(klonedPlayer.nameAndId())
                    .map(tag -> TagValueInput.create(reporter, server.registryAccess(), tag));
            input.ifPresent(klonedPlayer::load);
            klonedPlayer.snapTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
            server.getPlayerList().placeNewPlayer(new KloneNetworkManager(PacketFlow.SERVERBOUND), klonedPlayer, new CommonListenerCookie(profile, 0, klonedPlayer.clientInformation(), false));
            input.ifPresent(tag -> {
                klonedPlayer.loadAndSpawnEnderPearls(tag);
                klonedPlayer.loadAndSpawnParentVehicle(tag);
            });
        }
        klonedPlayer.teleportTo(world, player.getX(), player.getY(), player.getZ(), Set.of(), player.getYRot(), player.getXRot(), true);
        klonedPlayer.setHealth(player.getHealth());
        klonedPlayer.unsetRemoved();
        Objects.requireNonNull(klonedPlayer.getAttribute(Attributes.STEP_HEIGHT)).setBaseValue(0.6F);
        klonedPlayer.gameMode.changeGameModeForPlayer(player.gameMode.getGameModeForPlayer());

        server.getPlayerList().broadcastAll(new ClientboundRotateHeadPacket(klonedPlayer, (byte) (player.yHeadRot * 256 / 360)), klonedPlayer.level().dimension());
        server.getPlayerList().broadcastAll(ClientboundEntityPositionSyncPacket.of(klonedPlayer), klonedPlayer.level().dimension());
        server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, klonedPlayer));

        world.getChunkSource().move(klonedPlayer);

        klonedPlayer.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, (byte) 0x7f);
        klonedPlayer.getAbilities().flying = player.getAbilities().flying;
        return klonedPlayer;
    }

    private void getOut() {
        if (this.getVehicle() instanceof Player) {
            this.stopRiding();
        }
        this.getIndirectPassengers().forEach(entity -> {
            if (entity instanceof Player) {
                entity.stopRiding();
            }
        });
    }

    public void kill(Component reason) {
        this.kloneExtension.removeKlone(this);
        this.getOut();
        MinecraftServer server = this.level().getServer();
        server.schedule(new TickTask(server.getTickCount(), () -> this.connection.onDisconnect(new DisconnectionDetails(reason))));
    }

    public void kill() {
        this.kill(Component.literal("Killed"));
    }

    // /kill y similares.
    @Override
    public void kill(ServerLevel level) {
        this.kill();
    }

    @Override
    public void tick() {
        if (this.level().getServer().getTickCount() % 10 == 0) {
            this.connection.resetPosition();
            this.level().getChunkSource().move(this);
            this.hasChangedDimension();
        }
        try {
            super.tick();
            this.doTick();
        } catch (NullPointerException ignored) { }
    }

    @Override
    public void die(DamageSource source) {
        this.getOut();
        this.setHealth(20);
        this.foodData = new FoodData();
        Component text = this.getCombatTracker().getDeathMessage();
        if (this.level().getGameRules().get(GameRules.SHOW_DEATH_MESSAGES)) {
            this.connection.send(new ClientboundPlayerCombatKillPacket(this.getId(), text), PacketSendListener.exceptionallySend(() -> {
                String string = text.getString(256);
                Component text2 = Component.translatable("death.attack.message_too_long", Component.literal(string).withStyle(ChatFormatting.YELLOW));
                Component text3 = Component.translatable("death.attack.even_more_magic", this.getDisplayName()).withStyle((style) -> style.withHoverEvent(new HoverEvent.ShowText(text2)));
                return new ClientboundPlayerCombatKillPacket(this.getId(), text3);
            }));
        }
        this.kill(text);
    }

    @Override
    public String getIpAddress() {
        return "127.0.0.1";
    }
}
