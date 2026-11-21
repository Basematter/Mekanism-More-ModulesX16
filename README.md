# X16 资源包（Mekanism:MoreModules 物品图标）

本仓库现在同时是 **MoreMekasuitUnits 模组本体**（看 `1.20.1-x16` / `1.21.1-x16` 分支）
和 **X16 贴图资源包**（就是这个 `resourcepack` 分支）。

## 这个分支里有什么

- `pack/`：资源包源文件（`pack.mcmeta` + `assets/` + `pack.png`），可直接拖进 `resourcepacks/`
- `Mekanism-MoreModules-X16.zip`：打好包的分发版本

## 用法

1. 下载 `Mekanism-MoreModules-X16.zip`
2. 丢进 `.minecraft/resourcepacks/`
3. 游戏内启用（资源包优先级天然高于模组，不用管加载顺序）

## 说明

- 8 个单元物品图标，全部原生 16×16
- 底板沿用 Mekanism 模块图标的造型语言
- 修过一版 `pack.mcmeta`：原文件是 GBK 编码且字符串里有裸换行，属于非法 JSON，游戏会直接无视这个包
- 早期版本物模组内自带 128×128 图标，这个包会覆盖成 16×16

许可：MIT。贴图作者 Basematter。
