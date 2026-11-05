package com.kahzerx.kahzerxmod.extensions.totopoExtension;

import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.mojang.brigadier.CommandDispatcher;
import java.awt.*;
import java.util.HashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class TotopoExtension extends GenericExtension implements Extensions {
    public TotopoExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "totopo", "Enables /totopo command."));
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        new TotopoCommand().register(dispatcher, this);
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    public void totopoShape(ServerPlayer player, ServerLevel world, double pX, double pY, double pZ) {
        int abovePlayer = 7;
        float rows = 6;
        pY += abovePlayer;
        float playerYaw = player.getYRot();
        double finalPY = pY;
        new Thread(() -> {
            for (int k = 0; k < 5; k++) {
                spawnDustParticle(world, pX, finalPY, pZ);
                spawnDustParticle(world, pX, finalPY - ((rows - 1) - (rows - 1) / 8), pZ);
                boolean n = (playerYaw < 45 && playerYaw >= -45) || (playerYaw > 135 || playerYaw <= -135);
                for (float i = 1; i < rows; i++) {
                    try {
                        Thread.sleep(50L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    double x = n ? pX + (i * 0.5) : pX;
                    double z = n ? pZ : pZ + (i * 0.5);
                    spawnDustParticle(world, x, finalPY - (i - i / 8), z);
                    spawnDustParticle(world, x, finalPY - ((rows - 1) - (rows - 1) / 8), z);

                    float j = i - 0.5F;

                    x = n ? pX + (j * 0.5) : pX;
                    z = n ? pZ : pZ + (j * 0.5);
                    spawnDustParticle(world, x, finalPY - (j - j / 8), z);

                    double x1 = n ? pX - (i * 0.5) : pX;
                    double z1 = n ? pZ : pZ - (i * 0.5);
                    spawnDustParticle(world, x1, finalPY - (i - i / 8), z1);
                    spawnDustParticle(world, x1, finalPY - ((rows - 1) - (rows - 1) / 8), z1);

                    x1 = n ? pX - (j * 0.5) : pX;
                    z1 = n ? pZ : pZ - (j * 0.5);
                    spawnDustParticle(world, x1, finalPY - (j - j / 8), z1);
                }
                try {
                    Thread.sleep(600L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void spawnDustParticle(ServerLevel world, double x, double y, double z) {
        // Se llama desde un hilo propio: el envío se hace en el hilo del servidor.
        world.getServer().execute(() -> world.sendParticles(
                new DustParticleOptions(
                        new Color(255, 220, 0).getRGB() & 0xFFFFFF, 1.0F
                ), x, y, z, 50, 0, 0, 0, 1
        ));
    }
}
