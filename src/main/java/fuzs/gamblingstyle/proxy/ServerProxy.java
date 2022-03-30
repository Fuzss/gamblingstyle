package fuzs.gamblingstyle.proxy;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ServerProxy implements Proxy {
    @Override
    public void destroyBlock(Level level, BlockPos pos, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            // TODO currently set to not deal durability damage to prevent item breaking which will prevent all blocks from being broken
            // TODO this leads to a desync issue since all blocks have been broken on the client
            ItemStack stack = serverPlayer.getMainHandItem().copy();
            serverPlayer.gameMode.destroyBlock(pos);
            serverPlayer.setItemInHand(InteractionHand.MAIN_HAND, stack);
        }
    }
}
