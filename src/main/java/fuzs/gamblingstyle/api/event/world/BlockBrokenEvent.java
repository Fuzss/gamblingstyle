package fuzs.gamblingstyle.api.event.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Event;

public class BlockBrokenEvent extends Event {
    private final LevelAccessor levelAccessor;
    private final BlockPos pos;
    private final Player player;
    private final Direction direction;

    public BlockBrokenEvent(LevelAccessor levelAccessor, BlockPos pos, Player player, Direction direction) {
        this.pos = pos;
        this.levelAccessor = levelAccessor;
        this.player = player;
        this.direction = direction;
    }

    public LevelAccessor getLevelAccessor()
    {
        return this.levelAccessor;
    }

    public BlockPos getPos()
    {
        return this.pos;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Direction getDirection() {
        return this.direction;
    }
}
