# Advanced Leveling (Fabric 1.21.1)

Advanced Leveling is an unofficial derivative and continuation of **Auto Leveling** by
Daripher. This branch is the Fabric 1.21.1 build; it keeps the `autoleveling` mod ID so
existing worlds and datapacks remain compatible.

This project is not affiliated with or endorsed by the original author.

- Original project: [Auto Leveling on CurseForge](https://www.curseforge.com/minecraft/mc-mods/auto-leveling)
- Embedded expression library: [YiRanExpressionLib](https://github.com/yiran1457/YiRanExpressionLib)

## What this branch contains

- Numerical mob leveling: a level derived from the distance to the world spawn, the depth
  below sea level and the time of day, applied to a mob the first time it ticks and stored
  in the entity's persistent data as the one-based level. Attribute bonuses are the
  built-in numeric ones (movement speed, attack damage, armor, max health).
- `config/advancedleveling.json` with `mobs.blacklist`, `mobs.whitelist` and
  `mobs.hidden_levels`. The blacklist and whitelist tools toggle entries in game.
- A read-only integration API, `daripher.autoleveling.api.LevelingApi`.
- The canonical YiRanExpressionLib binary bundled in the distributable jar.

This branch does **not** compile the level plate/nameplate renderer, the Jade provider, the
command and saved-data level bonuses, the KubeJS/LootJS integration, the TOML configuration
files or the JSON attribute-expression settings that the NeoForge branches have. The
bundled library is present for integrations and for the shared API/tests, not because this
artifact evaluates configured attribute expressions. Nothing in 1.0.3 adds those features
back.

## Leveling API

`LevelingApi` is a pure read: it never creates a level, never writes entity data and never
refreshes attributes, equipment or drops, so it is safe to call from a server tick.

```java
OptionalInt level = LevelingApi.displayLevel(livingEntity);
boolean hidden = LevelingApi.isHidden(entityType);
```

- The stored value is the level this branch already saves: one-based, with `-1`
  (`LevelingApi.NO_LEVEL`) meaning "no level". The API returns it unchanged, keeps a stored
  zero as zero and never adds one, so existing worlds keep their values.
- Players always get an empty result. A type listed in `mobs.hidden_levels` gets an empty
  result too.
- `mobs.hidden_levels` entries are exact IDs (`minecraft:zombie`) or namespace wildcards
  (`minecraft:*`). There is no global wildcard.

## Configuration

```json
{
  "mobs": {
    "blacklist": [
      "minecraft:ender_dragon"
    ],
    "whitelist": [],
    "hidden_levels": []
  }
}
```

A non-empty whitelist restricts leveling to matching entities. `advancedleveling.json` is
read when the game starts and rewritten when a tool toggles an entry. The hidden list only
affects what the API reports; it does not stop a mob from being leveled.

## Building

Java 21 is required.

```shell
./gradlew build
```

The distributable jar is generated as
`build/libs/advanced-leveling-fabric-1.21.1-<version>.jar`.

`./gradlew build` also runs the unit tests and the bundled-library gates:

- `verifyExpressionLibArtifact` checks the local `libs/maven` artifact against the pinned
  size and SHA-256 and its Maven identity.
- `verifyExpressionLibResolution` checks that the coordinate resolves from that local
  artifact only (Gradle exclusive content, no JitPack fallback).
- `verifyPackagedExpressionLib` opens the built jar, requires exactly one nested library
  under `META-INF/jars/`, checks that every entry of the canonical source artifact is
  unchanged inside the nested archive, allows only the Fabric metadata Loom generates, and
  verifies the root `fabric.mod.json` linkage.
- `verifyExpressionLibGateFixtures` proves those gates reject missing, truncated, tampered
  and re-packed inputs.

## Bundled library and packaging

The source artifact in `libs/maven` is the canonical `YiRanExpressionLib` build:

- size: 62,897 bytes
- SHA-256: `d797d9eaf22923b5f0d0d152b583aef3d60600bf349aa481a89ecd159255106a`

The distributable jar nests it at `META-INF/jars/YiRanExpressionLib-1.0.1.jar` through the
standard Loom `include` mechanism. Loom generates the Fabric metadata a nested non-Fabric
library needs and re-packs the archive, so **the nested archive is not byte-identical to the
source artifact and has its own digest**. Every entry of the source artifact is verified
unchanged during the build; the wrapper digest is reported but deliberately never compared
to `d797…`.

## Upstream and licensing

This project is derived from **Auto Leveling** by Daripher, whose CurseForge project
declares GNU GPLv3. Original portions remain attributed to Daripher.

Advanced Leveling is distributed under GNU AGPLv3. The bundled YiRanExpressionLib declares
GNU Lesser General Public License v3.0, keeps its own terms and is not relicensed here.

See [NOTICE.md](NOTICE.md) for component attribution and [LICENSE](LICENSE) for the project
license. Minecraft, Fabric Loader, Fabric API and other build/runtime dependencies retain
their own licenses.
