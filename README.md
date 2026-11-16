# Advanced Leveling

Advanced Leveling is an unofficial derivative and continuation of **Auto Leveling** by
Daripher for Minecraft Forge 1.20.1. This branch is the NeoForge 1.21.1 port. It keeps the
`autoleveling` mod ID so existing worlds and datapacks remain compatible. Version 1.0.1
introduced new configuration files and does not import the original Auto Leveling
configuration.

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

## Level API

`daripher.autoleveling.api.LevelingApi.displayLevel(LivingEntity)` returns the level this
mod displays for a non-player living entity as an `OptionalInt`. It is a read-only,
server-safe call: nothing is written to the entity, and no attribute, equipment or drop is
touched. Players always get an empty result, because this branch has no Advanced Leveling
player progression.

The result is empty when `LEVEL` is absent or is not an integer, when the stored value is
negative or would overflow when incremented, and when the entity type is listed in
`mobs.hidden_levels`. A stored `0` is a real level and is reported as `1`.

```java
OptionalInt level = LevelingApi.displayLevel(entity);
if (level.isPresent()) {
  // level.getAsInt() is the number the level plate shows
}
```

## Building

Java 21 is required.

```shell
./gradlew build
```

The distributable jar is generated as
`build/libs/advanced-leveling-neoforge-1.21.1-<version>.jar`.

YiRanExpressionLib `1.0.1` is declared as Gradle exclusive content of the local Maven
repository in `libs/maven`: no other repository is asked for that module, and the artifact is
committed. It is the official upstream `universal` build pinned by size and SHA-256 in
`build.gradle`. The build refuses to package anything else and fails instead of resolving the
coordinate from JitPack or anywhere else. `./gradlew check` verifies the artifact and its
resolution, and `./gradlew build` additionally verifies the nested library inside the
packaged jar.

## Upstream and licensing

This project is derived from **Auto Leveling** by Daripher, whose CurseForge project
declares GNU GPLv3. Original portions remain attributed to Daripher.

Advanced Leveling continues to be distributed under its existing GNU AGPLv3 root license
(see [LICENSE](LICENSE)). This version changes no licensing decision and makes no
legal-compliance conclusion. The distributable jar carries one copy of YiRanExpressionLib,
pinned in `build.gradle` to the official upstream `1.0.1` `universal` build by size and
SHA-256. That build declares GNU Lesser General Public License v3.0 in its own embedded mod
metadata and remains under its own terms; see [NOTICE.md](NOTICE.md) for the component
attribution and the pinned identity.

See [NOTICE.md](NOTICE.md) for component attribution and [LICENSE](LICENSE) for the
project license. Forge and other build/runtime dependencies retain their own licenses.
