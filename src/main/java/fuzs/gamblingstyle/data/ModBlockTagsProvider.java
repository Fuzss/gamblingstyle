package fuzs.gamblingstyle.data;

import fuzs.gamblingstyle.registry.ModRegistry;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.nio.file.Path;

public class ModBlockTagsProvider extends TagsProvider<Block> {
    public ModBlockTagsProvider(DataGenerator p_126546_, ExistingFileHelper fileHelperIn, String modId) {
        super(p_126546_, Registry.BLOCK, modId, fileHelperIn);
    }

    @Override
    protected void addTags() {
        this.tag(ModRegistry.MINEABLE_WITH_DRILL_TAG).addTags(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.MINEABLE_WITH_SHOVEL);
        this.tag(ModRegistry.MINEABLE_WITH_SAW_TAG).addTags(BlockTags.MINEABLE_WITH_AXE, BlockTags.MINEABLE_WITH_HOE);
    }

    @Override
    protected Path getPath(ResourceLocation p_126537_) {
        return this.generator.getOutputFolder().resolve("data/" + p_126537_.getNamespace() + "/tags/blocks/" + p_126537_.getPath() + ".json");
    }

    @Override
    public String getName() {
        return "Block Tags";
    }
}
