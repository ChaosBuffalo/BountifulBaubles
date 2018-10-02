package cursedflames.bountifulbaubles.block;

import cursedflames.bountifulbaubles.BountifulBaubles;
import cursedflames.lib.block.GenericTileBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

//TODO block rotation
public class BlockReforger extends GenericTileBlock {
	public static final int GUI_ID = 1;

	public BlockReforger() {
		super(BountifulBaubles.MODID, "reforger", TileReforger.class, BountifulBaubles.TAB,
				Material.IRON, 3.0f, 100.0f);
		setLightLevel(13f/16f).setLightOpacity(0);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
			EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY,
			float hitZ) {
		if (world.isRemote) {
			return true;
		}
		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof TileReforger)) {
			return false;
		}

		player.openGui(BountifulBaubles.instance, GUI_ID, world, pos.getX(), pos.getY(),
				pos.getZ());
		return true;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileReforger) {
				ItemStack stack = ((TileReforger) te)
						.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
						.getStackInSlot(0);
				world.spawnEntity(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack));
			}
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos,
			EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	// TODO proper bounding box
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess source,
			BlockPos pos) {
		return NULL_AABB;
	}

	// TODO why does compiler think the super method is abstract?
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		try {
			return tileEntity.newInstance();
		} catch (InstantiationException|IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
}