package github.mkaksoy;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Droplet implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("droplet");

	@Override
	public void onInitialize() {
		LOGGER.info("Droplet is running!");
	}
}