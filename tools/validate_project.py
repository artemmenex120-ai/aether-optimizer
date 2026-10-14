#!/usr/bin/env python3
"""Fast static checks for Aether Optimizer metadata and safety guardrails."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
mod = json.loads((ROOT / "src/main/resources/fabric.mod.json").read_text(encoding="utf-8"))
mixins = json.loads((ROOT / "src/main/resources/aether_optimizer.mixins.json").read_text(encoding="utf-8"))
props = dict(line.split("=", 1) for line in (ROOT / "gradle.properties").read_text(encoding="utf-8").splitlines() if "=" in line and not line.lstrip().startswith("#"))
java = (ROOT / "src/main/java/dev/aether/optimizer/OptimizerConfig.java").read_text(encoding="utf-8")
mixin_java = (ROOT / "src/main/java/dev/aether/optimizer/mixin/MobAiThrottleMixin.java").read_text(encoding="utf-8")

assert mod["id"] == "aether_optimizer", "Unexpected mod id"
assert mod["version"] == "${version}", "Version must be expanded from Gradle"
assert "aether_optimizer.mixins.json" in mod["mixins"], "Mixin config not registered"
assert mixins["required"] is True, "Mixin config must fail loudly if target is missing"
assert mixins["injectors"]["defaultRequire"] == 1, "Expected strict injector requirement"
assert props["mod_version"].split("+")[0].split("-")[0] in (ROOT / "README.md").read_text(encoding="utf-8"), "README version mismatch"
assert 'method = "serverAiStep"' in mixin_java and "require = 1" in mixin_java, "Strict target injection missing"
assert "minimumAgeTicks" in java and "aether.optimizer.minimumAgeTicks" in java, "Spawn grace period config missing"
assert "maxConsecutiveSkips" in java, "Skip safety cap missing"
assert "AgeableMob ageable && ageable.isBaby()" in mixin_java, "Baby check must be type-safe for Mob"
assert "mob.isBaby()" not in mixin_java, "Mob does not guarantee the AgeableMob API"
assert "getNearestPlayer" in mixin_java, "Player-distance guard missing"
assert "readBoolean(" + '"aether.optimizer.ai", true)' in java, "Master switch must have a defensive default"
assert 'case "true", "yes", "1", "on"' in java, "Boolean parsing aliases missing"
assert (ROOT / "src/test/java/dev/aether/optimizer/OptimizerConfigTest.java").is_file(), "Config unit tests missing"
assert "useJUnitPlatform()" in (ROOT / "build.gradle").read_text(encoding="utf-8"), "JUnit platform not enabled"
assert "aether$consecutiveSkips = 0" in mixin_java, "Skip counter reset guards missing"
assert props["minecraft_version"] == "26.2", "Minecraft version mismatch"
print(f"Static validation passed: {mod['name']} {props['mod_version']}")
