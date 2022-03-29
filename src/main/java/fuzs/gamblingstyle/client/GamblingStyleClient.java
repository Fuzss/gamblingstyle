package fuzs.gamblingstyle.client;

import fuzs.gamblingstyle.GamblingStyle;
import fuzs.gamblingstyle.client.handler.RangedItemVisualsHandler;
import fuzs.gamblingstyle.client.handler.RecipeBookExchangeHandler;
import fuzs.gamblingstyle.client.handler.RecipeBookMouseHandler;
import fuzs.gamblingstyle.client.renderer.item.RangedItemPropertyFunction;
import fuzs.gamblingstyle.registry.ModRegistry;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

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
        RangedItemVisualsHandler rangedItemVisualsHandler = new RangedItemVisualsHandler();
        MinecraftForge.EVENT_BUS.addListener(rangedItemVisualsHandler::onClickInput);
        MinecraftForge.EVENT_BUS.addListener(rangedItemVisualsHandler::onHighlightBlock);
        MinecraftForge.EVENT_BUS.addListener(rangedItemVisualsHandler::onPlaySound);
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent evt) {
        RangedItemPropertyFunction rangedItemPropertyFunction = new RangedItemPropertyFunction();
        MinecraftForge.EVENT_BUS.addListener(rangedItemPropertyFunction::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(rangedItemPropertyFunction::onLeftClickBlock);
        ItemProperties.register(ModRegistry.DRILL_ITEM.get(), new ResourceLocation("active"), rangedItemPropertyFunction);
        ItemProperties.register(ModRegistry.CHAINSAW_ITEM.get(), new ResourceLocation("active"), rangedItemPropertyFunction);
    }
}
