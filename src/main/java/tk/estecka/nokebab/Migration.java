package tk.estecka.nokebab;

import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.ibm.icu.impl.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public abstract class Migration
implements Function<PaintingEntity, Pair<String, RegistryEntry<PaintingVariant>>>
{
	static public record Result(int success, int error) {
		public int total(){ return success + error; }
	}

	/**
	 * @return Null if the migration does not match the painting. Otherwise, the
	 * state the painting should be moved to.
	 *
	 * The string should be empty if the target variant is valid. The registry 
	 * entry should point to minecraft:kebab if the target variant is invalid.
	 */
	@Override
	public abstract @Nullable Pair<@NotNull String, RegistryEntry<PaintingVariant>> apply(PaintingEntity painting);


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

	static public Pair<String, RegistryEntry<PaintingVariant>> GetState(String name){
		var entry = GetEntry(name);
		if (entry == null)
			entry = GetEntry(Registries.PAINTING_VARIANT.getDefaultId());
		else
			name = "";

		return Pair.of(name, entry);
	}

	static public boolean TryMigrate(PaintingEntity painting, Pair<String, RegistryEntry<PaintingVariant>> state){
		IPaintingEntityDuck duck = IPaintingEntityDuck.Of(painting);
		
		var original = duck.nokebab$GetState();
		duck.nokebab$SetState(state);

		if (painting.canStayAttached())
			return true;
		else {
			duck.nokebab$SetState(original);
			return false;
		}
	}

	public Result Run(Iterable<Entity> entities){
		int ok = 0;
		int err = 0;

		for (Entity e : entities)
		if  (e instanceof PaintingEntity painting)
		{
			var state = this.apply(painting);
			if (state == null)
				continue;

			String src = IPaintingEntityDuck.Of(painting).nokebab$GetIntendedVariant();

			if (TryMigrate(painting, state)){
				String dst = IPaintingEntityDuck.Of(painting).nokebab$GetIntendedVariant();
				NoKebab.LOGGER.info("Migrated painting from \"{}\" to \"{}\"", src, dst);
				++ok;
			}
			else{
				NoKebab.LOGGER.warn("Painting could not be migrated to due to size constraint: {} {}", src, painting.getUuid(), painting.getPos());
				++err;
			}
		}

		return new Result(ok, err);
	}

	static public class Literal
	extends Migration
	{
		private final String source;
		private final Pair<String, RegistryEntry<PaintingVariant>> destination;

		public Literal(String source, String destination){
			this.source = source;
			this.destination = GetState(destination);
		}

		public boolean Matches(PaintingEntity painting){
			String raw = IPaintingEntityDuck.Of(painting).nokebab$GetMissingName();
			if (!raw.isEmpty())
				return raw.equals(source);
			else {
				Identifier id = painting.getVariant().getKey().get().getValue();
				return Objects.equals(id, Identifier.tryParse(source));
			}
		}

		@Override
		public @Nullable Pair<@NotNull String, RegistryEntry<PaintingVariant>> apply(PaintingEntity painting){
			return this.Matches(painting) ? this.destination : null;
		}
	}

	static public class Regex
	extends Migration
	{
		private final Pattern source;
		private final String destination;

		public Regex(Pattern source, String destination){
			this.source = source;
			this.destination = destination;
		}

		@Override
		public @Nullable Pair<@NotNull String, RegistryEntry<PaintingVariant>> apply(PaintingEntity painting){
			String src = IPaintingEntityDuck.Of(painting).nokebab$GetIntendedVariant();
			Matcher match = this.source.matcher(src);
			if (!match.matches())
				return null;
			else
				return GetState(match.replaceAll(this.destination));
		}
	}
}
