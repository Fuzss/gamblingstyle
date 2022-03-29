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
        this.add(ModRegistry.SAW_ITEM.get(), "Mechanized Chainsaw");
    }
}
