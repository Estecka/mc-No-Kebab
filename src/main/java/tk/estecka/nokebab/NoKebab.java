package tk.estecka.nokebab;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoKebab implements ModInitializer {
	static public final Logger LOGGER = LoggerFactory.getLogger("no-kebab");

	static public final Identifier MISSINGNO_ID = new Identifier("nokebab", "missingno");
	static public final RegistryKey<PaintingVariant> MISSINGNO_KEY = RegistryKey.of(RegistryKeys.PAINTING_VARIANT, MISSINGNO_ID);
	static public final PaintingVariant MISSINGNO = Registry.register(Registries.PAINTING_VARIANT, MISSINGNO_KEY, new PaintingVariant(16, 16));
	static public final RegistryEntry<PaintingVariant> MISSINGNO_ENTRY = Registries.PAINTING_VARIANT.getEntry(MISSINGNO);


	@Override
	public void onInitialize() {
	}
}
