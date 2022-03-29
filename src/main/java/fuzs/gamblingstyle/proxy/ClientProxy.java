package fuzs.gamblingstyle.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ClientProxy extends ServerProxy {
    @Override
    public void destroyBlock(Level level, BlockPos pos, Player player) {
        if (player instanceof ServerPlayer) {
            super.destroyBlock(level, pos, player);
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.gameMode.destroyBlock(pos);
        }
    }
}
