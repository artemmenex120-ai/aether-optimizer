# Aether Optimizer 0.33.0 — experimental Minecraft 26.2 prototype

**Not yet build- or runtime-verified.** This Fabric mod attempts to reduce server-side AI work for selected distant mobs. It does not optimize rendering and does not promise a client-FPS increase.

## Build requirements
JDK 25 and Gradle 9.5.1. Run from the project root:

```sh
python tools/validate_project.py
gradle test --no-daemon --stacktrace
gradle build --no-daemon --stacktrace
```

CI runs metadata checks, static validation, unit tests and Gradle build. If successful, JARs and SHA-256 checksums are uploaded as a GitHub Actions artifact.

## JVM properties
- `-Daether.optimizer.ai=true|false` — master switch (default `true`)
- `-Daether.optimizer.profile=conservative|balanced|aggressive`
- `-Daether.optimizer.distance=16..192`
- `-Daether.optimizer.interval=2..3`
- `-Daether.optimizer.playerCheckTicks=1..10`
- `-Daether.optimizer.maxConsecutiveSkips=1..12`
- `-Daether.optimizer.minimumAgeTicks=0..1200`
- `-Daether.optimizer.debug=true|false`

Profiles: conservative = 64 blocks / interval 2; balanced = 80 / interval 2; aggressive = 96 / interval 3.

## Important limitations
The Mixin target and method signature must be verified against the exact Minecraft/Fabric build. Cancelling AI steps changes simulation behavior. Back up worlds and test only on a disposable copy. Static validation is not compilation, and compilation is not runtime testing. No performance gain is claimed until repeatable measurements demonstrate one.
