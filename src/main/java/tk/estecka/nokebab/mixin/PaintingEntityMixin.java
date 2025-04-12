package tk.estecka.nokebab.mixin;

import net.minecraft.entity.Variants;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import tk.estecka.nokebab.NoKebab;
import tk.estecka.nokebab.PaintingState;
import tk.estecka.nokebab.duck.IPaintingEntityDuck;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import static tk.estecka.nokebab.RegistryUtil.*;

@Unique
@Mixin(PaintingEntity.class)
public abstract class PaintingEntityMixin
extends AbstractDecorationEntity
implements IPaintingEntityDuck
{
	static private final String VARIANT_NBT_KEY = Variants.VARIANT_NBT_KEY;
	static private final TrackedData<String> MISSING_TRACKER;

	static {
		if (NoKebab.areCustomTrackersEnabled())
			MISSING_TRACKER = DataTracker.registerData(PaintingEntity.class, TrackedDataHandlerRegistry.STRING);
		else
			MISSING_TRACKER = null;
	}


	private @NotNull String MISSING_VARIANT = "";

	private PaintingEntityMixin(){ super(null, null); }
	@Shadow public abstract RegistryEntry<PaintingVariant> getVariant();
	@Shadow private void setVariant(RegistryEntry<PaintingVariant> variant){ throw new AssertionError(); }


/******************************************************************************/
/* # Interface                                                                */
/******************************************************************************/

	@Override
	public @NotNull String	nokebab$GetMissingName(){
		return NoKebab.areCustomTrackersEnabled() ? this.dataTracker.get(MISSING_TRACKER) : MISSING_VARIANT;
	}
	@Override
	public void	nokebab$SetMissingName(@NotNull String value){
		if (NoKebab.areCustomTrackersEnabled())
			this.dataTracker.set(MISSING_TRACKER, value);
		else
			this.MISSING_VARIANT = value;
	}

	@Override
	public PaintingState nokebab$GetState(){
		return new PaintingState(this.nokebab$GetMissingName(), this.getVariant());
	}
	@Override
	public void nokebab$SetState(PaintingState state){
		this.setVariant(state.activeVariant());
		this.nokebab$SetMissingName(state.missingName());
	}


/******************************************************************************/
/* # Lifecycle                                                                */
/******************************************************************************/

	@Inject( method="initDataTracker", at=@At("HEAD") )
	private void	InitMissingTracker(DataTracker.Builder builder, CallbackInfo info){
		if (NoKebab.areCustomTrackersEnabled())
			builder.add(MISSING_TRACKER, "");
	}

	@Inject( method="setVariant", at=@At("HEAD") )
	private void	DiscardMissingno(RegistryEntry<PaintingVariant> entry, CallbackInfo info){
		final String missingName = this.nokebab$GetMissingName();
		if (!missingName.isEmpty()){
			NoKebab.LOGGER.warn("Missingno painting had its variant changed from \"{}\" to {}", missingName, entry.getKey());
			this.nokebab$SetMissingName("");
		}
	}


/******************************************************************************/
/* # Serialization                                                            */
/******************************************************************************/

	@WrapOperation( method="writeCustomDataToNbt", at=@At(value="INVOKE", target="net/minecraft/entity/Variants.writeVariantToNbt(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/registry/entry/RegistryEntry;)V") )
	private void	WriteMissingVariantToNBT(NbtCompound nbt, RegistryEntry<PaintingVariant> variant, Operation<Void> original) {
		final PaintingState state = this.nokebab$GetState();

		if (!state.IsMissingno())
			original.call(nbt, variant);
		else {
			final var defaultId = GetFallbackId(PaintingsOf(this.getWorld()));
			if (!state.activeVariant().matchesId(defaultId)){
				NoKebab.LOGGER.error("Painting is Missingno, but active variant is not the default one: {} {} ", this.getPos(), this.getUuid());
				NoKebab.LOGGER.error("Known: \"{}\" Active: {} Expected: {}", state.missingName(), state.activeVariant().getKey(), defaultId);
			}

			nbt.putString(VARIANT_NBT_KEY, state.missingName());
		}
	}

	@Inject( method="readCustomDataFromNbt", at=@At("TAIL") )
	private void	preserveMissingVariantFromNBT(NbtCompound nbt, CallbackInfo info){
		String variantName = nbt.getString(VARIANT_NBT_KEY, "");
		if (variantName.isEmpty())
			return;

		final Registry<PaintingVariant> registry = PaintingsOf(this.getWorld());
		Identifier nbtId = Identifier.tryParse(variantName);
		boolean valid = (nbtId != null);
		boolean exists = valid && registry.containsId(nbtId);
		if (!valid)
			NoKebab.LOGGER.warn("Painting with malformed ID: \"{}\" {} {}", variantName, this.getPos(), this.getUuid());
		else if (!exists)
			NoKebab.LOGGER.warn("Painting with missing ID: \"{}\" {} {}", variantName, this.getPos(), this.getUuid());

		if (!valid || !exists)
			this.nokebab$SetState(PaintingState.ForName(variantName, registry));
	}
}
