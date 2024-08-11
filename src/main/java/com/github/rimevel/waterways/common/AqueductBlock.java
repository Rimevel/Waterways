package com.github.rimevel.waterways.common;

import com.github.rimevel.waterways.DirectionPropertyBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.material.FluidState;

public class AqueductBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape[] SHAPES = new VoxelShape[16];

    private static final int SHORT_TICK_DELAY = 5;
    private static final int LONG_TICK_DELAY = SHORT_TICK_DELAY * 4;

    static
    {
        final VoxelShape north = box(4, 10, 0, 12, 16, 4);
        final VoxelShape east = box(12, 10, 4, 16, 16, 12);
        final VoxelShape south = box(4, 10, 12, 12, 16, 16);
        final VoxelShape west = box(0, 10, 4, 4, 16, 12);

        // Must match Direction.data2d order
        final VoxelShape[] directions = new VoxelShape[] {south, west, north, east};

        final VoxelShape base = Shapes.or(
                box(0, 0, 0, 16, 10, 16),
                box(0, 10, 0, 4, 16, 4),
                box(12, 10, 0, 16, 16, 4),
                box(0, 10, 12, 4, 16, 16),
                box(12, 10, 12, 16, 16, 16)
        );

        for (int i = 0; i < SHAPES.length; i++)
        {
            VoxelShape shape = base;
            for (Direction direction : Direction.Plane.HORIZONTAL)
            {
                if (((i >> direction.get2DDataValue()) & 1) == 0)
                {
                    shape = Shapes.or(shape, directions[direction.get2DDataValue()]);
                }
            }
            SHAPES[i] = shape;
        }
    }

    public AqueductBlock(Properties props) {
        super(props);
        registerDefaultState(
                getStateDefinition()
                        .any()
                        .setValue(NORTH, false)
                        .setValue(EAST, false)
                        .setValue(SOUTH, false)
                        .setValue(WEST, false)
                        .setValue(FACING, Direction.NORTH)
                        .setValue(WATERLOGGED, false));
    }

    private static boolean isValidSource(BlockState state)
    {
        // An aqueduct source may be a source block, or a flowing fluid at level = 8
        final boolean isWater = !state.isAir() && state.getFluidState().is(Fluids.WATER);
        final boolean isSource = state.getFluidState().isSource();
        final boolean isFlowingDown = (state.getFluidState().hasProperty(FlowingFluid.LEVEL) && state.getFluidState().getValue(FlowingFluid.LEVEL) == 8);

        return isWater && (isSource || isFlowingDown);
    }

    private static BlockState updateOpenSides(LevelAccessor level, BlockPos pos, BlockState state)
    {
        int openSides = 0;
        @Nullable Direction openDirection = null;
        for (final Direction direction : Direction.Plane.HORIZONTAL)
        {
            final BlockPos adjacentPos = pos.relative(direction);
            final BlockState adjacentState = level.getBlockState(adjacentPos);
            final boolean adjacentAqueduct = adjacentState.getBlock() instanceof AqueductBlock;
            if (adjacentAqueduct)
            {
                openSides++;
                openDirection = direction;
            }

            state = state.setValue(DirectionPropertyBlock.getProperty(direction), adjacentAqueduct);
        }

        if (openSides == 1)
        {
            // If we only have a single open side, then we always treat this as a straight aqueduct.
            state = state.setValue(DirectionPropertyBlock.getProperty(openDirection.getOpposite()), true);
        }

        return state;
    }

    private static void tickAllAdjacentAqueducts(LevelAccessor level, BlockPos pos, int tickDelay, @Nullable Direction excludedDirection)
    {
        for (final Direction direction : Direction.Plane.HORIZONTAL)
        {
            if (direction == excludedDirection)
            {
                continue;
            }
            final BlockPos adjacentPos = pos.relative(direction);
            final BlockState adjacentState = level.getBlockState(adjacentPos);
            if (adjacentState.getBlock() instanceof AqueductBlock)
            {
                level.scheduleTick(adjacentPos, adjacentState.getBlock(), tickDelay);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static void tickFluid(LevelAccessor level, BlockPos pos, BlockState state)
    {
        if (!state.getFluidState().isEmpty())
        {
            final Fluid fluid = state.getFluidState().getType();
            level.scheduleTick(pos, fluid, fluid.getTickDelay(level));
        }
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid)
    {
        return fluid == Fluids.WATER;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return updateOpenSides(context.getLevel(), context.getClickedPos(), defaultBlockState());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        level.scheduleTick(pos, this, LONG_TICK_DELAY);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (newState.getBlock() != this && // When replacing with another block, aka air
                state.getValue(WATERLOGGED).booleanValue() && // And the aqueduct currently has fluid
                newState.getFluidState().getType().isSame(Fluids.WATER)) // And we're replacing with the same fluid currently in the block
        {
            // Then, replace with a non-source block of the new fluid, if we can, or air if we can't.
            final BlockState newFluidState = newState.getFluidState().getType() instanceof FlowingFluid fluid ? fluid.getFlowing(1, false).createLegacyBlock() : Blocks.AIR.defaultBlockState();
            level.setBlock(pos, newFluidState, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPES[(state.getValue(NORTH) ? 1 << Direction.NORTH.get2DDataValue() : 0) |
                (state.getValue(EAST) ? 1 << Direction.EAST.get2DDataValue() : 0) |
                (state.getValue(SOUTH) ? 1 << Direction.SOUTH.get2DDataValue() : 0) |
                (state.getValue(WEST) ? 1 << Direction.WEST.get2DDataValue() : 0)];
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction direction, BlockState adjacentState, LevelAccessor level, BlockPos pos, BlockPos adjacentPos)
    {
        tickFluid(level, pos, state);
        final BlockState newState = updateOpenSides(level, pos, state);
        if (state != newState || (state.getFluidState().is(Fluids.EMPTY) ? direction.getAxis().getPlane() == Direction.Plane.HORIZONTAL && state.getValue(DirectionPropertyBlock.getProperty(direction)) : direction == state.getValue(FACING)))
        {
            level.scheduleTick(pos, this, LONG_TICK_DELAY);
        }
        return newState;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(NORTH, EAST, SOUTH, WEST, FACING, WATERLOGGED);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.defaultFluidState() : super.getFluidState(state);
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidStateIn)
    {
        final boolean result = SimpleWaterloggedBlock.super.placeLiquid(level, pos, state, fluidStateIn);
        if (result)
        {
            level.scheduleTick(pos, this, SHORT_TICK_DELAY);
        }
        return result;
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state)
    {
        if (state.getFluidState().is(Fluids.EMPTY))
        {
            level.scheduleTick(pos, this, LONG_TICK_DELAY);
        }
        return SimpleWaterloggedBlock.super.pickupBlock(level, pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        // First, if we have a fluid, we have to check if this fluid is still valid
        final Direction sourceDirection = state.getValue(FACING);
        final boolean sourceIsWater = state.getValue(WATERLOGGED);

        if (sourceIsWater)
        {
            // This aqueduct currently has water, incoming from a direction. Validate that still exists
            boolean valid = false;

            // Must always be open on this side, otherwise it cannot possibly be valid
            if (state.getValue(DirectionPropertyBlock.getProperty(sourceDirection)))
            {
                //State of the block adjacent to this one in the upstream direction
                final BlockState sourceState = level.getBlockState(pos.relative(sourceDirection));
                if (sourceState.getBlock() instanceof AqueductBlock)
                {
                    // Validate that the aqueduct is not also expecting this block as a source, and it contains the same fluid.
                    valid = sourceState.getValue(WATERLOGGED) && sourceState.getValue(FACING) != sourceDirection.getOpposite();
                }
                else if (isValidSource(sourceState) && sourceState.getFluidState().getType() == Fluids.WATER)
                {
                    // Source block is valid
                    valid = true;
                }
            }

            // Need to check that the source has not become invalid - otherwise, we just carry on.
            if (!valid)
            {
                // The source has become invalid
                state = state.setValue(BlockStateProperties.WATERLOGGED, false);

                // Tick adjacent aqueducts at a short tick delay
                // We need to do this before setting the block, as the latter operation may cause updateShape() -> scheduleTick()s to occur
                // Those ticks will be at a long delay, and we want the short tick here to take priority.
                tickAllAdjacentAqueducts(level, pos, SHORT_TICK_DELAY, sourceDirection);

                level.setBlockAndUpdate(pos, state);
            }
        }
        else
        {
            // If the aqueduct doesn't currently have a source, and is being ticked, we want to look for one
            boolean filled = false;
            for (final Direction direction : Direction.Plane.HORIZONTAL)
            {
                // Only consider directions where this block is open
                if (!state.getValue(DirectionPropertyBlock.getProperty(direction)))
                {
                    continue;
                }

                final BlockPos adjacentPos = pos.relative(direction);
                final BlockState adjacentState = level.getBlockState(adjacentPos);
                final Fluid adjacentFluid = adjacentState.getFluidState().getType() instanceof FlowingFluid flow ? flow.getSource() : adjacentState.getFluidState().getType();
                if (adjacentState.getBlock() instanceof AqueductBlock)
                {
                    // Adjacent aqueduct - it must be open, and contain a fluid, and not pointing to this block as a source
                    if (adjacentState.getValue(DirectionPropertyBlock.getProperty(direction.getOpposite())) && adjacentState.getValue(FACING) != direction.getOpposite() && !(adjacentState.getFluidState().getType() == Fluids.EMPTY))
                    {
                        // Then, the first one we find, we can flow into this block
                        state = state.setValue(FACING, direction)
                                .setValue(WATERLOGGED, adjacentState.getValue(WATERLOGGED));
                        filled = true;
                        break;
                    }
                }
                else if (isValidSource(adjacentState))
                {
                    // Source blocks can always flow into an open aqueduct
                    state = state.setValue(FACING, direction)
                            .setValue(WATERLOGGED, true);
                    filled = true;
                    break;
                }
            }

            if (filled)
            {
                // Update this block
                level.setBlockAndUpdate(pos, state);
                level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));

                // If we managed to fill this aqueduct in this tick, then we want to schedule ticks for all it's neighbors
                tickAllAdjacentAqueducts(level, pos, LONG_TICK_DELAY, state.getValue(FACING));
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, Rotation rot)
    {
        return DirectionPropertyBlock.rotate(super.rotate(state, rot), rot);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirror)
    {
        // super method uses rotate which breaks the orientation of asymmetrical blocks
        return DirectionPropertyBlock.mirror(state, mirror).setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }
}
