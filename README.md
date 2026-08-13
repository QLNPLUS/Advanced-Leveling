# Advanced Leveling

Advanced Leveling is an unofficial derivative and continuation of **Auto Leveling** by
Daripher for Minecraft Forge 1.20.1. It keeps the `autoleveling` mod ID so existing
worlds, configs, and datapacks remain compatible.

This project is not affiliated with or endorsed by the original author.

- Original project: [Auto Leveling on CurseForge](https://www.curseforge.com/minecraft/mc-mods/auto-leveling)
- Embedded expression library: [YiRanExpressionLib](https://github.com/yiran1457/YiRanExpressionLib)

## Changes from Auto Leveling

- Attribute formulas using `base` and `level` through YiRanExpressionLib.
- LootJS compatibility for the additional leveled-mob loot table.
- KubeJS-accessible per-dimension level bonus setters and increment operations.
- Readable multiline TOML formatting for entity lists and attribute entries.

## Attribute expressions

The original numeric mode remains supported:

```toml
["minecraft:generic.max_health", 0.05]
```

The second value may instead be an expression returning the final target value:

```toml
["minecraft:generic.max_health", "base + (level - 1) * 10"]
```

- `base`: the entity's base attribute value.
- `level`: the displayed mob level, starting at 1.

Datapack `attribute_modifiers` also accept an expression:

```json
{
  "attribute": "minecraft:generic.max_health",
  "expression": "base + (level - 1) * 10"
}
```

## Building

Java 17 is required.

```shell
./gradlew build
```

The distributable jar is generated as
`build/libs/AdvancedLeveling-1.20.1-1.0.0-all.jar`.

YiRanExpressionLib is normally resolved from JitPack. The `libs/maven` directory is
an optional local Maven fallback; its jar is intentionally not committed.

## Upstream and licensing

This project is derived from **Auto Leveling** by Daripher, whose CurseForge project
declares GNU GPLv3. Original portions remain attributed to Daripher.

Advanced Leveling is distributed under GNU AGPLv3 because its distributable jar embeds
YiRanExpressionLib, which declares GNU AGPLv3. Complete corresponding source for each
published binary must be made available from this repository at the matching tag.

See [NOTICE.md](NOTICE.md) for component attribution and [LICENSE](LICENSE) for the
project license. Forge and other build/runtime dependencies retain their own licenses.
