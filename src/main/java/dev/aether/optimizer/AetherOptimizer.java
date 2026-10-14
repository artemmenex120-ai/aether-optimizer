package dev.aether.optimizer;

import net.fabricmc.api.ModInitializer;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

public final class AetherOptimizer implements ModInitializer {
    public static final String MOD_ID = "aether_optimizer";
    private static final Logger LOGGER = System.getLogger("Aether Optimizer");
    private static OptimizerConfig config;

    @Override
    public void onInitialize() {
        config = OptimizerConfig.load();
        LOGGER.log(Level.INFO,
                "Aether Optimizer {0}: enabled={1}, profile={2}, distance={3}, interval={4}, playerCheckTicks={5}, maxSkips={6}, minAgeTicks={7}",
                "0.33.0-prototype", config.enabled(), config.profile(), config.distance(),
                config.interval(), config.playerCheckTicks(), config.maxConsecutiveSkips(),
                config.minimumAgeTicks());
        LOGGER.log(Level.WARNING,
                "Experimental build: confirm Mixin application and benchmark in a disposable world before use.");
        if (config.diagnostics()) {
            LOGGER.log(Level.INFO, "Diagnostics enabled; no performance gain is assumed without a repeatable benchmark.");
        }
    }

    public static OptimizerConfig config() {
        if (config == null) config = OptimizerConfig.load();
        return config;
    }
}
