package tablookup.java;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LookupConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("player-lookup.json");
	private static LookupConfig config = new LookupConfig();

	private LookupConfigManager() {
	}

	public static void load() {
		if (!Files.exists(CONFIG_PATH)) {
			save();
			return;
		}

		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			LookupConfig loaded = GSON.fromJson(reader, LookupConfig.class);
			config = loaded == null ? new LookupConfig() : loaded;
		} catch (IOException | JsonSyntaxException exception) {
			PlayerLookup.LOGGER.warn("Failed to read config {}, using defaults.", CONFIG_PATH, exception);
			config = new LookupConfig();
			save();
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException exception) {
			PlayerLookup.LOGGER.error("Failed to write config {}.", CONFIG_PATH, exception);
		}
	}

	public static LookupConfig getConfig() {
		return config;
	}

	public static LookupConfig copyConfig() {
		return config.copy();
	}

	public static void updateConfig(LookupConfig newConfig) {
		config = newConfig.copy();
	}
}
