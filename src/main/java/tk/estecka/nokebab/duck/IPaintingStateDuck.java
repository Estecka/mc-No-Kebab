package tk.estecka.nokebab.duck;

import net.minecraft.client.render.entity.state.PaintingEntityRenderState;

public interface IPaintingStateDuck
{
	static public IPaintingStateDuck Of(PaintingEntityRenderState state){
		return (IPaintingStateDuck)state;
	}

	public String nokebab$GetMissingName();
	public void nokebab$SetMissingName(String value);
}
