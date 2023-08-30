package tk.estecka.nokebab.mixin;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PaintingEntityRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.util.Identifier;
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
{
	static private final Identifier MISSINGNO_ID = new Identifier("nokebab", "missingno");
	private final PaintingEntityRenderer paintingRenderer = (PaintingEntityRenderer)(Object)this;


	@Shadow
	private void renderPainting(MatrixStack matrices, VertexConsumer vertexConsumer, PaintingEntity entity, int width, int height, Sprite paintingSprite, Sprite backSprite)
	{ throw new AssertionError(); }


	@Inject( method="render", at=@At("TAIL") )
	void	renderMissingnoLabel(PaintingEntity painting, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertex, int light, CallbackInfo info)
	{
		if (!IPaintingEntityDuck.Of(painting).GetRawVariant().isEmpty()) {
			final TextRenderer textRenderer = paintingRenderer.getTextRenderer();
			String variantId = IPaintingEntityDuck.Of(painting).GetRawVariant();
			float x = -textRenderer.getWidth(variantId)/2;
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
			matrices.translate(0, -4, 2.5f);
			textRenderer.draw(variantId, x, y, 0xff000000, false, matrices.peek().getPositionMatrix(), vertex, TextLayerType.NORMAL, 0x0, light);
			matrices.translate(0, 0, 0.5f);
			textRenderer.draw(variantId, x, y, 0xffff88ff, true, matrices.peek().getPositionMatrix(), vertex, TextLayerType.NORMAL, 0x0, light);
			matrices.pop();
		}
	}

	@Redirect( method="render", at=@At(value="INVOKE", target="net/minecraft/client/render/entity/PaintingEntityRenderer.renderPainting (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/entity/decoration/painting/PaintingEntity;IILnet/minecraft/client/texture/Sprite;Lnet/minecraft/client/texture/Sprite;)V") )
	void	renderMissingno(PaintingEntityRenderer renderer, MatrixStack matrices, VertexConsumer vertexConsumer, PaintingEntity painting, int width, int height, Sprite paintingSprite, Sprite backSprite) {
		if (!IPaintingEntityDuck.Of(painting).GetRawVariant().isEmpty()) {
			ISpriteAtlasHolderMixin atlas = (ISpriteAtlasHolderMixin)MinecraftClient.getInstance().getPaintingManager();
			paintingSprite = atlas.GetSpriteFromID(MISSINGNO_ID);
		}
		this.renderPainting(matrices, vertexConsumer, painting, width, height, paintingSprite, backSprite);
	}

}
