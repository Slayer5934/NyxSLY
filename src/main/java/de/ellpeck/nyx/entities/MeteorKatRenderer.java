package de.ellpeck.nyx.entities;

import net.minecraft.entity.passive.EntityOcelot;
import de.ellpeck.nyx.Nyx;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderOcelot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class MeteorKatRenderer extends RenderOcelot {

	public static final Factory FACTORY = new Factory();
	private static final ResourceLocation skin = new ResourceLocation(Nyx.ID, "textures/entities/meteor_kat.png");

	public MeteorKatRenderer(RenderManager renderManager) {
		super(renderManager);
		mainModel = new ModelMeteorKat();
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityOcelot par1Entity)
    {
        return skin;
    }

	public static class Factory implements IRenderFactory<EntityOcelot> {
		@Override
		public Render<? super EntityOcelot> createRenderFor(RenderManager manager) {
			return new MeteorKatRenderer(manager);
		}
	}
}

