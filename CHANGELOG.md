# Changelog

## 1.0.3

- Added a read-only level API, `daripher.autoleveling.api.LevelingApi`. It reports the level the mod displays (the saved zero-based level plus one) and an empty result for players, for missing or non-integer data, for negative or overflowing values, and for entity types hidden through `mobs.hidden_levels`.
- Added the shared hidden-level rule, so the level plate, Jade and the API all treat an exact entity ID and a `namespace:*` entry the same way.
- Bundled YiRanExpressionLib is now the pinned canonical 1.0.1 build and is resolved exclusively from the repository copy; a missing or edited artifact fails the build instead of falling back to a remote package.
- The canonical library declares Forge 47 as its minimum loader version, so this branch nests a derived Forge 43 packaging of the same library. Only the library's `loaderVersion` line differs; every class and every other resource is byte-identical to the canonical artifact, and the derived packaging is recorded with its own hash.

## 1.0.2

- Added Forge 1.19.2 support.
- Improved compatibility with modded attributes, loot tables, and per-dimension leveling settings.
