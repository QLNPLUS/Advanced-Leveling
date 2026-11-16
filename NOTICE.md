# Attribution and license notice

## Auto Leveling

Advanced Leveling is an unofficial derivative of **Auto Leveling**, originally authored
by Daripher and published on CurseForge under GNU GPLv3:

https://www.curseforge.com/minecraft/mc-mods/auto-leveling

Copyright in the original Auto Leveling code and assets remains with Daripher and the
respective original contributors. The fork name does not imply endorsement by the
original author.

## Advanced Leveling changes

Changes made for Advanced Leveling include expression-based attributes, LootJS
compatibility, KubeJS APIs, configuration formatting, build changes, and maintenance
for this fork. These changes are distributed under GNU AGPLv3 together with the fork.

## YiRanExpressionLib

The distributable jar (`advanced-leveling-neoforge-1.21.1-<version>.jar`) embeds exactly one
copy of YiRanExpressionLib by yiran1457. Its embedded mod metadata declares GNU Lesser General
Public License v3.0. Its source is available from the upstream YiRanExpressionLib project
associated with the Maven coordinate and repository:

`com.github.yiran1457:YiRanExpressionLib:1.0.1`

https://github.com/yiran1457/YiRanExpressionLib

Build provenance of the embedded copy, as pinned in `build.gradle`: the official upstream
`universal` build of upstream commit `60584101e698d1e3f79d327b18982dec7ddb5465`, 62897 bytes,
SHA-256 `d797d9eaf22923b5f0d0d152b583aef3d60600bf349aa481a89ecd159255106a`, requested with the
exact JarJar range `[1.0.1]` and resolved only from `libs/maven`. Those are build facts about
the bytes; they are not a licensing or compliance conclusion.

YiRanExpressionLib remains copyright its author and contributors and is not relicensed
by this project. Advanced Leveling keeps its existing GNU AGPLv3 root license and the
original Auto Leveling attribution; nothing here changes either.

## Other components

Minecraft, NeoForge, mappings, optional compatibility dependencies, and all other third-party
components retain their respective copyrights and licenses. They are not relicensed by
Advanced Leveling.
