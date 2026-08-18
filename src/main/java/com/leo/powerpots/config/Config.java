package com.leo.powerpots.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.leo.powerpots.PowerPots;
import com.leo.powerpots.block.PotTier;
import net.neoforged.fml.loading.FMLPaths;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Config {
    public static Config INSTANCE;

    private static final int CONFIG_VERSION = 2;

    @Expose
    private int version = CONFIG_VERSION;

    @Expose
    private PotTier[] tiers = new PotTier[]{
            new PotTier(1, 1000, 10, 2, 1),
            new PotTier(2, 2000, 20, 4, 2),
            new PotTier(3, 4000, 40, 8, 4),
            new PotTier(4, 8000, 80, 16, 8),
            new PotTier(5, 16000, 160, 32, 16)
    };

    public static void initialize() {
        Config defaults = new Config();
        Config config = defaults;

        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .create();

        Path configPath = FMLPaths.CONFIGDIR.get().resolve("PowerPots.json");

        if (Files.exists(configPath)) {
            try (BufferedReader reader = Files.newBufferedReader(configPath)) {
                Config loaded = gson.fromJson(reader, Config.class);

                if (loaded == null) {
                    PowerPots.LOGGER.warn("Config was empty, using defaults.");
                    config = defaults;
                } else if (loaded.version != CONFIG_VERSION) {
                    PowerPots.LOGGER.info(
                            "Config version {} is outdated (current: {}), merging missing tiers.",
                            loaded.version, CONFIG_VERSION);

                    config = mergeTiers(loaded, defaults);
                    config.version = CONFIG_VERSION;
                } else {
                    config = mergeTiers(loaded, defaults);
                    config.version = CONFIG_VERSION;
                }

            } catch (IOException e) {
                PowerPots.LOGGER.error("Failed to read config, using defaults: {}", e.getMessage());
                config = defaults;
            }
        } else {
            try {
                Files.createDirectories(configPath.getParent());
                PowerPots.LOGGER.info("Config not found, creating with defaults.");
            } catch (IOException e) {
                PowerPots.LOGGER.error("Failed to create config directory: {}", e.getMessage());
            }
        }

        if (config == null) config = defaults;

        INSTANCE = config;
        INSTANCE.onConfigLoaded();

        try (BufferedWriter writer = Files.newBufferedWriter(configPath)) {
            gson.toJson(INSTANCE, writer);
            PowerPots.LOGGER.info("Configuration saved to: {}", configPath);
        } catch (IOException e) {
            PowerPots.LOGGER.error("Failed to save config: {}", e.getMessage());
        }
    }

    private static Config mergeTiers(Config existing, Config defaults) {
        List<PotTier> merged = new ArrayList<>(Arrays.asList(existing.tiers));

        for (PotTier defaultTier : defaults.tiers) {
            boolean found = false;
            for (PotTier existingTier : existing.tiers) {
                if (existingTier.index() == defaultTier.index()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                merged.add(defaultTier);
                PowerPots.LOGGER.info("Added missing tier {} from defaults.", defaultTier.index());
            }
        }

        merged.sort((a, b) -> Integer.compare(a.index(), b.index()));

        Config result = new Config();
        result.tiers = merged.toArray(new PotTier[0]);
        return result;
    }

    public List<PotTier> TIERS = new ArrayList<>();

    private void onConfigLoaded() {
        // Populate TIERS from tiers array
        TIERS.clear();
        if (tiers != null) {
            TIERS.addAll(Arrays.asList(tiers));
        }
    }
}
