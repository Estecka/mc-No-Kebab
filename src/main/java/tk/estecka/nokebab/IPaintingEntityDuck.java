package tk.estecka.nokebab;

import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.Entity;

public interface IPaintingEntityDuck 
{
	static public IPaintingEntityDuck	Of(PaintingEntity painting){
		return (IPaintingEntityDuck)painting;
	}

	static public IPaintingEntityDuck	Of(Entity entity){
		return (IPaintingEntityDuck)entity;
	}

	public String	GetRawVariant();
}
