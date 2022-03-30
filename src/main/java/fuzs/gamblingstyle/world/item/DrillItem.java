package fuzs.gamblingstyle.world.item;

import com.google.common.collect.ImmutableSet;
import fuzs.gamblingstyle.registry.ModRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import java.util.Set;

public class DrillItem extends RangedDiggerItem {
    public static final Set<ToolAction> DEFAULT_DRILL_ACTIONS = ImmutableSet.of(ToolActions.PICKAXE_DIG, ToolActions.SHOVEL_DIG);

    public DrillItem(float baseAttackDamage, float attackSpeed, Tier tier, Properties properties) {
        super(baseAttackDamage, attackSpeed, tier, ModRegistry.MINEABLE_WITH_DRILL_TAG, properties);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
        return DEFAULT_DRILL_ACTIONS.contains(toolAction);
    }
}
