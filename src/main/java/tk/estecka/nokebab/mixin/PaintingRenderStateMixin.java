package tk.estecka.nokebab.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import net.minecraft.client.render.entity.state.PaintingEntityRenderState;
import tk.estecka.nokebab.duck.IPaintingStateDuck;

@Unique
@Mixin(PaintingEntityRenderState.class)
public class PaintingRenderStateMixin
implements IPaintingStateDuck
{
	private String missingName = "";

	@Override public String nokebab$GetMissingName(){ return missingName; }
	@Override public void nokebab$SetMissingName(String id){ this.missingName=id; }
}
