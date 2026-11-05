package com.kahzerx.kahzerxmod.extensions.skullExtension;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.server.players.NameAndId;
import com.kahzerx.kahzerxmod.utils.MarkEnum;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static net.minecraft.commands.Commands.literal;

public class SkullCommand {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, SkullExtension skull) {
        dispatcher.register(literal("skull").
                requires(isEnabled -> skull.extensionSettings().isEnabled()).
                then(Commands.argument("playerName", StringArgumentType.word()).
                        executes(context -> {
                            Optional<NameAndId> profile = context.getSource().getServer().services().nameToIdCache().get(StringArgumentType.getString(context, "playerName"));
                            if (profile.isEmpty()) {
                                context.getSource().sendSuccess(MarkEnum.CROSS.appendMessage("Not premium!"), false);
                                return 1;
                            }
                            ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
                            stack.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved(profile.get().name()));
                            ServerPlayer sourcePlayer = context.getSource().getPlayer();
                            if (!sourcePlayer.addItem(stack)) {
                                Containers.dropItemStack(sourcePlayer.level(), sourcePlayer.getX(), sourcePlayer.getY(), sourcePlayer.getZ(), stack);
                            }
                            return 1;
                        })));
    }
}
