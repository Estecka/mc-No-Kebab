package tk.estecka.nokebab.mixin;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.PaintingEntityRenderer;
import net.minecraft.client.render.entity.state.PaintingEntityRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import tk.estecka.nokebab.IPaintingEntityDuck;

/**
 * Euler to quaternions: https://computergraphics.stackexchange.com/a/8229
 * x = sin(roll/2) * cos(pitch/2) * cos(yaw/2) - cos(roll/2) * sin(pitch/2) * sin(yaw/2)
 * y = cos(roll/2) * sin(pitch/2) * cos(yaw/2) + sin(roll/2) * cos(pitch/2) * sin(yaw/2)
 * z = cos(roll/2) * cos(pitch/2) * sin(yaw/2) - sin(roll/2) * sin(pitch/2) * cos(yaw/2)
 * w = cos(roll/2) * cos(pitch/2) * cos(yaw/2) + sin(roll/2) * sin(pitch/2) * sin(yaw/2)
 */

@Environment(EnvType.CLIENT)
@Mixin(PaintingEntityRenderer.class)
public abstract class PaintingEntityRendererMixin 
extends EntityRenderer<PaintingEntity,PaintingEntityRenderState>
{
	static private final Identifier MISSINGNO_ID = Identifier.of("nokebab", "missingno");


	private PaintingEntityRendererMixin(){ super(null); }


	@Inject( method="render", at=@At("TAIL") )
	private void	renderMissingnoLabel(PaintingEntity painting, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertex, int light, CallbackInfo info)
	{
		String missingName = IPaintingEntityDuck.Of(painting).nokebab$GetMissingName();

		if (!missingName.isEmpty()) {
			final TextRenderer textRenderer = this.getTextRenderer();
			float x = -textRenderer.getWidth(missingName)/2;
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
			textRenderer.drawWithOutline(Text.literal(missingName).asOrderedText(), x, y, 0xffff88ff, 0xff000000, matrices.peek().getPositionMatrix(), vertex, light);
			matrices.pop();
		}
	}

	@WrapOperation( method="render", at=@At(value="INVOKE", target="net/minecraft/client/render/entity/PaintingEntityRenderer.renderPainting (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/entity/decoration/painting/PaintingEntity;IILnet/minecraft/client/texture/Sprite;Lnet/minecraft/client/texture/Sprite;)V") )
	private void	renderMissingno(PaintingEntityRenderer renderer, MatrixStack matrices, VertexConsumer vertexConsumer, PaintingEntity painting, int width, int height, Sprite paintingSprite, Sprite backSprite, Operation<Void> original) {
		if (!IPaintingEntityDuck.Of(painting).nokebab$GetMissingName().isEmpty()) {
			ISpriteAtlasHolderMixin atlas = (ISpriteAtlasHolderMixin)MinecraftClient.getInstance().getPaintingManager();
			paintingSprite = atlas.GetSpriteFromID(MISSINGNO_ID);
		}
		original.call(renderer, matrices, vertexConsumer, painting, width, height, paintingSprite, backSprite);
	}

}
