package tk.estecka.nokebab.mixin;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.PaintingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import tk.estecka.nokebab.IPaintingEntityDuck;

/**
 * Euler to quaternions: https://computergraphics.stackexchange.com/a/8229
 * x = sin(roll/2) * cos(pitch/2) * cos(yaw/2) - cos(roll/2) * sin(pitch/2) * sin(yaw/2)
 * y = cos(roll/2) * sin(pitch/2) * cos(yaw/2) + sin(roll/2) * cos(pitch/2) * sin(yaw/2)
 * z = cos(roll/2) * cos(pitch/2) * sin(yaw/2) - sin(roll/2) * sin(pitch/2) * cos(yaw/2)
 * w = cos(roll/2) * cos(pitch/2) * cos(yaw/2) + sin(roll/2) * sin(pitch/2) * sin(yaw/2)
 */

@Mixin(PaintingEntityRenderer.class)
public abstract class PaintingEntityRendererMixin 
extends EntityRenderer<PaintingEntity>
{
	private PaintingEntityRendererMixin() { super(null); throw new AssertionError(); }

	@Inject( method="render", at=@At("TAIL") )
	void	renderMissingnoLabel(PaintingEntity painting, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info)
	{
		if (!IPaintingEntityDuck.Of(painting).GetRawVariant().isEmpty()) {
			final TextRenderer renderer = this.getTextRenderer();
			String variantId = IPaintingEntityDuck.Of(painting).GetRawVariant();
			float x = -renderer.getWidth(variantId)/2;
			float y = -painting.getHeight();

			// For the text renderer: roll is X, yaw is Z, pitch is Y
			// Assuming roll==0 and yaw==0 simplifies a bunch of factors to either 0 or 1.
			double pitch = -Math.toRadians(yaw+180) / 2;
			Quaternionf entityRotation = new Quaternionf();
			entityRotation.x = 0;
			entityRotation.y = (float)Math.sin(pitch);
			entityRotation.z = 0;
			entityRotation.w = (float)Math.cos(pitch);

			matrices.push();
			matrices.scale(-0.025f, -0.025f, -0.025f);
			matrices.multiply(entityRotation);
			matrices.translate(0, -4, 2);
			renderer.draw(variantId, x, y, 0xff000000, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextLayerType.NORMAL, 0x0, light);
			matrices.translate(0, 0, 1);
			renderer.draw(variantId, x, y, 0xffff88ff, true, matrices.peek().getPositionMatrix(), vertexConsumers, TextLayerType.NORMAL, 0x0, light);
			matrices.pop();
		}
	}

}
