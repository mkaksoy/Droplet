package github.mkaksoy;

import net.fabricmc.api.ClientModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Droplet implements ClientModInitializer {
	public static final String MOD_ID = "droplet";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LOGGER.info("Droplet is running!");
	}
}