package tk.estecka.nokebab.mixin;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(PaintingEntity.class)
public abstract class PaintingEntityMixin
extends AbstractDecorationEntity
implements IPaintingEntityDuck
{
	static private final TrackedData<String> MISSING_VARIANT = DataTracker.registerData(PaintingEntity.class, TrackedDataHandlerRegistry.STRING);

	private PaintingEntityMixin(){ super(null, null); }

	@Override
	public @NotNull String	nokebab$GetMissingVariant(){
		return this.getDataTracker().get(MISSING_VARIANT);
	}

	@Override
	public void	nokebab$SetMissingVariant(@NotNull String value){
		this.getDataTracker().set(MISSING_VARIANT, value);
	}

	@Override
	public @NotNull String nokebab$GetIntendedVariant(){
		String missing = this.nokebab$GetMissingVariant();
		if (!missing.isEmpty())
			return missing;
		else
			return ((PaintingEntity)(Object)this).getVariant().getKey().get().getValue().toString();
	}

	@Inject( method="initDataTracker", at=@At("HEAD") )
	private void	InitMissingTracker(CallbackInfo info){
		this.getDataTracker().startTracking(MISSING_VARIANT, "");
	}

	@Inject( method="setVariant", at=@At("HEAD") )
	private void	DiscardMissingno(RegistryEntry<PaintingVariant> entry, CallbackInfo info){
		final String missingName = this.nokebab$GetMissingVariant();
		if (!missingName.isEmpty()){
			NoKebab.LOGGER.warn("Missingno painting had its variant changed from \"{}\" to {}", missingName, entry.getKey());
			this.nokebab$SetMissingVariant("");
		}
	}

	@WrapOperation( method="writeCustomDataToNbt", at=@At(value="INVOKE", target="net/minecraft/entity/decoration/painting/PaintingEntity.writeVariantToNbt (Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/registry/entry/RegistryEntry;)V") )
	private void	WriteMissingVariantToNBT(NbtCompound nbt, RegistryEntry<PaintingVariant> entry, Operation<Void> original) {
		final String missingName = this.nokebab$GetMissingVariant();

		if (missingName.isEmpty())
			original.call(nbt, entry);
		else {
			nbt.putString(PaintingEntity.VARIANT_NBT_KEY, missingName);
			if (!entry.matchesId(Registries.PAINTING_VARIANT.getDefaultId())){
				NoKebab.LOGGER.error("Painting is Missingno, but active variant is not the default one: {} {} ", this.getPos(), this.getUuid());
				NoKebab.LOGGER.error("Known: \"{}\" Active: {}", missingName, entry.getKey());
			}
		}
	}

	@Inject( method="readCustomDataFromNbt", at=@At("TAIL") )
	private void	preserveMissingVariantFromNBT(NbtCompound nbt, CallbackInfo info){
		String nbtString = nbt.getString(PaintingEntity.VARIANT_NBT_KEY);
		if (nbtString.isEmpty())
			return;

		Identifier nbtId = Identifier.tryParse(nbtString);
		boolean valid = (nbtId != null);
		boolean exists = valid && Registries.PAINTING_VARIANT.containsId(nbtId);
		if (!valid)
			NoKebab.LOGGER.warn("Painting with malformed ID: \"{}\" {} {}", nbtString, this.getPos(), this.getUuid());
		else if (!exists)
			NoKebab.LOGGER.warn("Painting with missing ID: \"{}\" {} {}", nbtString, this.getPos(), this.getUuid());

		if (!valid || !exists)
			this.nokebab$SetMissingVariant(nbtString);
	}
}
