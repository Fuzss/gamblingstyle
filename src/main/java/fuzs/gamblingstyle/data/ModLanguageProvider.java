package fuzs.gamblingstyle.data;

import fuzs.gamblingstyle.registry.ModRegistry;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(DataGenerator gen, String modid) {
        super(gen, modid, "en_us");
    }

    @Override
    protected void addTranslations() {
        this.add(ModRegistry.DRILL_ITEM.get(), "Mechanized Drill");
        this.add(ModRegistry.CHAINSAW_ITEM.get(), "Mechanized Chainsaw");
        this.add(ModRegistry.POTENCY_ENCHANTMENT.get(), "Potency");
        this.add("enchantment.gamblingstyle.potency.desc", "Increases the effective area of ranged digger tools.");
    }
}
