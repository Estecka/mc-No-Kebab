package tk.estecka.nokebab;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoKebab
implements ModInitializer
{
	static public final Logger LOGGER = LoggerFactory.getLogger("no-kebab");
	static private boolean customTrackers = true;

	static {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
			customTrackers = false;
		else
			customTrackers = true;

		try {
			new ConfigIO("nokebab.properties").GetOrCreate(new ConfigProperties());
		}
		catch (IOException e){
			LOGGER.error(e.toString());
		}

	}

	static public boolean areCustomTrackersEnabled(){ return customTrackers; }

	@Override
	public void onInitialize() {
		Commands.Register();
	}

	static class ConfigProperties
	extends ConfigIO.AFixedCoded
	{
		@Override
		public Map<String, ConfigIO.Property<?>> GetProperties(){
			return new HashMap<>(){{
				this.put("customTracker", ConfigIO.Property.Boolean( ()->customTrackers, v->customTrackers=v ));
			}};
		}
	}
}
