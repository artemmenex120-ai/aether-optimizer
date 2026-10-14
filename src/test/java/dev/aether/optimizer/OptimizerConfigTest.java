package dev.aether.optimizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OptimizerConfigTest {
    private static final String[] KEYS = {
        "aether.optimizer.profile", "aether.optimizer.ai", "aether.optimizer.distance",
        "aether.optimizer.interval", "aether.optimizer.playerCheckTicks",
        "aether.optimizer.maxConsecutiveSkips", "aether.optimizer.minimumAgeTicks",
        "aether.optimizer.debug"
    };
    private final Map<String, String> original = new HashMap<>();

    @BeforeEach
    void isolateSystemProperties() {
        for (String key : KEYS) {
            String value = System.getProperty(key);
            if (value != null) original.put(key, value);
            System.clearProperty(key);
        }
    }

    @AfterEach
    void restoreSystemProperties() {
        for (String key : KEYS) {
            System.clearProperty(key);
            if (original.containsKey(key)) System.setProperty(key, original.get(key));
        }
    }

    @Test
    void balancedProfileHasDocumentedDefaults() {
        OptimizerConfig config = OptimizerConfig.load();
        assertTrue(config.enabled());
        assertEquals("balanced", config.profile());
        assertEquals(80, config.distance());
        assertEquals(2, config.interval());
        assertEquals(100, config.minimumAgeTicks());
        assertFalse(config.diagnostics());
    }

    @Test
    void conservativeProfileUsesConservativeDefaults() {
        System.setProperty("aether.optimizer.profile", " CONSERVATIVE ");
        OptimizerConfig config = OptimizerConfig.load();
        assertEquals("conservative", config.profile());
        assertEquals(64, config.distance());
        assertEquals(2, config.interval());
    }

    @Test
    void invalidProfileFallsBackToBalanced() {
        System.setProperty("aether.optimizer.profile", "turbo");
        assertEquals("balanced", OptimizerConfig.load().profile());
    }

    @Test
    void numericOverridesAreClampedAndMalformedValuesFallback() {
        System.setProperty("aether.optimizer.distance", "9999");
        System.setProperty("aether.optimizer.interval", "bad-value");
        System.setProperty("aether.optimizer.minimumAgeTicks", "-3");
        OptimizerConfig config = OptimizerConfig.load();
        assertEquals(192, config.distance());
        assertEquals(2, config.interval());
        assertEquals(0, config.minimumAgeTicks());
    }

    @Test
    void booleanPropertiesAcceptAliasesAndTrimWhitespace() {
        System.setProperty("aether.optimizer.ai", " OFF ");
        System.setProperty("aether.optimizer.debug", " YES ");
        OptimizerConfig config = OptimizerConfig.load();
        assertFalse(OptimizerConfig.load().enabled());
        assertTrue(config.diagnostics());
    }

    @Test
    void malformedBooleanUsesSafeFallback() {
        System.setProperty("aether.optimizer.ai", "perhaps");
        assertTrue(OptimizerConfig.load().enabled());
    }
}
