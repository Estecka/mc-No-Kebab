package tk.estecka.nokebab;

import org.jetbrains.annotations.NotNull;
import net.minecraft.entity.decoration.painting.PaintingEntity;

public interface IPaintingEntityDuck 
{
	static public IPaintingEntityDuck Of(PaintingEntity painting){
		return (IPaintingEntityDuck)painting;
	}

	public @NotNull String nokebab$GetMissingName();
	public void nokebab$SetMissingName(@NotNull String id);

	public PaintingState nokebab$GetState();
	public void nokebab$SetState(PaintingState state);
}
