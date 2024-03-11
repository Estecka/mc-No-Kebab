package tk.estecka.nokebab;

import java.util.Objects;
import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class Migration 
{

	static public @Nullable RegistryEntry<PaintingVariant> GetEntry(String name){
		Identifier id = Identifier.tryParse(name);
		if (id == null)
			return null;
		else
			return GetEntry(id);
	}

	static public @Nullable RegistryEntry<PaintingVariant> GetEntry(Identifier id){
		var variant = Registries.PAINTING_VARIANT.getOrEmpty(id);
		if (variant.isEmpty())
			return null;
		else
			return Registries.PAINTING_VARIANT.getEntry(variant.get());
	}

	static public boolean Matches(PaintingEntity painting, String variant){
		String raw = IPaintingEntityDuck.Of(painting).nokebab$GetRawVariant();
		if (!raw.isEmpty())
			return raw.equals(variant);
		else {
			Identifier id = painting.getVariant().getKey().get().getValue();
			return Objects.equals(id, Identifier.tryParse(variant));
		}
	}

	/***
	 * @param src The variant id to migrate
	 * @param dst The variant id it will be replaced with.
	 * @return The amount of paintings that were succesfully migrated.
	 */
	static public int Migrate(String src, String dst, ServerWorld world){
		int r = 0;
		var variant = GetEntry(dst);

		if (variant == null)
			variant = GetEntry(Registries.PAINTING_VARIANT.getDefaultId());
		else
			dst = "";

		for (Entity e : world.iterateEntities())
		if  (e instanceof PaintingEntity painting && Matches(painting, src))
		{
			++r;
			painting.setVariant(variant);
			IPaintingEntityDuck.Of(painting).nokebab$SetRawVariant(dst);
		}

		return r;
	}
}
