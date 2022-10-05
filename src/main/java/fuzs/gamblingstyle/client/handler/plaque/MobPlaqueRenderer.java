package fuzs.gamblingstyle.client.handler.plaque;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;

public abstract class MobPlaqueRenderer {

    public abstract void drawIcon(PoseStack poseStack, int posX, int posY, LivingEntity entity);
}
