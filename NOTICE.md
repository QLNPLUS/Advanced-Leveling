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

The distributable JAR embeds YiRanExpressionLib by yiran1457. Its embedded mod
metadata declares GNU Lesser General Public License v3.0. Its source is available from the upstream
YiRanExpressionLib project associated with the Maven coordinate and repository:

`com.github.yiran1457:YiRanExpressionLib:1.0.1`

https://github.com/yiran1457/YiRanExpressionLib

YiRanExpressionLib remains copyright its author and contributors and is not relicensed
by this project.

### Bundled archive and packaging (Fabric)

The source artifact kept in `libs/maven` is the canonical YiRanExpressionLib build:
62,897 bytes, SHA-256
`d797d9eaf22923b5f0d0d152b583aef3d60600bf349aa481a89ecd159255106a`.

The distributable jar nests this library at
`META-INF/jars/YiRanExpressionLib-1.0.1.jar` through the standard Loom `include`
mechanism. Loom generates the Fabric metadata a nested non-Fabric library requires and
re-packs the nested archive, so the nested archive has its own digest and is **not**
byte-identical to the source artifact. The build verifies that every entry of the source
artifact is unchanged inside the wrapper and records the wrapper digest; this project does
not claim the nested archive matches the canonical source hash.

### Leveling API

`daripher.autoleveling.api.LevelingApi` is a read-only integration surface. It contains no
client-only references and does not modify entity data. The Fabric artifact does not
compile the nameplate renderer or the Jade provider, so the API does not advertise them.

## Other components

Minecraft, Fabric Loader, Fabric API, mappings, optional compatibility dependencies, and all
other third-party components retain their respective copyrights and licenses. They are not
relicensed by Advanced Leveling.
