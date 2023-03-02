package de.ellpeck.nyx.blocks;

import de.ellpeck.nyx.Registry;
import de.ellpeck.nyx.items.ItemNyxSlab;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeModContainer;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Random;
import java.util.function.Supplier;

public abstract class NyxSlab extends Block {

    protected static final PropertyEnum<EnumBlockHalf> HALF = PropertyEnum.create("half", EnumBlockHalf.class);
    protected static final AxisAlignedBB AABB_BOTTOM_HALF = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
    protected static final AxisAlignedBB AABB_TOP_HALF = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);

    public final Supplier<NyxSlab> singleSlab;
    public final Supplier<NyxSlab> doubleSlab;

    public NyxSlab(Material materialIn, Supplier<NyxSlab> singleSlab, Supplier<NyxSlab> doubleSlab) {
        super(materialIn);
        this.singleSlab = singleSlab;
        this.doubleSlab = doubleSlab;
    }

    @Override
    protected boolean canSilkHarvest() {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (this.isDouble())
            return FULL_BLOCK_AABB;
        else
            return state.getValue(HALF) == EnumBlockHalf.TOP ? AABB_TOP_HALF : AABB_BOTTOM_HALF;
    }

    @Override
    public boolean isTopSolid(IBlockState state) {
        return this.isDouble() || state.getValue(HALF) == EnumBlockHalf.TOP;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        if (this.isDouble())
            return BlockFaceShape.SOLID;
        else if (face == EnumFacing.UP && state.getValue(HALF) == EnumBlockHalf.TOP)
            return BlockFaceShape.SOLID;
        else
            return face == EnumFacing.DOWN && state.getValue(HALF) == EnumBlockHalf.BOTTOM ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return this.isDouble();
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return this.isDouble();
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return this.isDouble();
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return this.isDouble();
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        if (ForgeModContainer.disableStairSlabCulling)
            return super.doesSideBlockRendering(state, world, pos, face);

        if (state.isOpaqueCube())
            return true;

        EnumBlockHalf side = state.getValue(HALF);
        return (side == EnumBlockHalf.TOP && face == EnumFacing.UP) || (side == EnumBlockHalf.BOTTOM && face == EnumFacing.DOWN);
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        if (this.isDouble())
            return this.getDefaultState();
        else {
            IBlockState state = this.getStateFromMeta(meta);
            return facing != EnumFacing.DOWN && (facing == EnumFacing.UP || (double) hitY <= 0.5D) ?
                    state.withProperty(HALF, EnumBlockHalf.BOTTOM) : state.withProperty(HALF, EnumBlockHalf.TOP);
        }
    }

    @Override
    public int quantityDropped(Random random) {
        return this.isDouble() ? 2 : 1;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(this.singleSlab.get());
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return this.isDouble() ? new BlockStateContainer(this) : new BlockStateContainer(this, HALF);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return this.isDouble() ? 0 : (state.getValue(HALF) == EnumBlockHalf.TOP ? 1 : 0);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.isDouble() ? this.getDefaultState() : this.getDefaultState().withProperty(HALF, meta == 1 ? EnumBlockHalf.TOP : EnumBlockHalf.BOTTOM);
    }

    public abstract boolean isDouble();

    public static NyxSlab[] makeSlab(String baseName, Material material, SoundType soundType, float hardness) {
        MutableObject<NyxSlab> singl = new MutableObject<>();
        MutableObject<NyxSlab> doubl = new MutableObject<>();
        singl.setValue(new NyxSlab(material, singl::getValue, doubl::getValue) {
            @Override
            public boolean isDouble() {
                return false;
            }
        });
        singl.getValue().setSoundType(soundType).setHardness(hardness);
        Registry.initBlock(singl.getValue(), baseName, b -> new ItemNyxSlab(b, singl::getValue, doubl::getValue));
        doubl.setValue(new NyxSlab(material, singl::getValue, doubl::getValue) {
            @Override
            public boolean isDouble() {
                return true;
            }
        });
        doubl.getValue().setSoundType(soundType).setHardness(hardness);
        Registry.initBlock(doubl.getValue(), baseName + "_double", null);
        return new NyxSlab[]{singl.getValue(), doubl.getValue()};
    }
}