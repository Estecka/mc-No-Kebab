package tk.estecka.nokebab;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

/**
 * Represents  the in-memory state  of  a PaintingEntity  based on  its intended
 * variant, and whether it's a missingno.
 * 
 * If the intended variant  is valid, missingName will be empty (not null!), and
 * activeVariant represents the intended variant. missingName can never be null,
 * because it is not a valid value for the DataTracker to hold.
 * 
 * If the intended variant  is missing, missingName will  represent the intended
 * variant, and  activeVariant  will  serve as  the  placeholder  which will  be
 * visible to vanilla clients. By convention, the placeholder variant  should be
 * set to {@link RegistryUtil#GetFallback}; nothing bad will happen if it's not,
 * but it being otherwise can indicate that something unexpected occured.
 */
public record PaintingState (@NotNull String missingName, @NotNull RegistryEntry<PaintingVariant> activeVariant)
{
	static public PaintingState ForName(String variantName, Registry<PaintingVariant> registry){
		return FromEntry(variantName, GetEntry(variantName, registry), registry);
	}
	static public PaintingState ForId(Identifier variantId, Registry<PaintingVariant> registry){
		return FromEntry(variantId.toString(), GetEntry(variantId, registry), registry);
	}
	static private PaintingState FromEntry(String name, @Nullable RegistryEntry<PaintingVariant> entry, Registry<PaintingVariant> registry){
		if (entry != null)
			return new PaintingState("", entry);
		else
			return new PaintingState(name, RegistryUtil.GetFallback(registry));
	}

	static public @Nullable RegistryEntry<PaintingVariant> GetEntry(String name, Registry<PaintingVariant> registry){
		Identifier id = Identifier.tryParse(name);
		if (id == null)
			return null;
		else
			return GetEntry(id, registry);
	}
	static public @Nullable RegistryEntry<PaintingVariant> GetEntry(Identifier id, Registry<PaintingVariant> registry){
		var variant = registry.getOptionalValue(id);
		if (variant.isEmpty())
			return null;
		else
			return registry.getEntry(variant.get());
	}

	public boolean IsMissingno(){
		return !this.missingName.isEmpty();
	}

	public String GetIntendedName(){
		if (this.IsMissingno())
			return missingName;
		else
			return activeVariant.getKey().get().getValue().toString();
	}
}
