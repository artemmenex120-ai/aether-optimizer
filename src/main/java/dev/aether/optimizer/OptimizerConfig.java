package dev.aether.optimizer;

/** Immutable, defensive configuration; system properties are read once at startup. */
public record OptimizerConfig(
        boolean enabled,
        String profile,
        int distance,
        int interval,
        int playerCheckTicks,
        int maxConsecutiveSkips,
        int minimumAgeTicks,
        boolean diagnostics) {

    public static OptimizerConfig load() {
        String requested = System.getProperty("aether.optimizer.profile", "balanced")
                .trim().toLowerCase(java.util.Locale.ROOT);
        String profile = switch (requested) {
            case "conservative", "balanced", "aggressive" -> requested;
            default -> "balanced";
        };
        int defaultDistance = switch (profile) {
            case "conservative" -> 64;
            case "aggressive" -> 96;
            default -> 80;
        };
        int defaultInterval = switch (profile) {
            case "aggressive" -> 3;
            default -> 2;
        };
        return new OptimizerConfig(
                readBoolean("aether.optimizer.ai", true),
                profile,
                readInt("aether.optimizer.distance", defaultDistance, 16, 192),
                readInt("aether.optimizer.interval", defaultInterval, 2, 3),
                readInt("aether.optimizer.playerCheckTicks", 4, 1, 10),
                readInt("aether.optimizer.maxConsecutiveSkips", 4, 1, 12),
                readInt("aether.optimizer.minimumAgeTicks", 100, 0, 1200),
                readBoolean("aether.optimizer.debug", false));
    }

    static boolean readBoolean(String key, boolean fallback) {
        String raw = System.getProperty(key);
        if (raw == null) return fallback;
        return switch (raw.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "true", "yes", "1", "on" -> true;
            case "false", "no", "0", "off" -> false;
            default -> fallback;
        };
    }

    static int readInt(String key, int fallback, int min, int max) {
        try {
            return Math.max(min, Math.min(max,
                    Integer.parseInt(System.getProperty(key, Integer.toString(fallback)).trim())));
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }
}
