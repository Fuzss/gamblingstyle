package fuzs.gamblingstyle.proxy;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ServerProxy implements Proxy {
    @Override
    public void destroyBlock(Level level, BlockPos pos, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (serverPlayer.gameMode.destroyBlock(pos)) {
            serverPlayer.connection.send(new ClientboundBlockUpdatePacket(level, pos));
        }
    }
}
