package fuzs.gamblingstyle.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(DataGenerator gen, String modid) {
        super(gen, modid, "en_us");
    }

    @Override
    protected void addTranslations() {
        this.add("gui.recipebook.tooltip.place", "Place ingredients in grid");
        this.add("gui.recipebook.tooltip.craft", "Craft recipe directly");
    }
}
