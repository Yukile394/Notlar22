package dev.silvera.notlar22;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Notlar22 implements ModInitializer {

    public static final String MOD_ID = "notlar22";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_SERVER_SINGLE_PRESS = "single_press_on_servers";

    private static volatile boolean enabled = true;
    private static volatile boolean serverSinglePress = false;

    @Override
    public void onInitialize() {
        load();
        LOGGER.info("Notlar22 yüklendi.");
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isServerSinglePress() {
        return serverSinglePress;
    }

    public static boolean toggle() {
        enabled = !enabled;
        save();
        return enabled;
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".properties");
    }

    private static void load() {
        Path path = configPath();
        if (!Files.isRegularFile(path)) {
            save();
            return;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
            enabled = Boolean.parseBoolean(props.getProperty(KEY_ENABLED, "true"));
            serverSinglePress = Boolean.parseBoolean(props.getProperty(KEY_SERVER_SINGLE_PRESS, "false"));
        } catch (IOException e) {
            LOGGER.warn("Config okunamadı: {}", e.getMessage());
        }
    }

    private static void save() {
        Properties props = new Properties();
        props.setProperty(KEY_ENABLED, Boolean.toString(enabled));
        props.setProperty(KEY_SERVER_SINGLE_PRESS, Boolean.toString(serverSinglePress));
        try (OutputStream out = Files.newOutputStream(configPath())) {
            props.store(out, "Notlar22");
        } catch (IOException e) {
            LOGGER.warn("Config yazılamadı: {}", e.getMessage());
        }
    }
}
