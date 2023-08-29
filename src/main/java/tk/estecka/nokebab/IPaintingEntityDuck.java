package tk.estecka.nokebab;

import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.Entity;

public interface IPaintingEntityDuck 
{
	static public IPaintingEntityDuck	Of(PaintingEntity e){
		return (IPaintingEntityDuck)e;
	}

	static public IPaintingEntityDuck	Of(Entity e){
		return (IPaintingEntityDuck)e;
	}

	public String	GetRawVariant();
}
