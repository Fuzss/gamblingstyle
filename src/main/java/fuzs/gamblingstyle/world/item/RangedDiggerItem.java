package fuzs.gamblingstyle.world.item;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import fuzs.gamblingstyle.GamblingStyle;
import fuzs.gamblingstyle.capability.LastHitBlockCapability;
import fuzs.gamblingstyle.proxy.Proxy;
import fuzs.gamblingstyle.registry.ModRegistry;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class RangedDiggerItem extends DiggerItem {
    public static final int FALLING_HARVEST_LIMIT = 12;
    static final String TAG_HARVEST_MODE = "HarvestMode";

    public RangedDiggerItem(float baseAttackDamage, float attackSpeed, Tier tier, TagKey<Block> blocks, Properties properties) {
        super(baseAttackDamage, attackSpeed, tier, blocks, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) return InteractionResultHolder.pass(itemstack);
        level.playLocalSound(player.getX(), player.getY(), player.getZ(), ModRegistry.SWITCH_MODE_SOUND_EVENT.get(), SoundSource.PLAYERS, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F), false);
        if (!level.isClientSide) {
            RangedDiggerItemMode diggerItemMode = this.getHarvestMode(itemstack).getNextItemMode(itemstack);
            this.setHarvestMode(itemstack, diggerItemMode);
            player.displayClientMessage(diggerItemMode.makeComponent(false), false);
        }
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level p_41422_, List<Component> list, TooltipFlag p_41424_) {
        super.appendHoverText(stack, p_41422_, list, p_41424_);
        list.add(this.getHarvestMode(stack).makeComponent(true));
    }

    @Override
    public abstract boolean canPerformAction(ItemStack stack, ToolAction toolAction);

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.isSame(oldStack, newStack);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (!player.level.isClientSide) {
            Stream<BlockPos> blocksToHarvest = this.getAllHarvestBlocks(stack, pos, player, false);
            Stream<BlockPos> blocksToUpdate = this.getAllHarvestBlocks(stack, pos, player, null, false, false);
            blocksToHarvest.forEach(pos1 -> Proxy.INSTANCE.destroyBlock(player.level, pos1, player));
            if (player instanceof ServerPlayer serverPlayer) {
                blocksToUpdate.forEach(pos1 -> serverPlayer.connection.send(new ClientboundBlockUpdatePacket(serverPlayer.level, pos)));
            }
        }
        return super.onBlockStartBreak(stack, pos, player);
    }

    public Stream<BlockPos> getAllHarvestBlocks(ItemStack stack, BlockPos pos, Player player, boolean allBlocks) {
        return this.getAllHarvestBlocks(stack, pos, player, null, allBlocks, true);
    }

    public Stream<BlockPos> getAllHarvestBlocks(ItemStack stack, BlockPos pos, Player player, @Nullable Direction lastHitBlockDirection, boolean allBlocks, boolean applyFilter) {
        if (lastHitBlockDirection == null) {
            lastHitBlockDirection = this.getLastHitBlockDirection(player, pos);
        }
        if (lastHitBlockDirection != null && this.canHarvestBlockEfficiently(stack, player.level, pos, player)) {
//            RangedDiggerItemMode harvestMode = this.getHarvestMode(stack);
//            List<BlockPos> blocks = getNeighboringBlocksOnAxis(pos, harvestMode.distance, harvestMode.depth, lastHitBlockDirection, allBlocks);
//            List<BlockPos> fallingBlocks = this.collectFallingBlocks(player, pos, blocks);
//            Stream<BlockPos> stream = Stream.concat(blocks.stream(), fallingBlocks.stream());
            List<BlockPos> blocks = getConnectedBlocks2(player.level, pos, allBlocks, 64, 178, 0);
            Stream<BlockPos> stream = blocks.stream();
            return applyFilter ? stream.filter(pos1 -> this.canHarvestBlockEfficiently(stack, player.level, pos1, player)) : stream;
        }
        return Stream.empty();
    }

    private List<BlockPos> collectFallingBlocks(Player player, BlockPos pos, List<BlockPos> blocks) {
        Map.Entry<Integer, List<BlockPos>> lastEntry = Stream.concat(blocks.stream(), Stream.of(pos)).collect(Collectors.groupingBy(Vec3i::getY, TreeMap::new, Collectors.toList())).lastEntry();
        List<BlockPos> fallingBlocks = Lists.newArrayList();
        if (lastEntry == null) return fallingBlocks;
        for (BlockPos topBlock : lastEntry.getValue()) {
            BlockPos.MutableBlockPos mutable = topBlock.mutable();
            int i = 0;
            while (i++ < FALLING_HARVEST_LIMIT && player.level.getBlockState(mutable.move(Direction.UP)).getBlock() instanceof FallingBlock) {
                fallingBlocks.add(mutable.immutable());
            }
        }
        return fallingBlocks;
    }

    @Nullable
    private Direction getLastHitBlockDirection(Player player, BlockPos pos) {
        LazyOptional<LastHitBlockCapability> optional = player.getCapability(ModRegistry.LAST_HIT_BLOCK_CAPABILITY);
        if (optional.isPresent()) {
            LastHitBlockCapability capability = optional.orElseThrow(IllegalStateException::new);
            if (capability.isDataValid(pos)) {
                return capability.getLastHitBlockDirection();
            }
        }
        return null;
    }

    public RangedDiggerItemMode getHarvestMode(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag.contains(TAG_HARVEST_MODE)) {
                int harvestModeId = tag.getInt(TAG_HARVEST_MODE);
                if (harvestModeId < RangedDiggerItemMode.values().length) {
                    return RangedDiggerItemMode.values()[harvestModeId];
                }
            }
        }
        return RangedDiggerItemMode.SINGLE;
    }

    public void setHarvestMode(ItemStack stack, RangedDiggerItemMode mode) {
        stack.getOrCreateTag().putInt(TAG_HARVEST_MODE, mode.ordinal());
    }

    private boolean canHarvestBlockEfficiently(ItemStack stack, Level level, BlockPos pos, Player player) {
        // first check if tool is effective (block might still be able to break with drops, but we don't want that case)
        // second check is if we will get drops
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && stack.getDestroySpeed(state) > 1.0F && state.canHarvestBlock(level, pos, player);
    }

    private static List<BlockPos> getNeighboringBlocksOnAxis(BlockPos pos, int distance, boolean withDepth, Direction direction, boolean all) {
        if (distance < 1) return all ? ImmutableList.of(pos) : ImmutableList.of();
        Direction.Axis axis = direction.getAxis();
        BlockPos cubeCenterPos = pos.relative(direction.getOpposite());
        int minHeight;
        int maxHeight;
        if (axis != Direction.Axis.Y) {
            minHeight = cubeCenterPos.getY() - 1;
            maxHeight = cubeCenterPos.getY() + 2 * distance - 1;
        } else {
            minHeight = cubeCenterPos.getY() - distance;
            maxHeight = cubeCenterPos.getY() + distance;
        }
        int axisCoordinate = axis.choose(pos.getX(), pos.getY(), pos.getZ());
        return BlockPos.betweenClosedStream(cubeCenterPos.getX() - distance, minHeight, cubeCenterPos.getZ() - distance, cubeCenterPos.getX() + distance, maxHeight, cubeCenterPos.getZ() + distance).filter(pos1 -> {
            return (withDepth || axis.choose(pos1.getX(), pos1.getY(), pos1.getZ()) == axisCoordinate) && (all || !pos1.equals(pos));
        }).map(BlockPos::immutable).toList();
    }

//    private static void getConnectedBlocks(Level level, BlockPos pos) {
//        Long2IntMap knownPositions = new Long2IntOpenHashMap();
//        List<BlockPos> validPositions = Lists.newArrayList();
//    }
//
//
//    private static void getConnectedBlocks(Level level, BlockPos start, BlockPos.MutableBlockPos pos, Block block, Long2IntMap knownPositions, List<BlockPos> validPositions, ) {
//        for (BlockPos offset : CONNECTED_BLOCK_OFFSETS) {
//            pos.offset(offset);
//            long asLong = pos.asLong();
//            if (!knownPositions.containsKey(asLong)) {
//                knownPositions.add(asLong);
//                if (level.getBlockState(pos).getBlock() == block) {
//                    validPositions.add(pos.immutable());
//                    getConnectedBlocks(level, pos, block, knownPositions, validPositions);
//                }
//            }
//            pos.offset(offset.multiply(-1));
//        }
//    }

    public static final List<BlockPos> CONNECTED_BLOCK_OFFSETS = BlockPos.betweenClosedStream(-1, -1, -1, 1, 1, 1)
            .map(BlockPos::immutable)
            .sorted(Comparator.<BlockPos>comparingInt(pos -> pos.distManhattan(BlockPos.ZERO)).thenComparing(Comparator.<BlockPos>comparingInt(BlockPos::getY).reversed()))
            .skip(1)
            .toList();

    private static List<BlockPos> getConnectedBlocks2(Level level, BlockPos start, boolean allBlocks, int maxDistance, int maxBlocks, int maxFails) {
        Block block = level.getBlockState(start).getBlock();
        List<BlockPos> validPositions = Lists.newLinkedList();
        LongSet knownPositions = new LongOpenHashSet();
        Queue<Pair<BlockPos, Integer>> positions = new ArrayDeque<>();
        if (allBlocks) {
            validPositions.add(start);
        } else {
            maxBlocks--;
        }
        knownPositions.add(start.asLong());
        positions.offer(Pair.of(start, 0));
        int jj = 0;
        int jjj = 0;
        main : for (int i = 0; i < maxDistance; i++) {
            while (!positions.isEmpty()) {
                Pair<BlockPos, Integer> pair = positions.poll();
                for (BlockPos pos : CONNECTED_BLOCK_OFFSETS) {
                    BlockPos offsetPos = pair.left().offset(pos);
                    long asLong = offsetPos.asLong();
                    jj++;
                    if (!knownPositions.contains(asLong)) {
                        jjj++;
                        knownPositions.add(asLong);
                        boolean validBlock = level.getBlockState(offsetPos).getBlock() == block;
                        int fails = pair.right();
                        if (validBlock || fails < maxFails) {
                            if (validBlock) {
                                validPositions.add(offsetPos);
                                if (validPositions.size() >= maxBlocks) break main;
                            }
                            positions.offer(Pair.of(offsetPos, !validBlock ? ++fails : 0));
                        }
                    }
                }
            }
        }
        GamblingStyle.LOGGER.info("total positions: {}, checked positions: {}, unique positions {}", validPositions.size(), jj, jjj);
        return ImmutableList.copyOf(validPositions);
    }

    public enum RangedDiggerItemMode {
        SINGLE("item.gamblingstyle.rangeddiggeritem.mode.single", 0, false, 0),
        PLANE_3("item.gamblingstyle.rangeddiggeritem.mode.plane3", 1, false, 0),
        CUBE_3("item.gamblingstyle.rangeddiggeritem.mode.cube3", 1, true, 1),
        PLANE_5("item.gamblingstyle.rangeddiggeritem.mode.plane5", 2, false, 1),
        CUBE_5("item.gamblingstyle.rangeddiggeritem.mode.cube5", 2, true, 3);

        private final String translationKey;
        public final int distance;
        public final boolean depth;
        private final int enchantmentLevel;

        RangedDiggerItemMode(String translationKey, int distance, boolean depth, int enchantmentLevel) {
            this.translationKey = translationKey;
            this.distance = distance;
            this.depth = depth;
            this.enchantmentLevel = enchantmentLevel;
        }

        public Component makeComponent(boolean withStyle) {
            MutableComponent modeComponent = new TranslatableComponent(this.translationKey);
            if (withStyle) modeComponent.withStyle(ChatFormatting.GOLD);
            MutableComponent component = new TranslatableComponent("item.gamblingstyle.rangeddiggeritem.mode", modeComponent);
            if (withStyle) component.withStyle(ChatFormatting.GRAY);
            return component;
        }

        public RangedDiggerItemMode getNextItemMode(ItemStack stack) {
            List<RangedDiggerItemMode> availableItemModes = this.getAvailableItemModes(stack);
            int index = availableItemModes.indexOf(this);
            if (index == -1) {
                return SINGLE;
            }
            return availableItemModes.get(++index % availableItemModes.size());
        }

        private List<RangedDiggerItemMode> getAvailableItemModes(ItemStack stack) {
            int level = EnchantmentHelper.getItemEnchantmentLevel(ModRegistry.POTENCY_ENCHANTMENT.get(), stack);
            return Stream.of(RangedDiggerItemMode.values())
                    .filter(mode -> level >= mode.enchantmentLevel)
                    .toList();
        }
    }
}
