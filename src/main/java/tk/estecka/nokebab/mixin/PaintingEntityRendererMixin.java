package tk.estecka.nokebab.mixin;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.PaintingEntityRenderer;
import net.minecraft.client.render.entity.state.PaintingEntityRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import tk.estecka.nokebab.duck.IPaintingEntityDuck;
import tk.estecka.nokebab.duck.IPaintingStateDuck;


@Environment(EnvType.CLIENT)
@Mixin(PaintingEntityRenderer.class)
public abstract class PaintingEntityRendererMixin 
extends EntityRenderer<PaintingEntity,PaintingEntityRenderState>
{
	static private final Identifier MISSINGNO_ID = Identifier.of("nokebab", "missingno");


	private PaintingEntityRendererMixin(){ super(null); }

	@Inject( method="updateRenderState", at=@At("HEAD") )
	public void updateMissingName(PaintingEntity entity, PaintingEntityRenderState state, float tickDelta, CallbackInfo ci) {
		IPaintingStateDuck.Of(state).nokebab$SetMissingName(IPaintingEntityDuck.Of(entity).nokebab$GetMissingName());
	}

	@Inject( method="render", at=@At("TAIL") )
	private void	renderMissingnoLabel(PaintingEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertex, int light, CallbackInfo info)
	{
		String missingName = IPaintingStateDuck.Of(state).nokebab$GetMissingName();

		if (!missingName.isEmpty()) {
			final TextRenderer textRenderer = this.getTextRenderer();
			Quaternionf entityRotation = RotationAxis.POSITIVE_Y.rotationDegrees(180 - state.facing.getHorizontalQuarterTurns() * 90);
			float x = -textRenderer.getWidth(missingName)/2;
			float y = -state.variant.height();

			matrices.push();
			matrices.scale(-0.025f, -0.025f, -0.025f);
			matrices.multiply(entityRotation);
			matrices.translate(0, -4, 2.5f);
			textRenderer.drawWithOutline(Text.literal(missingName).asOrderedText(), x, y, 0xffff88ff, 0xff000000, matrices.peek().getPositionMatrix(), vertex, light);
			matrices.pop();
		}
	}

	@ModifyArg( method="render", index=5, at=@At(value="INVOKE", target="net/minecraft/client/render/entity/PaintingEntityRenderer.renderPainting (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;[IIILnet/minecraft/client/texture/Sprite;Lnet/minecraft/client/texture/Sprite;)V") )
	private Sprite renderMissingno(Sprite paintingSprite, @Local(argsOnly=true) PaintingEntityRenderState state) {
		if (!IPaintingStateDuck.Of(state).nokebab$GetMissingName().isEmpty()) {
			ISpriteAtlasHolderMixin atlas = (ISpriteAtlasHolderMixin)MinecraftClient.getInstance().getPaintingManager();
			paintingSprite = atlas.GetSpriteFromID(MISSINGNO_ID);
		}
		return paintingSprite;
	}

}
