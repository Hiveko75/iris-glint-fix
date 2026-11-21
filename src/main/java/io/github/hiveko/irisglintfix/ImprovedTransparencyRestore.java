package io.github.hiveko.irisglintfix;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import io.github.hiveko.irisglintfix.mixin.OptionInstanceAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Iris turns Minecraft's "Improved Transparency" off whenever a shader pack is enabled
 * (MixinDisableFabulousGraphics, on every renderer reload), but never turns it back on when the
 * shader pack is disabled again. This remembers that Iris switched it off and switches it back on
 * at the next renderer reload without shaders.
 *
 * <p>Only a change made while shaders are enabled is remembered (the player cannot change the option
 * then, so that is Iris). The flag is kept in config/iris-glint-fix.properties, so it also works
 * after restarting the game with shaders on. If Improved Transparency was off before shaders were
 * enabled, nothing is remembered and nothing is changed.
 */
public final class ImprovedTransparencyRestore {
    private static final Logger LOGGER = LoggerFactory.getLogger("Iris Glint Fix");
    private static final String KEY = "restoreImprovedTransparency";

    private static Boolean restore;          // loaded from the file on first use
    private static boolean irisLookedUp;
    private static Method getIrisConfig;
    private static Method areShadersEnabled;

    private ImprovedTransparencyRestore() {
    }

    /** Called before any OptionInstance.set. */
    public static void beforeSet(OptionInstance<?> option, Object newValue) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.options == null || option != mc.options.improvedTransparency()) {
            return;
        }
        if (Boolean.TRUE.equals(option.get()) && Boolean.FALSE.equals(newValue) && shadersEnabled()) {
            if (!isRestore()) {
                setRestore(true);
                LOGGER.info("Iris turned Improved Transparency off for shaders; it will be turned back on when shaders are disabled");
            }
        }
    }

    /** Called at the start of LevelExtractor.allChanged and onResourceManagerReload. */
    public static void beforeRendererReload() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.options == null || !isRestore() || shadersEnabled()) {
            return;
        }
        setRestore(false);
        OptionInstance<Boolean> option = mc.options.improvedTransparency();
        if (!option.get()) {
            // Set the value directly: the option's own listener would start another renderer reload,
            // and this one, which is just starting, already uses the new value.
            ((OptionInstanceAccessor) (Object) option).irisGlintFix$setValue(Boolean.TRUE);
            mc.options.save();
            LOGGER.info("Shaders disabled: Improved Transparency turned back on");
        }
    }

    private static boolean shadersEnabled() {
        if (!irisLookedUp) {
            irisLookedUp = true;
            try {
                Class<?> iris = Class.forName("net.irisshaders.iris.Iris");
                getIrisConfig = iris.getMethod("getIrisConfig");
                areShadersEnabled = getIrisConfig.getReturnType().getMethod("areShadersEnabled");
            } catch (ReflectiveOperationException | LinkageError e) {
                LOGGER.warn("Iris config not found; Improved Transparency will not be restored", e);
            }
        }
        if (areShadersEnabled == null) {
            return false;
        }
        try {
            Object config = getIrisConfig.invoke(null);
            return config != null && (Boolean) areShadersEnabled.invoke(config);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("iris-glint-fix.properties");
    }

    private static boolean isRestore() {
        if (restore == null) {
            restore = false;
            Path file = file();
            if (Files.isRegularFile(file)) {
                Properties props = new Properties();
                try (InputStream in = Files.newInputStream(file)) {
                    props.load(in);
                    restore = Boolean.parseBoolean(props.getProperty(KEY, "false"));
                } catch (IOException e) {
                    LOGGER.warn("Could not read {}", file, e);
                }
            }
        }
        return restore;
    }

    private static void setRestore(boolean value) {
        restore = value;
        Properties props = new Properties();
        props.setProperty(KEY, Boolean.toString(value));
        try (OutputStream out = Files.newOutputStream(file())) {
            props.store(out, "Iris Glint Fix: true while Improved Transparency is off only because shaders are on");
        } catch (IOException e) {
            LOGGER.warn("Could not write {}", file(), e);
        }
    }
}
