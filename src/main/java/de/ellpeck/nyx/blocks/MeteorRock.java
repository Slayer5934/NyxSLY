package de.ellpeck.nyx.blocks;

import de.ellpeck.nyx.capabilities.NyxWorld;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;
import java.util.function.Supplier;

public class MeteorRock extends Block {

    private final Supplier<Item> droppedItem;

    public MeteorRock(Supplier<Item> droppedItem) {
        super(Material.ROCK);
        this.droppedItem = droppedItem;

        this.setHarvestLevel("pickaxe", 3);
        this.setHardness(40);
        this.setResistance(3000);
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!worldIn.isRemote) {
            NyxWorld data = NyxWorld.get(worldIn);
            if (data != null) {
                data.meteorLandingSites.remove(pos);
                data.sendToClients();
            }
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return this.droppedItem.get();
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        if (!entityIn.isImmuneToFire() && entityIn instanceof EntityLivingBase && !EnchantmentHelper.hasFrostWalkerEnchantment((EntityLivingBase) entityIn))
            entityIn.attackEntityFrom(DamageSource.HOT_FLOOR, 1);
        super.onEntityWalk(worldIn, pos, entityIn);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        for (int i = 0; i < 3; i++) {
            boolean side = rand.nextBoolean();
            float x = side ? rand.nextFloat() : rand.nextBoolean() ? 1 : 0;
            float z = !side ? rand.nextFloat() : rand.nextBoolean() ? 1 : 0;
            float y = rand.nextBoolean() ? 1 : 0;
            worldIn.spawnParticle(EnumParticleTypes.SUSPENDED_DEPTH, pos.getX() + x, pos.getY() + y, pos.getZ() + z, 0, 0, 0);
        }
    }
}
