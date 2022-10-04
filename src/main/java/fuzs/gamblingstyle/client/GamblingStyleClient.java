package fuzs.gamblingstyle.client;

import fuzs.gamblingstyle.GamblingStyle;
import fuzs.gamblingstyle.client.handler.*;
import fuzs.gamblingstyle.client.renderer.item.RangedItemPropertyFunction;
import fuzs.gamblingstyle.registry.ModRegistry;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

@Mod.EventBusSubscriber(modid = GamblingStyle.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class GamblingStyleClient {
    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        registerHandlers();
    }

    private static void registerHandlers() {
        RecipeBookExchangeHandler recipeBookExchangeHandler = new RecipeBookExchangeHandler();
        MinecraftForge.EVENT_BUS.addListener(recipeBookExchangeHandler::onScreenOpen);
        RecipeBookMouseHandler recipeBookMouseHandler = new RecipeBookMouseHandler();
        MinecraftForge.EVENT_BUS.addListener(recipeBookMouseHandler::onMouseReleased);
        MinecraftForge.EVENT_BUS.addListener(recipeBookMouseHandler::onMouseScroll);
        MinecraftForge.EVENT_BUS.addListener(recipeBookMouseHandler::onMouseDrag);
        HighlightBlocksHandler highlightBlocksHandler = new HighlightBlocksHandler();
        MinecraftForge.EVENT_BUS.addListener(highlightBlocksHandler::onHighlightBlock);
        BlockHarvestingHandler blockHarvestingHandler = new BlockHarvestingHandler();
        MinecraftForge.EVENT_BUS.addListener(blockHarvestingHandler::onLeftClickBlock);
        MinecraftForge.EVENT_BUS.addListener(blockHarvestingHandler::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(blockHarvestingHandler::onClickInput);
        MinecraftForge.EVENT_BUS.addListener(blockHarvestingHandler::onPlaySound);
        CreativeInventoryScreenHandler creativeInventoryScreenHandler = new CreativeInventoryScreenHandler();
        MinecraftForge.EVENT_BUS.addListener(creativeInventoryScreenHandler::onScreenOpen);
        MinecraftForge.EVENT_BUS.addListener(PetHealthRenderer.INSTANCE::onRenderNameplate);
        MinecraftForge.EVENT_BUS.addListener((final TickEvent.ClientTickEvent evt) -> {
            if (evt.phase == TickEvent.Phase.END) PetHealthRenderer.INSTANCE.onClientTick$End();
        });
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent evt) {
        RangedItemPropertyFunction rangedItemPropertyFunction = new RangedItemPropertyFunction();
        MinecraftForge.EVENT_BUS.addListener(rangedItemPropertyFunction::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(rangedItemPropertyFunction::onLeftClickBlock);
        ItemProperties.register(ModRegistry.DRILL_ITEM.get(), new ResourceLocation("active"), rangedItemPropertyFunction);
        ItemProperties.register(ModRegistry.CHAINSAW_ITEM.get(), new ResourceLocation("active"), rangedItemPropertyFunction);
    }


    @SubscribeEvent
    public static void onLoadComplete(final FMLLoadCompleteEvent evt) {
        CreativeSearchTreeManager.INSTANCE.load();
    }
}
