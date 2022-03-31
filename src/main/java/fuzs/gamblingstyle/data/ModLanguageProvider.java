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
        this.add("item.gamblingstyle.rangeddiggeritem.mode", "Harvest Mode: %s");
        this.add("item.gamblingstyle.rangeddiggeritem.mode.single", "Single");
        this.add("item.gamblingstyle.rangeddiggeritem.mode.plane3", "3x3");
        this.add("item.gamblingstyle.rangeddiggeritem.mode.cube3", "3x3x3");
        this.add("item.gamblingstyle.rangeddiggeritem.mode.plane5", "5x5");
        this.add("item.gamblingstyle.rangeddiggeritem.mode.cube5", "5x5x5");
        this.add("subtitles.item.rangeddiggeritem.switch_mode", "Harvest mode switched");
    }
}
