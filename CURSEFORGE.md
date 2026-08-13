# Advanced Leveling

Advanced Leveling is an unofficial derivative and continuation of **Auto Leveling** by
Daripher for Minecraft Forge 1.20.1. It adds precise formula-based attribute scaling,
KubeJS level controls, LootJS compatibility, and a redesigned configuration system.

The mod keeps the `autoleveling` mod ID so existing worlds and datapacks remain
compatible. This project is not affiliated with or endorsed by the original author.

## Features

- Mob levels based on distance, depth, time, dimension, and entity settings
- Numeric and expression-based attribute scaling
- Linear, exponential, conditional, and piecewise growth curves
- KubeJS controls for per-dimension level bonuses
- LootJS-compatible additional leveled-mob loot
- Entity blacklists, whitelists, namespace wildcards, and hidden-level lists
- Pretty-printed JSON for attributes and entity lists
- Existing Auto Leveling world and datapack compatibility

## Configuration

Advanced Leveling 1.0.1 uses three configuration files:

- `config/advancedleveling-common.toml`: general server and leveling settings
- `config/advancedleveling-client.toml`: client display settings
- `config/advancedleveling.json`: attributes and entity lists

The old `autoleveling-common.toml` and `autoleveling-client.toml` files are not read or
migrated. Move any settings you still need to the new files manually.

Example `advancedleveling.json`:

```json
{
  "attributes": {
    "minecraft:generic.movement_speed": 0.001,
    "minecraft:generic.flying_speed": 0.001,
    "minecraft:generic.attack_damage": "level <= 10 ? base + (level - 1) * 2 : base + 18 + (level - 10) * 5",
    "minecraft:generic.armor": 0.1,
    "minecraft:generic.max_health": "base * (1.05 ^ (level - 1))",
    "autoleveling:monster.projectile_damage_bonus": 0.1,
    "autoleveling:monster.explosion_damage_bonus": 0.1
  },
  "mobs": {
    "blacklist": [
      "minecraft:ender_dragon",
      "minecraft:wither"
    ],
    "whitelist": [],
    "hidden_levels": []
  }
}
```

Entity lists accept exact IDs such as `minecraft:zombie` and namespace wildcards such
as `minecraft:*`. When the whitelist is not empty, only matching entities can level.
The blacklist takes priority over the whitelist. Changes are loaded on restart or with
`/reload`.

## Attribute expressions

A numeric value keeps the original behavior and applies a `MULTIPLY_BASE` bonus for
each level above level 1:

```json
"minecraft:generic.max_health": 0.05
```

A quoted expression returns the final target attribute value:

```json
"minecraft:generic.max_health": "base + (level - 1) * 10"
```

Available variables:

- `base`: the entity's original base attribute value
- `level`: the displayed mob level, starting at 1

Exponential growth uses `^`:

```json
"minecraft:generic.max_health": "base * (1.05 ^ (level - 1))"
```

Conditional expressions can create piecewise growth curves:

```json
"minecraft:generic.attack_damage": "level <= 10 ? base + (level - 1) * 2 : base + 18 + (level - 10) * 5"
```

Datapack `attribute_modifiers` also support expressions:

```json
{
  "attribute": "minecraft:generic.max_health",
  "expression": "base + (level - 1) * 10"
}
```

## KubeJS integration

The saved level bonus of each dimension can be replaced or incremented from a KubeJS
server script:

```javascript
const ServerLifecycleHooks = Java.loadClass(
    'net.minecraftforge.server.ServerLifecycleHooks'
)
const WorldLevelingData = Java.loadClass(
    'daripher.autoleveling.saveddata.WorldLevelingData'
)

let server = ServerLifecycleHooks.getCurrentServer()
let level = server.getLevel(dimension)

if (level != null) {
    let levelingData = WorldLevelingData.get(level)
    levelingData.setLevelBonus(25)
    levelingData.addLevelBonus(5)
}
```

`setLevelBonus(value)` replaces the dimension's saved bonus. `addLevelBonus(value)`
adds to its current bonus. Both methods mark the data for saving automatically.

## LootJS compatibility

Loot added through LootJS entity modifiers no longer causes Advanced Leveling's
optional additional loot table to execute multiple times. LootJS additions and the
`autoleveling:gameplay/leveled_mobs` loot table can be used together without unintended
duplicate drops.

## Requirements and compatibility

- Minecraft 1.20.1
- Minecraft Forge 47 or newer
- Java 17
- KubeJS and LootJS are optional
- YiRanExpressionLib is embedded and does not need to be installed separately

## Attribution and license

Advanced Leveling is an unofficial derivative of **Auto Leveling** by Daripher. Original
portions remain attributed to Daripher and the original contributors. The fork does not
imply endorsement by the original author.

- Original project: https://www.curseforge.com/minecraft/mc-mods/auto-leveling
- Source code: https://github.com/QLNPLUS/Advanced-Leveling
- License: GNU AGPLv3

The distributed jar embeds YiRanExpressionLib, which is also licensed under GNU AGPLv3.
