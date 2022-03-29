package fuzs.gamblingstyle.proxy;

import fuzs.puzzleslib.core.EnvTypeExecutor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface Proxy {
    @SuppressWarnings("Convert2MethodRef")
    Proxy INSTANCE = EnvTypeExecutor.runForDist(() -> () -> new ClientProxy(), () -> () -> new ServerProxy());

    void destroyBlock(Level level, BlockPos pos, Player player);
}
