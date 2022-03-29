package fuzs.gamblingstyle;

import fuzs.gamblingstyle.config.ClientConfig;
import fuzs.gamblingstyle.data.ModBlockTagsProvider;
import fuzs.gamblingstyle.data.ModLanguageProvider;
import fuzs.gamblingstyle.handler.HitBlockFaceHandler;
import fuzs.gamblingstyle.registry.ModRegistry;
import fuzs.puzzleslib.config.AbstractConfig;
import fuzs.puzzleslib.config.ConfigHolder;
import fuzs.puzzleslib.config.ConfigHolderImpl;
import fuzs.puzzleslib.network.NetworkHandler;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(GamblingStyle.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class GamblingStyle {
    public static final String MOD_ID = "gamblingstyle";
    public static final String MOD_NAME = "Gambling Style";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final NetworkHandler NETWORK = NetworkHandler.of(MOD_ID);
    @SuppressWarnings("Convert2MethodRef")
    public static final ConfigHolder<ClientConfig, AbstractConfig> CONFIG = ConfigHolder.client(() -> new ClientConfig());

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ((ConfigHolderImpl<?, ?>) CONFIG).addConfigs(MOD_ID);
        ModRegistry.touch();
        registerHandlers();
    }

    private static void registerHandlers() {
        HitBlockFaceHandler hitBlockFaceHandler = new HitBlockFaceHandler();
        MinecraftForge.EVENT_BUS.addListener(hitBlockFaceHandler::onLeftClickBlock);
    }

    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent evt) {
        DataGenerator generator = evt.getGenerator();
        final ExistingFileHelper existingFileHelper = evt.getExistingFileHelper();
        generator.addProvider(new ModBlockTagsProvider(generator, existingFileHelper, MOD_ID));
        generator.addProvider(new ModLanguageProvider(generator, MOD_ID));
    }
}
