package com.kahzerx.kahzerxmod.extensions.randomTPExtension;

import net.minecraft.world.level.storage.LevelData;
import com.kahzerx.kahzerxmod.utils.PlayerUtils;
import com.kahzerx.kahzerxmod.ExtensionManager;
import com.kahzerx.kahzerxmod.Extensions;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;
import com.kahzerx.kahzerxmod.extensions.GenericExtension;
import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsExtension;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.mojang.brigadier.CommandDispatcher;
import java.util.HashMap;
import java.util.Random;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

public class RandomTPExtension extends GenericExtension implements Extensions {
    private PermsExtension permsExtension;

    public RandomTPExtension(HashMap<String, String> fileSettings) {
        super(new ExtensionSettings(fileSettings, "randomTP", "randomTP in a 10k block radius."));
    }

    @Override
    public void onExtensionsReady(ExtensionManager em) {
        this.permsExtension = (PermsExtension) em.getExtensions().get("perms");
    }

    @Override
    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        new RandomTPCommand().register(dispatcher, this);
    }

    public PermsExtension getPermsExtension() {
        return this.permsExtension;
    }

    @Override
    public ExtensionSettings extensionSettings() {
        return this.getSettings();
    }

    public int tpAndSpawnPoint(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            return 1;
        }
        if (player.level().dimension() != Level.OVERWORLD) {
            source.sendSuccess(MarkEnum.CROSS.appendMessage("You can only run this command in the overworld"), false);
            return 1;
        }
        final int min = -10000;
        final int max = 10000;

        Random rand = new Random();
        double x = min + (max + max) * rand.nextDouble();
        double z = min + (max + max) * rand.nextDouble();
        final double y = 255;

        PlayerUtils.teleportToWithTicket(player, x, y, z);
        BlockPos pos1 = source.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos((int) x, (int) y, (int) z));
        BlockPos posBelow = new BlockPos(pos1.getX(), pos1.getY() - 1, pos1.getZ());
        if (source.getLevel().getBlockState(posBelow).getBlock().equals(Blocks.WATER) || source.getLevel().getBlockState(posBelow).getBlock().equals(Blocks.LAVA)) {
            tpAndSpawnPoint(source);
        } else {
            PlayerUtils.teleportToWithTicket(player, x, pos1.getY(), z);
            player.setRespawnPosition(
                    new ServerPlayer.RespawnConfig(LevelData.RespawnData.of(player.level().dimension(), player.blockPosition(), 0.0F, 0.0F), true),
                    false
            );
        }
        return 1;
    }
}
