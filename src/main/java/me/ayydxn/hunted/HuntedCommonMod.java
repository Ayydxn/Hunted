package me.ayydxn.hunted;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class HuntedCommonMod implements ModInitializer
{
    public static final Logger LOGGER = (Logger) LogManager.getLogger("Hunted");

    @Override
    public void onInitialize()
    {
        LOGGER.info("Initializing Hunted... (Version: {})", FabricLoader.getInstance().getModContainer("hunted").orElseThrow(NullPointerException::new)
                .getMetadata().getVersion().getFriendlyString());
    }
}
