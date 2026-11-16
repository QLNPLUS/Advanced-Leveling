# Advanced Leveling

Advanced Leveling is an unofficial derivative and continuation of **Auto Leveling** by
Daripher for Minecraft Forge 1.19.2. It keeps the `autoleveling` mod ID so existing
worlds and datapacks remain compatible. Version 1.0.1 uses new configuration files and
does not import the original Auto Leveling configuration.

This project is not affiliated with or endorsed by the original author.

- Original project: [Auto Leveling on CurseForge](https://www.curseforge.com/minecraft/mc-mods/auto-leveling)
- Embedded expression library: [YiRanExpressionLib](https://github.com/yiran1457/YiRanExpressionLib)

## Changes from Auto Leveling

- Attribute formulas using `base` and `level` through YiRanExpressionLib.
- LootJS compatibility for the additional leveled-mob loot table.
- KubeJS-accessible per-dimension level bonus setters and increment operations.
- A dedicated, pretty-printed JSON file for attributes and entity lists.

## Configuration files

- `config/advancedleveling-common.toml`: general server and leveling values.
- `config/advancedleveling-client.toml`: client display settings.
- `config/advancedleveling.json`: attributes and entity lists.

The old `autoleveling-common.toml` and `autoleveling-client.toml` files are not read.
This intentional break prevents Forge's TOML writer from collapsing complex lists.

```json
{
  "attributes": {
    "minecraft:generic.movement_speed": 0.001,
    "minecraft:generic.flying_speed": 0.001,
    "minecraft:generic.attack_damage": 0.1,
    "minecraft:generic.armor": 0.1,
    "minecraft:generic.max_health": "base + (level - 1) * 10",
    "autoleveling:monster.projectile_damage_bonus": 0.1,
    "autoleveling:monster.explosion_damage_bonus": 0.1
  },
  "mobs": {
    "blacklist": [
      "minecraft:ender_dragon"
    ],
    "whitelist": [],
    "hidden_levels": []
  }
}
```

Entity lists accept exact IDs such as `minecraft:zombie` and namespace wildcards such
as `minecraft:*`. A non-empty whitelist restricts leveling to matching entities.
Changes to `advancedleveling.json` are loaded on restart or with `/reload`.

## Attribute expressions

The original numeric value mode remains supported in `advancedleveling.json`:

```json
"minecraft:generic.max_health": 0.05
```

The second value may instead be an expression returning the final target value:

```json
"minecraft:generic.max_health": "base + (level - 1) * 10"
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

## Read-only level API

`daripher.autoleveling.api.LevelingApi` lets another mod read the level Advanced Leveling
displays, without changing anything:

```java
OptionalInt level = LevelingApi.displayLevel(livingEntity);
boolean hidden = LevelingApi.isHidden(entityType);
```

- The result is the saved zero-based `LEVEL` value plus one, i.e. the number on the level
  plate. A saved zero is a real level and reports `1`.
- The result is empty for players, for a missing or non-integer `LEVEL` value, for a
  negative value, for a value that would overflow when incremented, and for entity types
  listed in `mobs.hidden_levels`.
- The call only reads. It never writes NBT, never refreshes attributes, equipment or
  drops, and has no client-only references, so it is safe on a dedicated server.
- `isHidden` is the same exact-ID / `namespace:*` rule every display path uses.

## Building

Java 17 is required.

```shell
./gradlew build
```

The distributable jar is generated as
`build/forge119-release/advanced-leveling-forge-1.19.2-1.0.3-all.jar`.

The YiRanExpressionLib artifact committed under `libs/maven` is the pinned canonical
1.0.1 build (62897 bytes). It is the only source Gradle is allowed to resolve that
coordinate from, and the build fails if the artifact is missing or edited.

That canonical artifact declares Forge 47 as its minimum loader version, which Forge 43
refuses to load, so the release nests a derived Forge 43 packaging of the same library.
The packaging changes only the library's `META-INF/mods.toml` `loaderVersion` line from
`[47,)` to `[43,)`; every class and every other resource is byte-identical to the
canonical artifact, and both hashes are recorded separately.

## Upstream and licensing

This project is derived from **Auto Leveling** by Daripher, whose CurseForge project
declares GNU GPLv3. Original portions remain attributed to Daripher.

Advanced Leveling is distributed under GNU AGPLv3 because its distributable jar embeds
YiRanExpressionLib, which declares GNU AGPLv3. Complete corresponding source for each
published binary must be made available from this repository at the matching tag.

See [NOTICE.md](NOTICE.md) for component attribution and [LICENSE](LICENSE) for the
project license. Forge and other build/runtime dependencies retain their own licenses.
