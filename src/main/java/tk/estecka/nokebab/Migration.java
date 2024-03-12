package tk.estecka.nokebab;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
		String raw = IPaintingEntityDuck.Of(painting).nokebab$GetMissingVariant();
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
	static public int Literal(String src, String dst, ServerWorld world){
		int r = 0;
		var dstEntry = GetEntry(dst);

		if (dstEntry == null)
			dstEntry = GetEntry(Registries.PAINTING_VARIANT.getDefaultId());
		else
			dst = "";

		for (Entity e : world.iterateEntities())
		if  (e instanceof PaintingEntity painting && Matches(painting, src))
		{
			++r;
			painting.setVariant(dstEntry);
			IPaintingEntityDuck.Of(painting).nokebab$SetMissingVariant(dst);
		}

		return r;
	}

	
	/***
	 * @param src The variant id to migrate
	 * @param dst The variant id it will be replaced with.
	 * @return The amount of paintings that were succesfully migrated.
	 */
	static public int Regex(Pattern regex, String substitution, ServerWorld world){
		int r = 0;

		for (Entity e : world.iterateEntities())
		if  (e instanceof PaintingEntity painting)
		{
			String src = IPaintingEntityDuck.Of(painting).nokebab$GetIntendedVariant();
			Matcher match = regex.matcher(src);
			if (match.matches())
			{
				++r;
				String dst = match.replaceAll(substitution);
				NoKebab.LOGGER.info("Migrated painting from \"{}\" to \"{}\"", src, dst);

				var dstEntry = GetEntry(dst);
				if (dstEntry == null)
					dstEntry = GetEntry(Registries.PAINTING_VARIANT.getDefaultId());
				else
					dst = "";

				painting.setVariant(dstEntry);
				IPaintingEntityDuck.Of(painting).nokebab$SetMissingVariant(dst);
			}
		}

		return r;
	}
}
