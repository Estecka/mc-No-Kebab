package tk.estecka.nokebab;

import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class RegistryUtil
{
	/**
	 * May not be the default Id depending on teh loaded paintings.
	 * @return
	 */
	@Deprecated
	static public Identifier GetDefaultId(){
		return Identifier.of("minecraft", "alban");
	}

	static public Registry<PaintingVariant> PaintingsOf(World world){
		return world.getRegistryManager().get(RegistryKeys.PAINTING_VARIANT);
	}
}
