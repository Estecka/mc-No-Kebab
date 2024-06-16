package tk.estecka.nokebab;

import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class RegistryUtil
{
	/**
	 * Seeks the smallest existing painting, to serve as fallback.
	 * Returns immediately upon finding a 1x1 painting.
	 */
	static public RegistryEntry<PaintingVariant> GetFallback(Registry<PaintingVariant> registry){
		RegistryEntry<PaintingVariant> result = null;
		int bestFit = Integer.MAX_VALUE;

		for (var entry : registry.getIndexedEntries()){
			int surface = entry.value().getArea();
			if (surface == 1)
				return entry;

			if (surface < bestFit){
				result = entry;
				bestFit = surface;
			}
		}

		if (result == null)
			throw new RuntimeException("Empty painting registry");

		return result;
	}

	static public Identifier GetFallbackId(Registry<PaintingVariant> registry){
		return GetFallback(registry).getKey().get().getValue();
	}

	static public Registry<PaintingVariant> PaintingsOf(World world){
		return world.getRegistryManager().get(RegistryKeys.PAINTING_VARIANT);
	}
}
