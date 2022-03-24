package fuzs.gamblingstyle.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(DataGenerator gen, String modid) {
        super(gen, modid, "en_us");
    }

    @Override
    protected void addTranslations() {
//        this.add(ModRegistry.LEATHER_BAG_OF_HOLDING_ITEM.get(), "Leather Bag of Holding");
//        this.add(ModRegistry.IRON_BAG_OF_HOLDING_ITEM.get(), "Iron Bag of Holding");
//        this.add(ModRegistry.GOLDEN_BAG_OF_HOLDING_ITEM.get(), "Golden Bag of Holding");
//        this.add(ModRegistry.PRESERVATION_ENCHANTMENT.get(), "Preservation");
        this.add("item.container.tooltip.info", "Hold %s to reveal contents");
        this.add("item.container.tooltip.shift", "Shift");
        this.add("tutorial.container.itemInsert.title", "Use a %s");
        this.add("tutorial.container.bag_of_holding.name", "Bag of Holding");
        this.add("enchantment.bagofholding.preservation.desc", "Prevents a bag of holding from being lost on death. The enchantment level is reduced by one each time.");
    }
}
