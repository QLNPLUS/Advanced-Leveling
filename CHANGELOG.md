# Changelog

## 1.0.2

- Added NeoForge 1.21.1 support.
- Improved compatibility with leveled-entity attributes, loot, and configuration settings.

## 1.0.1-fix (NeoForge 1.21.1)

- Fixed a crash when leveled entities dropped additional loot.
- Fixed existing leveled mobs being restored to full health when reloaded from disk or re-added to a level.

## 1.0.1

- Added a JSON configuration file for advanced settings.
- Improved readability when viewing advanced attributes and entity lists.
- Renamed the common and client configuration files to use the Advanced Leveling names.
- Advanced settings can now be reloaded with `/reload`.
- Blacklist and whitelist tool changes now take effect immediately in the advanced configuration.
- Improved reliability when saving advanced settings.
- Existing `autoleveling-common.toml` and `autoleveling-client.toml` files are not migrated automatically; move their settings to the new Advanced Leveling configuration files.

## 1.0.0

- Renamed the maintained fork to Advanced Leveling.
- Added expression-based attribute scaling with `base` and `level` variables.
- Fixed duplicate LootJS entity loot processing for additional leveled-mob loot.
- Added KubeJS setters and increment operations for per-dimension level bonuses.
- Added readable multiline formatting for entity lists and attribute configuration.

## 1.14

- Added level bonus per deepness configuration.
- Created new attributes for mobs: projectile damage bonus and explosion damage bonus.
- Changed the attributes configuration to support modded attributes.

## 1.13a

- Fixed an issue that caused load time to increase drastically.

## 1.13

- Added the ability to change mob textures based on level.

## 1.12

- Added the ability to change mob equipment using datapacks.

## 1.11

- Added the blacklist tool item.
- Added the whitelist tool item.
- Added Jade (Waila) mod interaction.
- Default leveling settings are now stored in the configuration.

## 1.10a

- Fixed the whitelist configuration not working as intended.

## 1.10

- Added the `addlevel` command.
- Added the ability to whitelist or blacklist entire namespaces.
- Blacklist now has higher priority than whitelist.

## 1.9

- Fixed per-dimension datapack configuration not working.
- Added whitelist support.

## 1.8

- Added a special loot table common to all leveled mobs.
- Added separate leveling settings for each entity type. These settings have higher priority than dimension settings.

## 1.7

- All leveling settings can now be adjusted per dimension.
- Added compatibility with dimensions from mods.

## 1.6a

- Added Forge 41.1.0 compatibility.

## 1.6

- Added entity blacklist configuration.
- Added experience bonus per level configuration.
- Added maximum level configuration.
- Added random level bonus and starting level configuration.
- Changed the configuration format.

## 1.5

- Fixed an issue that caused players to be affected by the leveling system.
- Added a loot table condition for checking entity level.

## 1.4

- Added Minecraft 1.19 compatibility.
- The minimum monster level is now 1.
- Improved compatibility with other mods.

## 1.3

- Attribute bonuses are now based on the level of mobs.
- Mob levels are now shown above entities.

## 1.2

- Fixed an incorrect current health bug.

## 1.1

- Fixed the configuration not working.
- Improved attribute bonus calculation.
