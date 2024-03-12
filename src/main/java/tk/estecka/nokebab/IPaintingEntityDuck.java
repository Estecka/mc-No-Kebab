package tk.estecka.nokebab;

import org.jetbrains.annotations.NotNull;
import net.minecraft.entity.decoration.painting.PaintingEntity;

public interface IPaintingEntityDuck 
{
	static public IPaintingEntityDuck	Of(PaintingEntity painting){
		return (IPaintingEntityDuck)painting;
	}

	public @NotNull String nokebab$GetMissingVariant();
	public void nokebab$SetMissingVariant(@NotNull String id);

	public @NotNull String nokebab$GetIntendedVariant();
}
