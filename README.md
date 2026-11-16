# Advanced Leveling (NeoForge 26.1.2)

Advanced Leveling is an unofficial derivative and continuation of **Auto Leveling** by
Daripher. This branch is the NeoForge 26.1.2 build (Java 25); it keeps the `autoleveling`
mod ID so existing worlds and datapacks remain compatible.

This project is not affiliated with or endorsed by the original author.

- Original project: [Auto Leveling on CurseForge](https://www.curseforge.com/minecraft/mc-mods/auto-leveling)
- Embedded expression library: [YiRanExpressionLib](https://github.com/yiran1457/YiRanExpressionLib)

## What this branch contains

- Mob leveling on entity join: level from starting level, distance to spawn, depth below sea
  level, time of day, random bonus, per-level, world and global bonuses, clamped by the
  configured maximum. Levels are stored as the zero-based `LEVEL` value in the entity's
  persistent data and applied through permanent attribute modifiers.
- Extra experience and an additional loot table for leveled mobs.
- `config/advancedleveling-common.toml` and `config/advancedleveling-client.toml`, plus
  `config/advancedleveling.json` for attribute values, entity lists and hidden levels.
- Attribute values as a number (amount per stored level) or as an expression
  (`base`, `level`) evaluated by the bundled YiRanExpressionLib.
- `/autoleveling level add` and `/autoleveling level get` for the global level bonus.
- A read-only integration API, `daripher.autoleveling.api.LevelingApi`.

This branch does **not** compile the level plate/nameplate renderer, the Jade provider, the
packet sync, the client mixins or the datagen sources, so this artifact does not draw a level
above mobs and does not feed Jade. `alwaysShowLevel`/`showLevelWhenLookingAt` are configured
but have no renderer in this build; the 1.0.3 update does not claim otherwise and does not
restore those renderers.

## Leveling API

`LevelingApi` is a pure read: it never creates a level, never writes entity data and never
refreshes attributes, equipment or drops, so it is safe to call from a server tick.

```java
OptionalInt level = LevelingApi.displayLevel(livingEntity);
boolean hidden = LevelingApi.isHidden(entityType);
```

- The returned value is the displayed level, i.e. the persisted zero-based `LEVEL` value plus
  one, matching how this branch would render a level. A stored zero is reported as `1`.
- The result is empty for players, for a missing or non-integer `LEVEL`, for a negative value,
  for a value that would overflow when incremented, and for types hidden through
  `mobs.hidden_levels`.
- `mobs.hidden_levels` entries are exact IDs (`minecraft:zombie`) or namespace wildcards
  (`minecraft:*`). There is no global wildcard.

## Configuration

`advancedleveling.json` holds attributes and entity lists:

```json
{
  "attributes": {
    "minecraft:generic.max_health": "base + (level - 1) * 10"
  },
  "mobs": {
    "blacklist": ["minecraft:ender_dragon"],
    "whitelist": [],
    "hidden_levels": []
  }
}
```

A non-empty whitelist restricts leveling to matching entities. The hidden list only affects
what the API reports; it does not stop a mob from being leveled.

## Building

Java 25 is required (the NeoForge 26.1.2 toolchain).

```shell
./gradlew build
```

The distributable jar is generated as
`build/libs/advanced-leveling-neoforge-26.1.2-<version>.jar`.

`./gradlew build` also runs the unit tests and the bundled-library gates:

- `verifyExpressionLibArtifact` checks the local `libs/maven` artifact against the pinned
  size and SHA-256 and its Maven identity.
- `verifyExpressionLibResolution` checks that the coordinate resolves from that local
  artifact only (Gradle exclusive content, no JitPack fallback).
- `verifyPackagedExpressionLib` opens the built jar and requires exactly one JarJar entry at
  `META-INF/jarjar/YiRanExpressionLib-1.0.1.jar` whose bytes are the pinned build, matching
  entry for entry, with JarJar metadata that names the pinned coordinate and the library
  descriptors intact.
- `verifyExpressionLibGateFixtures` proves those gates reject missing, truncated, tampered and
  re-packed inputs.

## Bundled library and packaging

The source artifact in `libs/maven` is the canonical `YiRanExpressionLib` build:

- size: 62,897 bytes
- SHA-256: `d797d9eaf22923b5f0d0d152b583aef3d60600bf349aa481a89ecd159255106a`

NeoForge JarJar nests it at `META-INF/jarjar/YiRanExpressionLib-1.0.1.jar` by copying the
resolved bytes, so the nested archive is verified to be byte-identical to the canonical source
artifact.

## Upstream and licensing

This project is derived from **Auto Leveling** by Daripher, whose CurseForge project
declares GNU GPLv3. Original portions remain attributed to Daripher.

Advanced Leveling is distributed under GNU AGPLv3. The bundled YiRanExpressionLib declares
GNU Lesser General Public License v3.0, keeps its own terms and is not relicensed here.

See [NOTICE.md](NOTICE.md) for component attribution and [LICENSE](LICENSE) for the project
license. Minecraft, NeoForge and other build/runtime dependencies retain their own licenses.
