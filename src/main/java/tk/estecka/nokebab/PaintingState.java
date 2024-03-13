package tk.estecka.nokebab;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

/**
 * Represents the in-memory state of a PaintingEntity based on its variant. This
 * state varies greatly depending on whether the painting is a Missingno or not.
 * 
 * If the painting variant  is valid, missingName will be empty (not null!), and
 * activeVariant represents the intended variant. missingName can never be null,
 * because it is not a valid value for the DataTracker to hold.
 * 
 * If the painting variant  is missing, missingName will  represent the intended
 * variant, and  activeVariant  will  serve as  the  placeholder  which will  be
 * visible to vanilla clients. The  placeholder variant  should be set to kebab;
 * nothing dire  will  happen if it's not, but it being  otherwise  can indicate
 * that something unexpected occured.
 */
public record PaintingState (@NotNull String missingName, @NotNull RegistryEntry<PaintingVariant> activeVariant)
{
	static public PaintingState ForName(String variantName){
		return FromEntry(variantName, GetEntry(variantName));
	}
	static public PaintingState ForId(Identifier variantId){
		return FromEntry(variantId.toString(), GetEntry(variantId));
	}
	static private PaintingState FromEntry(String name, @Nullable RegistryEntry<PaintingVariant> entry){
		if (entry != null)
			return new PaintingState("", entry);
		else
			return new PaintingState(name, GetEntry(Registries.PAINTING_VARIANT.getDefaultId()));
	}

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
