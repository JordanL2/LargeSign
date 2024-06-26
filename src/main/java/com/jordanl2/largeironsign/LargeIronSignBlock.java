package com.jordanl2.largeironsign;

import java.util.Map;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class LargeIronSignBlock extends HorizontalFacingBlock implements BlockEntityProvider, Waterloggable {
	
	// BlockState properties
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	
	// IDs
	public static final String PATH = "large_iron_sign";
	public static final String BLOCK_PATH = "block/" + PATH;
	public static final String ITEM_PATH = "item/" + PATH;
	public static final Identifier ID = new Identifier(LargeIronSign.MOD_ID, PATH);
	
	// Block and Item singletons
	public static final LargeIronSignBlock LARGE_IRON_SIGN_BLOCK = new LargeIronSignBlock(FabricBlockSettings.create()
			.requiresTool()
			.strength(1.5f, 6.0f));
	public static final BlockItem LARGE_IRON_SIGN_BLOCK_ITEM = new BlockItem(
			LargeIronSignBlock.LARGE_IRON_SIGN_BLOCK, 
			new FabricItemSettings());
	
	// Textures
	public static final Identifier EDGE_TEXTURE = new Identifier(LargeIronSign.MOD_ID, "block/" + PATH + "_edge");
	public static final Identifier BACK_TEXTURE = new Identifier(LargeIronSign.MOD_ID, "block/" + PATH + "_back");
	public static final Identifier FRONT_TEXTURE = new Identifier(LargeIronSign.MOD_ID, "block/" + PATH + "_front");
	
	// Network packets
	public static final Identifier LARGE_IRON_SIGN_SCREEN_OPEN_PACKET_ID = new Identifier(LargeIronSign.MOD_ID, PATH + "_screen_open");
	public static final Identifier LARGE_IRON_SIGN_SET_SYMBOL_PACKET_ID = new Identifier(LargeIronSign.MOD_ID, PATH + "_set_symbol");
	public static final Identifier LARGE_IRON_SIGN_REFRESH_MODEL_PACKET_ID = new Identifier(LargeIronSign.MOD_ID, PATH + "_refresh_model");
	
	// Dyes
	public static final int DEFAULT_COLOUR_FOREGROUND = DyeColor.BLACK.getSignColor() | 0xff000000;
	public static final int DEFAULT_COLOUR_BACKGROUND = DyeColor.WHITE.getSignColor() | 0xff000000;
	public static final Map<Item, Integer> DYES = Map.ofEntries(
			Map.entry(Items.BLACK_DYE,      DyeColor.BLACK.getSignColor()      | 0xff000000),
			Map.entry(Items.GRAY_DYE,       DyeColor.GRAY.getSignColor()       | 0xff000000),
			Map.entry(Items.LIGHT_GRAY_DYE, DyeColor.LIGHT_GRAY.getSignColor() | 0xff000000),
			Map.entry(Items.WHITE_DYE,      DyeColor.WHITE.getSignColor()      | 0xff000000),
			Map.entry(Items.RED_DYE,        DyeColor.RED.getSignColor()        | 0xff000000),
			Map.entry(Items.BROWN_DYE,      DyeColor.BROWN.getSignColor()      | 0xff000000),
			Map.entry(Items.ORANGE_DYE,     DyeColor.ORANGE.getSignColor()     | 0xff000000),
			Map.entry(Items.YELLOW_DYE,     DyeColor.YELLOW.getSignColor()     | 0xff000000),
			Map.entry(Items.LIME_DYE,       DyeColor.LIME.getSignColor()       | 0xff000000),
			Map.entry(Items.GREEN_DYE,      DyeColor.GREEN.getSignColor()      | 0xff000000),
			Map.entry(Items.CYAN_DYE,       DyeColor.CYAN.getSignColor()       | 0xff000000),
			Map.entry(Items.LIGHT_BLUE_DYE, DyeColor.LIGHT_BLUE.getSignColor() | 0xff000000),
			Map.entry(Items.BLUE_DYE,       DyeColor.BLUE.getSignColor()       | 0xff000000),
			Map.entry(Items.PURPLE_DYE,     DyeColor.PURPLE.getSignColor()     | 0xff000000),
			Map.entry(Items.MAGENTA_DYE,    DyeColor.MAGENTA.getSignColor()    | 0xff000000),
			Map.entry(Items.PINK_DYE,       DyeColor.PINK.getSignColor()       | 0xff000000));
	
	public LargeIronSignBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState()
        		.with(FACING, Direction.NORTH)
        		.with(WATERLOGGED, false));
	}
	
	@Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(
				FACING,
				WATERLOGGED);
    }
	
	@Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
		Direction dir = state.get(FACING);
		return LargeIronSignBlock.getOutlineShape(dir);
    }
	
	public static VoxelShape getOutlineShape(Direction dir) {
		switch (dir) {
			case NORTH:
				return VoxelShapes.cuboid(0f, 0f, 0.9375f, 1f, 1f, 1f);
			case SOUTH:
				return VoxelShapes.cuboid(0f, 0f, 0f, 1f, 1f, 0.0625f);
			case EAST:
				return VoxelShapes.cuboid(0f, 0f, 0f, 0.0625f, 1f, 1f);
			case WEST:
				return VoxelShapes.cuboid(0.9375f, 0f, 0f, 1f, 1f, 1f);
			default:
				return VoxelShapes.fullCube();
		}
	}

	@Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		Item item1 = player.getMainHandStack().getItem();
		Item item2 = player.getOffHandStack().getItem();
		if (hand == Hand.MAIN_HAND && !world.isClient() && player instanceof ServerPlayerEntity serverPlayer) {
			// If holding dye, apply foreground / background colour on sign
			if (DYES.containsKey(item1) || DYES.containsKey(item2)) {
				BlockEntity blockEntity = world.getBlockEntity(pos);
				if (blockEntity != null && blockEntity instanceof LargeIronSignBlockEntity largeIronSignBlockEntity) {
					if (DYES.containsKey(item1)) {
						largeIronSignBlockEntity.foreground = DYES.get(item1);
					}
					if (DYES.containsKey(item2)) {
						largeIronSignBlockEntity.background = DYES.get(item2);
					}
					LargeIronSignBlockEntity.syncUpdateToClient(largeIronSignBlockEntity, pos, serverPlayer);
				}
				return ActionResult.SUCCESS;				
			}
			
			// Otherwise, open sign edit screen
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeBlockPos(pos);
			ServerPlayNetworking.send(serverPlayer, LARGE_IRON_SIGN_SCREEN_OPEN_PACKET_ID, buf);
			return ActionResult.SUCCESS;
		}
		
		// If holding dye, ensure animation plays for correct hand
		if (world.isClient()) {
			if (DYES.containsKey(item1) || DYES.containsKey(item2)) {
				if (hand == Hand.MAIN_HAND && DYES.containsKey(item1)) {
					return ActionResult.SUCCESS;
				} else if (hand == Hand.OFF_HAND && DYES.containsKey(item2)) {
					return ActionResult.SUCCESS;
				}
				return ActionResult.PASS;
			}
		}
		
		return ActionResult.SUCCESS;
	}
	
    @Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return super.getPlacementState(ctx)
				.with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing().getOpposite())
				.with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).isOf(Fluids.WATER));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }
    
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
 
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new LargeIronSignBlockEntity(pos, state);
	}
}
