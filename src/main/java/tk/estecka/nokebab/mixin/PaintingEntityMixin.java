package tk.estecka.nokebab.mixin;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import tk.estecka.nokebab.NoKebab;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PaintingEntity.class)
public abstract class PaintingEntityMixin 
{
	@Shadow
	static private final RegistryKey<PaintingVariant> DEFAULT_VARIANT = NoKebab.MISSINGNO_KEY;

	public final PaintingEntity	painting = (PaintingEntity)(Object)this;
	private final TrackedData<String> rawVariant = initRawVariant();

	TrackedData<String>	initRawVariant(){
		TrackedData<String> data = DataTracker.registerData(PaintingEntity.class, TrackedDataHandlerRegistry.STRING);
		((PaintingEntity)(Object)this).getDataTracker().startTracking(data, DEFAULT_VARIANT.getValue().toString());
		return data;
	}

	/**
	 * Expected to be nullable if installed client-side only.
	 */
	@Nullable
	public String	GetRawVariant(){
		return painting.getDataTracker().get(rawVariant);
	}

	private void	SetRawVariant(String value){
		painting.getDataTracker().set(rawVariant, value);
	}

	@Inject( method="setVariant", at=@At("HEAD") )
	void	trackKnownVariant(RegistryEntry<PaintingVariant> entry, CallbackInfo info){
		var key = entry.getKey();
		if (key.isEmpty())
			NoKebab.LOGGER.error("Invalid change to the active variant: of \"{}\"(ID kept) {} {}", this.GetRawVariant(), painting.getPos(), painting.getUuid());
		else if (!key.get().equals(NoKebab.MISSINGNO_KEY))
			this.SetRawVariant(key.get().getValue().toString());
	}

	@Redirect( method="writeCustomDataToNbt", at=@At(value="INVOKE", target="net/minecraft/entity/decoration/painting/PaintingEntity.writeVariantToNbt (Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/registry/entry/RegistryEntry;)V") )
	private void	WriteMissingVariantToNBT(NbtCompound nbt, RegistryEntry<PaintingVariant> variant) {
		final String rawVariant = this.GetRawVariant();
		boolean isMissingno = variant.matchesKey(NoKebab.MISSINGNO_KEY);

		if (rawVariant == null)
		{
			NoKebab.LOGGER.error("Painting has no last-known variant: {} {}", painting.getPos(), painting.getUuid());
			if (isMissingno || variant.getKey().isEmpty())
				NoKebab.LOGGER.error("Painting also has no valid active variant.");
			PaintingEntity.writeVariantToNbt(nbt, variant);
		}
		else if (!isMissingno && !variant.matchesId(new Identifier(rawVariant)))
		{
			NoKebab.LOGGER.error("Mismatch between last-known variant and active variant: {} {}", painting.getPos(), painting.getUuid());
			NoKebab.LOGGER.error("Known: \"{}\" Active: {}", rawVariant, variant.getKey());
			PaintingEntity.writeVariantToNbt(nbt, variant);
		}
		else
			nbt.putString(PaintingEntity.VARIANT_NBT_KEY, rawVariant);
	}

	@Redirect( method="readCustomDataFromNbt", at=@At(value="INVOKE", target="net/minecraft/entity/decoration/painting/PaintingEntity.readVariantFromNbt (Lnet/minecraft/nbt/NbtCompound;)Ljava/util/Optional;") )
	private Optional<? extends RegistryEntry<PaintingVariant>>	preserveMissingVariantFromNBT(NbtCompound nbt){
		String nbtString = nbt.getString(PaintingEntity.VARIANT_NBT_KEY);
		Identifier nbtId = Identifier.tryParse(nbtString);

		if (nbtString.isEmpty())
			return Optional.empty();

		this.SetRawVariant(nbtString);
		if (nbtId == null) {
			NoKebab.LOGGER.warn("Painting with malformed painting ID: \"{}\" {} {}", nbtString, painting.getPos(), painting.getUuid());
			return Optional.empty();
		}
		else {
			var entry = Registries.PAINTING_VARIANT.getEntry(RegistryKey.of(RegistryKeys.PAINTING_VARIANT, nbtId));
			if (entry.isEmpty())
				NoKebab.LOGGER.warn("Painting with missing ID: \"{}\" {} {}", nbtString, painting.getPos(), painting.getUuid());
			return entry;
		}

	}
}
