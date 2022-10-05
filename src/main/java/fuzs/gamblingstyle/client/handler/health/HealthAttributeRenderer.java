package fuzs.gamblingstyle.client.handler.health;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class HealthAttributeRenderer extends MobAttributeRenderer {

    public HealthAttributeRenderer(int iconsPerRow) {
        super(iconsPerRow);
    }

    @Override
    public int getValue(LivingEntity entity) {
        return Mth.ceil(entity.getHealth());
    }

    @Override
    public int getMaxValue(LivingEntity entity) {
        return Math.max(Mth.ceil(entity.getAttributeValue(Attributes.MAX_HEALTH)), this.getValue(entity));
    }

    @Override
    public boolean canRender(LivingEntity entity) {
        return true;
    }
}
