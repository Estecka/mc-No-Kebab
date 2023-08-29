package tk.estecka.nokebab.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.PaintingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.text.Text;
import tk.estecka.nokebab.IPaintingEntityDuck;
import tk.estecka.nokebab.NoKebab;

@Mixin(PaintingEntityRenderer.class)
public abstract class PaintingEntityRendererMixin 
extends EntityRenderer<PaintingEntity>
{
	private PaintingEntityRendererMixin() { super(null); throw new AssertionError(); }

	@Inject( method="render", at=@At("TAIL") )
	void	renderLabelWhenMissing(PaintingEntity painting, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info)
	{
		if (painting.getVariant() == NoKebab.MISSINGNO_ENTRY) {
			Text label = Text.literal(IPaintingEntityDuck.Of(painting).GetRawVariant());
			this.renderLabelIfPresent(painting, label, matrices, vertexConsumers, light);
		}
	}

}
