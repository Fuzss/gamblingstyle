package fuzs.gamblingstyle.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public class LastHitBlockCapabilityImpl implements LastHitBlockCapability {
    private BlockPos lastHitBlockPos = BlockPos.ZERO;
    private Direction lastHitBlockDirection;

    @Override
    public void setLastHitBlockData(BlockPos pos, Direction direction) {
        this.lastHitBlockPos = pos;
        this.lastHitBlockDirection = direction;
    }

    @Override
    public Direction getLastHitBlockDirection() {
        return this.lastHitBlockDirection;
    }

    @Override
    public boolean isDataValid(BlockPos pos) {
        return this.lastHitBlockPos.equals(pos) && this.lastHitBlockDirection != null;
    }

    @Override
    public void write(CompoundTag tag) {
        // transient field
    }

    @Override
    public void read(CompoundTag tag) {
        // transient field
    }
}
