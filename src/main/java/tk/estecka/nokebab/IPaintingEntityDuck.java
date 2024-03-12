package tk.estecka.nokebab;

import org.jetbrains.annotations.NotNull;
import com.ibm.icu.impl.Pair;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.entry.RegistryEntry;

public interface IPaintingEntityDuck 
{
	static public IPaintingEntityDuck	Of(PaintingEntity painting){
		return (IPaintingEntityDuck)painting;
	}

	public @NotNull String nokebab$GetMissingName();
	public void nokebab$SetMissingName(@NotNull String id);

	public @NotNull String nokebab$GetIntendedVariant();

	public Pair<String, RegistryEntry<PaintingVariant>> nokebab$GetState();
	public void nokebab$SetState(Pair<String,RegistryEntry<PaintingVariant>> state);
	public void nokebab$SetState(String missingName, RegistryEntry<PaintingVariant> activeVariant);
}
