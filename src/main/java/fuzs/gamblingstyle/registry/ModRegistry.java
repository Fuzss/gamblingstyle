package fuzs.gamblingstyle.registry;

import fuzs.gamblingstyle.GamblingStyle;
import fuzs.gamblingstyle.capability.LastHitBlockCapability;
import fuzs.gamblingstyle.capability.LastHitBlockCapabilityImpl;
import fuzs.gamblingstyle.world.item.RangedDiggerItem;
import fuzs.gamblingstyle.world.item.DrillItem;
import fuzs.gamblingstyle.world.item.SawItem;
import fuzs.gamblingstyle.world.item.enchantment.PotencyEnchantment;
import fuzs.puzzleslib.capability.CapabilityController;
import fuzs.puzzleslib.capability.data.PlayerRespawnStrategy;
import fuzs.puzzleslib.registry.RegistryManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.registries.RegistryObject;

import java.util.Locale;

public class ModRegistry {
    public static final EnchantmentCategory RANGED_DIGGER_ENCHANTMENT_CATEGORY = EnchantmentCategory.create(GamblingStyle.MOD_ID.toUpperCase(Locale.ROOT).concat("_RANGED_DIGGER"), item -> item instanceof RangedDiggerItem);

    public static final TagKey<Block> MINEABLE_WITH_DRILL_TAG = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(GamblingStyle.MOD_ID, "mineable/drill"));
    public static final TagKey<Block> MINEABLE_WITH_SAW_TAG = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(GamblingStyle.MOD_ID, "mineable/saw"));

    private static final RegistryManager REGISTRY = RegistryManager.of(GamblingStyle.MOD_ID);
    public static final RegistryObject<Item> DRILL_ITEM = REGISTRY.registerItem("drill", () -> new DrillItem(1, -2.8F, Tiers.STONE, new Item.Properties().tab(CreativeModeTab.TAB_TOOLS)));
    public static final RegistryObject<Item> CHAINSAW_ITEM = REGISTRY.registerItem("chainsaw", () -> new SawItem(1, -2.8F, Tiers.DIAMOND, new Item.Properties().tab(CreativeModeTab.TAB_TOOLS)));
    public static final RegistryObject<Enchantment> POTENCY_ENCHANTMENT = REGISTRY.registerEnchantment("potency", () -> new PotencyEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.MAINHAND));
    public static final RegistryObject<SoundEvent> SWITCH_MODE_SOUND_EVENT = REGISTRY.registerRawSoundEvent("item.rangeddiggeritem.switch_mode");

    private static final CapabilityController CAPABILITIES = CapabilityController.of(GamblingStyle.MOD_ID);
    public static final Capability<LastHitBlockCapability> LAST_HIT_BLOCK_CAPABILITY = CAPABILITIES.registerPlayerCapability("last_hit_block", LastHitBlockCapability.class, player -> new LastHitBlockCapabilityImpl(), PlayerRespawnStrategy.NEVER, new CapabilityToken<LastHitBlockCapability>() {});

    public static void touch() {

    }
}
