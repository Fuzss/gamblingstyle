package fuzs.gamblingstyle.capability;

import fuzs.puzzleslib.capability.data.CapabilityComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface LastHitBlockCapability extends CapabilityComponent {
    void setLastHitBlockData(BlockPos pos, Direction direction);

    boolean isDataValid(BlockPos pos);

    Direction getAndClearData();
}
