package com.kahzerx.kahzerxmod.klone;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.jspecify.annotations.Nullable;

// Conexión sin canal: todo lo que tocaría el canal de netty se anula.
public class KloneNetworkManager extends Connection {
    public KloneNetworkManager(PacketFlow side) {
        super(side);
    }

    @Override
    public <T extends PacketListener> void setupInboundProtocol(ProtocolInfo<T> protocol, T packetListener) {
    }

    @Override
    public void setListenerForServerboundHandshake(PacketListener packetListener) {
    }

    @Override
    public void send(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush) {
    }

    @Override
    public void flushChannel() {
    }

    @Override
    public void setReadOnly() {
    }

    @Override
    public void handleDisconnection() {
    }
}
