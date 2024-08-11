package com.github.rimevel.waterways.common;

import com.github.rimevel.waterways.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class BucketNerf {
    @SubscribeEvent
    public static void onRightClickWithBucket(PlayerInteractEvent.RightClickBlock event) {
        if(Config.BUCKET_NERF.get()) {
            Level level = event.getLevel();

            if(level.isClientSide()) {
                return;
            }

            Player player = event.getEntity();
            BlockPos pos = event.getPos();
            ItemStack heldItem = event.getItemStack();

            if(player.isCreative()) {
                return;
            }

            if(heldItem.getItem() instanceof BucketItem && heldItem.getItem() == Items.WATER_BUCKET) {
                if(event.getFace() == null) {
                    return;
                }
                //event.setCanceled(true);
                placeWater(level, pos.relative(event.getFace()), player, !hasAdjacentWaterSource(level, pos.relative(event.getFace())));
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }

    private static boolean hasAdjacentWaterSource(Level level, BlockPos pos) {
        // Check all adjacent positions for water source blocks
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = pos.relative(direction);
            FluidState fluidState = level.getFluidState(adjacentPos);

            if(level.getBlockState(adjacentPos).getBlock() instanceof AqueductBlock) {
                continue;
            }
            if (fluidState.getType() == Fluids.WATER && fluidState.isSource()) {
                return true;
            }
        }
        return false;
    }

    private static void placeWater(Level level, BlockPos pos, Player player, boolean flowing) {
        // Check if the position is empty or can be replaced
        BlockState blockState = level.getBlockState(pos);
        if (blockState.isAir() || blockState.canBeReplaced()) {
            // Place flowing water if no adjacent sources were found
            if(flowing) {
                createFlowingState(level, pos, 1);
                for (Direction dir : Direction.values()) {
                    if(dir == Direction.UP || dir == Direction.DOWN) {
                        continue;
                    }
                    BlockPos relative = pos.relative(dir);
                    BlockState relativeState = level.getBlockState(relative);
                    if (relativeState.isAir() || relativeState.canBeReplaced()) {
                        createFlowingState(level, relative, 3);
                    }
                }
            } else {
                level.setBlock(pos, Blocks.WATER.defaultBlockState().setValue(BlockStateProperties.LEVEL, 0), 3);
            }

            // Use the water bucket and give an empty bucket
            ItemStack held = player.getItemInHand(player.getUsedItemHand());
            held.shrink(1);
            if(held.getCount() == 0) {
                player.setItemInHand(player.getUsedItemHand(), new ItemStack(Items.BUCKET));
                return;
            }
            player.addItem(new ItemStack(Items.BUCKET));
        }
    }

    private static void createFlowingState(Level level, BlockPos pos, int depth) {
        FluidState fluidState = Fluids.FLOWING_WATER.defaultFluidState().setValue(BlockStateProperties.LEVEL_FLOWING, depth);
        level.setBlock(pos, fluidState.createLegacyBlock(), 3);
    }

    @SubscribeEvent
    public static void onFluidCreateSource(BlockEvent.CreateFluidSourceEvent event)
    {
        final BlockState state = event.getState();

        if (state.getBlock() instanceof AqueductBlock)
        {
            event.setResult(Event.Result.DENY); // Waterlogged aqueducts do not count as the source when creating source blocks
        }
    }
}
