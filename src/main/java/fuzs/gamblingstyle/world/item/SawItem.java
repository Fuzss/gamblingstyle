package fuzs.gamblingstyle.world.item;

import com.google.common.collect.ImmutableSet;
import fuzs.gamblingstyle.registry.ModRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import java.util.Set;

public class SawItem extends RangedDiggerItem {
    public static final Set<ToolAction> DEFAULT_SAW_ACTIONS = ImmutableSet.of(ToolActions.AXE_DIG, ToolActions.HOE_DIG);

    public SawItem(float baseAttackDamage, float attackSpeed, Tier tier, Properties properties) {
        super(baseAttackDamage, attackSpeed, tier, ModRegistry.MINEABLE_WITH_SAW_TAG, properties);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return DEFAULT_SAW_ACTIONS.contains(toolAction);
    }
}
