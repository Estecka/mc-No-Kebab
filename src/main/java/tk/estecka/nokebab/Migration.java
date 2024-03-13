package tk.estecka.nokebab;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.painting.PaintingEntity;

public abstract class Migration
implements Function<PaintingEntity, PaintingState>
{
	static public record Result(int success, int error) {
		public int total(){ return success + error; }
	}

	/**
	 * @return Null if the migration does not match the painting. Otherwise, the
	 * state the painting should be moved to.
	 */
	@Override
	public abstract @Nullable PaintingState apply(PaintingEntity painting);

	static public boolean TryMigrate(PaintingEntity painting, PaintingState state){
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
			PaintingState state = this.apply(painting);
			if (state == null)
				continue;

			String src = IPaintingEntityDuck.Of(painting).nokebab$GetState().GetIntendedName();
			String dst = state.GetIntendedName();

			if (TryMigrate(painting, state)){
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
		private final PaintingState destination;

		public Literal(String source, String destination){
			this.source = source;
			this.destination = PaintingState.ForName(destination);
		}

		public boolean Matches(PaintingEntity painting){
			return source.equals(IPaintingEntityDuck.Of(painting).nokebab$GetState().GetIntendedName());
		}

		@Override
		public @Nullable PaintingState apply(PaintingEntity painting){
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
		public @Nullable PaintingState apply(PaintingEntity painting){
			String src = IPaintingEntityDuck.Of(painting).nokebab$GetState().GetIntendedName();
			Matcher match = this.source.matcher(src);
			if (!match.matches())
				return null;
			else
				return PaintingState.ForName(match.replaceAll(this.destination));
		}
	}
}
