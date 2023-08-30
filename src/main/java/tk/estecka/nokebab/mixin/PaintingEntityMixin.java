package tk.estecka.nokebab.mixin;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import tk.estecka.nokebab.IPaintingEntityDuck;
import tk.estecka.nokebab.NoKebab;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PaintingEntity.class)
public abstract class PaintingEntityMixin 
implements IPaintingEntityDuck
{
	static private final Identifier	DEFAULT_ID = Registries.PAINTING_VARIANT.getDefaultId();
	static private final TrackedData<String> RAW_VARIANT = DataTracker.registerData(PaintingEntity.class, TrackedDataHandlerRegistry.STRING);

	public final PaintingEntity	painting = (PaintingEntity)(Object)this;

	@Inject( method="initDataTracker", at=@At("HEAD") )
	void	initRawVariant(CallbackInfo info){
		// Called from the constructor for `Entity`; `this.painting` is yet uninitialized.
		((PaintingEntity)(Object)this).getDataTracker().startTracking(RAW_VARIANT, "");
	}

	@NotNull
	public String	GetRawVariant(){
		return painting.getDataTracker().get(RAW_VARIANT);
	}

	private void	SetRawVariant(@NotNull String value){
		painting.getDataTracker().set(RAW_VARIANT, value);
	}

	@Inject( method="setVariant", at=@At("HEAD") )
	void	DiscardMissingno(RegistryEntry<PaintingVariant> entry, CallbackInfo info){
		final String rawVariant = this.GetRawVariant();
		if (!rawVariant.isEmpty()){
			NoKebab.LOGGER.warn("Missingno painting had its variant changed from \"{}\" to {}", rawVariant, entry.getKey());
			this.SetRawVariant("");
		}
	}

	@Redirect( method="writeCustomDataToNbt", at=@At(value="INVOKE", target="net/minecraft/entity/decoration/painting/PaintingEntity.writeVariantToNbt (Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/registry/entry/RegistryEntry;)V") )
	private void	WriteMissingVariantToNBT(NbtCompound nbt, RegistryEntry<PaintingVariant> entry) {
		final String rawVariant = this.GetRawVariant();

		if (rawVariant.isEmpty())
			PaintingEntity.writeVariantToNbt(nbt, entry);
		else {
			nbt.putString(PaintingEntity.VARIANT_NBT_KEY, rawVariant);
			if (!entry.matchesId(DEFAULT_ID)){
				NoKebab.LOGGER.error("Painting is Missingno, but active variant is not the default one: {} {} ", painting.getPos(), painting.getUuid());
				NoKebab.LOGGER.error("Known: \"{}\" Active: {}", rawVariant, entry.getKey());
			}
		}
	}

	@Inject( method="readCustomDataFromNbt", at=@At("TAIL") )
	private void	preserveMissingVariantFromNBT(NbtCompound nbt, CallbackInfo info){
		String nbtString = nbt.getString(PaintingEntity.VARIANT_NBT_KEY);
		if (nbtString.isEmpty())
			return;

		Identifier nbtId = Identifier.tryParse(nbtString);
		boolean valid  = nbtId!=null;
		boolean exists = valid && Registries.PAINTING_VARIANT.containsId(nbtId);
		if (!valid)
			NoKebab.LOGGER.warn("Painting with malformed ID: \"{}\" {} {}", nbtString, painting.getPos(), painting.getUuid());
		else if (!exists)
			NoKebab.LOGGER.warn("Painting with missing ID: \"{}\" {} {}", nbtString, painting.getPos(), painting.getUuid());

		if (!valid || !exists)
			this.SetRawVariant(nbtString);
	}
}
