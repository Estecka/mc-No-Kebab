package tk.estecka.nokebab;

import net.minecraft.entity.decoration.painting.PaintingEntity;

public interface IPaintingEntityDuck 
{
	static public IPaintingEntityDuck	Of(PaintingEntity painting){
		return (IPaintingEntityDuck)painting;
	}

	public String	nokebab$GetRawVariant();
}
