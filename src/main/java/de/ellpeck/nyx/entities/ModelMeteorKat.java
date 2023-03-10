package de.ellpeck.nyx.entities;

import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.Entity;
import net.minecraft.client.model.ModelOcelot;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ModelMeteorKat extends ModelOcelot
{
	ModelRenderer helmet;

	public ModelMeteorKat() {
		super();
		this.setTextureOffset("helmet.main", 38, 19);
		this.helmet = new ModelRenderer(this, "helmet");
		this.helmet.addBox("main", -3F, -3.5F, -4.5F, 6, 6, 7);
		this.helmet.setRotationPoint(0.0F, 15.0F, -9.0F);
	}

	@Override
	public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity) {
		super.setRotationAngles(par1, par2, par3, par4, par5, par6, par7Entity);
		this.helmet.rotateAngleX = par5 / (180F / (float)Math.PI);
		this.helmet.rotateAngleY = par4 / (180F / (float)Math.PI);
	}

	@Override
	public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7) {
		super.render(par1Entity, par2, par3, par4, par5, par6, par7);
		if (this.isChild) {
			float var8 = 2.0F;
			GL11.glPushMatrix();
			GL11.glScalef(1.5F / var8, 1.5F / var8, 1.5F / var8);
			GL11.glTranslatef(0.0F, 10.0F * par7, 4.0F * par7);
			this.helmet.render(par7);
			GL11.glPopMatrix();
		} else {
			this.helmet.render(par7);
		}
	}

	@Override
	public void setLivingAnimations(EntityLivingBase par1EntityLiving, float par2, float par3, float par4) {
		super.setLivingAnimations(par1EntityLiving, par2, par3, par4);
		EntityOcelot var5 = (EntityOcelot)par1EntityLiving;
		this.helmet.rotationPointY = 15.0F;
		this.helmet.rotationPointZ = -9.0F;
		if (var5.isSneaking()) {
			this.helmet.rotationPointY += 2.0F;
		} else if (var5.isSitting()) {
			this.helmet.rotationPointY += -3.3F;
			++this.helmet.rotationPointZ;
		}
	}
}