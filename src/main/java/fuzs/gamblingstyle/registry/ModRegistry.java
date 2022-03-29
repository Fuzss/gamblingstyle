package fuzs.gamblingstyle.registry;

import fuzs.gamblingstyle.GamblingStyle;
import fuzs.gamblingstyle.capability.LastHitBlockCapability;
import fuzs.gamblingstyle.capability.LastHitBlockCapabilityImpl;
import fuzs.gamblingstyle.world.item.DrillItem;
import fuzs.puzzleslib.capability.CapabilityController;
import fuzs.puzzleslib.capability.data.PlayerRespawnStrategy;
import fuzs.puzzleslib.registry.RegistryManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.registries.RegistryObject;

public class ModRegistry {
    public static final TagKey<Block> MINEABLE_WITH_DRILL_TAG = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(GamblingStyle.MOD_ID, "mineable/drill"));

    private static final RegistryManager REGISTRY = RegistryManager.of(GamblingStyle.MOD_ID);
    public static final RegistryObject<Item> DRILL_ITEM = REGISTRY.registerItem("drill", () -> new DrillItem(Tiers.DIAMOND, 1, -2.8F, (new Item.Properties()).tab(CreativeModeTab.TAB_TOOLS)));

    private static final CapabilityController CAPABILITIES = CapabilityController.of(GamblingStyle.MOD_ID);
    public static final Capability<LastHitBlockCapability> LAST_HIT_BLOCK_CAPABILITY = CAPABILITIES.registerPlayerCapability("last_hit_block", LastHitBlockCapability.class, player -> new LastHitBlockCapabilityImpl(), PlayerRespawnStrategy.NEVER, new CapabilityToken<LastHitBlockCapability>() {});

    public static void touch() {

    }
}
