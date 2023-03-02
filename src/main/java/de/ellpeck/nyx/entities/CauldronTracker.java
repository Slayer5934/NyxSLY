package de.ellpeck.nyx.entities;

import de.ellpeck.nyx.Config;
import de.ellpeck.nyx.Registry;
import de.ellpeck.nyx.blocks.LunarWaterCauldron;
import de.ellpeck.nyx.capabilities.NyxWorld;
import de.ellpeck.nyx.lunarevents.BloodMoon;
import de.ellpeck.nyx.lunarevents.HarvestMoon;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.List;

public class CauldronTracker extends Entity {

    private static final DataParameter<Boolean> IS_DONE = EntityDataManager.createKey(CauldronTracker.class, DataSerializers.BOOLEAN);
    private BlockPos trackingPos;
    private int timer;

    public CauldronTracker(World worldIn) {
        super(worldIn);
        this.setEntityBoundingBox(null);
    }

    public void setTrackingPos(BlockPos pos) {
        this.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        this.trackingPos = pos;
    }

    @Override
    public void onUpdate() {
        this.onEntityUpdate();
    }

    @Override
    public void onEntityUpdate() {
        if (this.world.isRemote) {
            if (this.dataManager.get(IS_DONE) && this.world.rand.nextBoolean()) {
                double x = this.world.rand.nextFloat() + this.posX - 0.5F;
                double z = this.world.rand.nextFloat() + this.posZ - 0.5F;
                this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, x, this.posY + 0.25F, z, 1, 1, 1);
            }
            return;
        }

        IBlockState state = this.world.getBlockState(this.trackingPos);
        Block block = state.getBlock();
        if (!(block instanceof BlockCauldron) || block instanceof LunarWaterCauldron) {
            this.setDead();
            return;
        }

        int level = state.getValue(BlockCauldron.LEVEL);
        if (level <= 0) {
            if (this.timer > 0) {
                this.dataManager.set(IS_DONE, false);
                this.timer = 0;
            }
            return;
        }

        if (!this.dataManager.get(IS_DONE)) {
            NyxWorld nyx = NyxWorld.get(this.world);
            if (nyx == null || !this.world.canSeeSky(this.trackingPos) || NyxWorld.isDaytime(this.world)) {
                this.timer = 0;
                return;
            }
            int phase = this.world.provider.getMoonPhase(this.world.getWorldTime());
            if (nyx.currentEvent instanceof HarvestMoon) {
                phase = 8;
            } else if (nyx.currentEvent instanceof BloodMoon) {
                phase = 9;
            }
            int ticksRequired = Config.lunarWaterTicks[phase];
            if (ticksRequired >= 0) {
                this.timer++;
                if (this.timer >= ticksRequired)
                    this.dataManager.set(IS_DONE, true);
            }
        } else {
            List<EntityItem> items = this.world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(this.trackingPos));
            for (EntityItem item : items) {
                if (item.isDead)
                    continue;
                ItemStack stack = item.getItem();
                if (stack.getItem() != Items.DYE || stack.getMetadata() != EnumDyeColor.BLUE.getDyeDamage())
                    continue;
                item.setDead();

                IBlockState newState = Registry.lunarWaterCauldron.getDefaultState().withProperty(BlockCauldron.LEVEL, level);
                this.world.setBlockState(this.trackingPos, newState);
                this.world.playSound(null, this.posX, this.posY, this.posZ, Registry.lunarWaterSound, SoundCategory.BLOCKS, 1, 1);
                this.setDead();
            }
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.setTrackingPos(NBTUtil.getPosFromTag(compound.getCompoundTag("tracking_pos")));
        this.timer = compound.getInteger("timer");
        this.dataManager.set(IS_DONE, compound.getBoolean("done"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag("tracking_pos", NBTUtil.createPosTag(this.trackingPos));
        compound.setInteger("timer", this.timer);
        compound.setBoolean("done", this.dataManager.get(IS_DONE));
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(IS_DONE, false);
    }
}
