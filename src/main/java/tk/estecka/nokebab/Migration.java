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
	static public record Result(int success, int error) {}

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
		String raw = IPaintingEntityDuck.Of(painting).nokebab$GetMissingName();
		if (!raw.isEmpty())
			return raw.equals(variant);
		else {
			Identifier id = painting.getVariant().getKey().get().getValue();
			return Objects.equals(id, Identifier.tryParse(variant));
		}
	}

	static public boolean TryMigrate(PaintingEntity painting, String missing, RegistryEntry<PaintingVariant> active){
		IPaintingEntityDuck duck = IPaintingEntityDuck.Of(painting);
		
		var original = duck.nokebab$GetState();
		duck.nokebab$SetState(missing, active);

		if (painting.canStayAttached())
			return true;
		else {
			duck.nokebab$SetState(original);
			return false;
		}
	}

	/***
	 * @param src The variant id to migrate
	 * @param dst The variant id it will be replaced with.
	 * @return The amount of paintings that were succesfully migrated.
	 */
	static public Result Literal(String src, String dst, ServerWorld world){
		int ok = 0;
		int err = 0;
		var dstEntry = GetEntry(dst);

		if (dstEntry == null)
			dstEntry = GetEntry(Registries.PAINTING_VARIANT.getDefaultId());
		else
			dst = "";

		for (Entity e : world.iterateEntities())
		if  (e instanceof PaintingEntity painting && Matches(painting, src))
		{
			if (TryMigrate(painting, dst, dstEntry))
				++ok;
			else {
				NoKebab.LOGGER.warn("Painting could not be migrated to \"{}\" due to size constraint: {} {}", src, painting.getUuid(), painting.getPos());
				++err;
			}
		}

		return new Result(ok, err);
	}

	
	/***
	 * @param src The variant id to migrate
	 * @param dst The variant id it will be replaced with.
	 * @return The amount of paintings that were succesfully migrated.
	 */
	static public Result Regex(Pattern regex, String substitution, ServerWorld world){
		int ok = 0;
		int err = 0;

		for (Entity e : world.iterateEntities())
		if  (e instanceof PaintingEntity painting)
		{
			String src = IPaintingEntityDuck.Of(painting).nokebab$GetIntendedVariant();
			Matcher match = regex.matcher(src);
			if (match.matches())
			{
				String dst = match.replaceAll(substitution);
				
				String missingName = dst;
				var dstEntry = GetEntry(dst);
				if (dstEntry == null)
					dstEntry = GetEntry(Registries.PAINTING_VARIANT.getDefaultId());
				else
					missingName = "";

				if (TryMigrate(painting, dst, dstEntry)){
					NoKebab.LOGGER.info("Migrated painting from \"{}\" to \"{}\"", src, dst);
					++ok;
				}
				else{
					NoKebab.LOGGER.warn("Painting could not be migrated to \"{}\" due to size constraint: {} {}", src, painting.getUuid(), painting.getPos());
					++err;
				}
			}
		}

		return new Result(ok, err);
	}
}
