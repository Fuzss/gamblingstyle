package fuzs.gamblingstyle.world.item.enchantment;

import fuzs.gamblingstyle.registry.ModRegistry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

public class PotencyEnchantment extends Enchantment {
   public PotencyEnchantment(Rarity p_45186_, EquipmentSlot... p_45187_) {
      super(p_45186_, ModRegistry.RANGED_DIGGER_ENCHANTMENT_CATEGORY, p_45187_);
   }

   @Override
   public int getMinCost(int p_45190_) {
      return 5 + (p_45190_ - 1) * 9;
   }

   @Override
   public int getMaxCost(int p_45192_) {
      return this.getMinCost(p_45192_) + 15;
   }

   @Override
   public int getMaxLevel() {
      return 3;
   }

   @Override
   public boolean isTreasureOnly() {
      return true;
   }
}