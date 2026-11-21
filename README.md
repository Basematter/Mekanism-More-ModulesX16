# MoreMekasuitUnits · X16

给 Mekanism 的 MekaSuit 加单元（module）的扩展模组。

这是 **1.21.1 / NeoForge** 分支。1.20.1 / Forge 在 [`1.20.1-x16`](../../tree/1.20.1-x16) 分支。

## 下载

| 版本 | 分支 | 说明 |
|---|---|---|
| MC 1.21.1 / NeoForge 21.1.x | 本分支 `1.21.1-x16` | 升级会清掉存档里的旧物品 |
| MC 1.20.1 / Forge 47.x | [`1.20.1-x16`](../../tree/1.20.1-x16) | 直接覆盖升级，不清物品 |

jar 在 [Releases](../../releases) 里。

## 依赖

| 模组 | 必要性 | 说明 |
|---|---|---|
| Mekanism | **必需** | ≥ 10.7 |
| TACZ | 可选 | 启用枪械相关单元 |
| Thirst | 可选 | 启用自动喝水单元 |

## 单元

火焰喷射 / 鞘翅加速 / 抢夺 / 防弹 / 快速开镜 / 快速换弹 / 快速跑射 / 后座抵消 / 自动喝水 —— 共 9 个。

## 升级注意

本分支把 modid 从 `moremekasuitmodules` 改回了 `moremekasuitunits`（原名和另一个模组撞车，同 id 时加载器会静默丢掉一个）。

**代价：命名空间变了，存档里本模组的旧物品会失效。** 新开档，或确认能接受清空再升级。

物品图标为 X16 重投版本，底板沿用 Mekanism 模块图标的造型。资源包分支 [`resourcepack`](../../tree/resourcepack) 里有单独可分发的版本。

## 已知问题

- **TiCEX**：单元装不进它的镶板盔甲；装了单元的 MekaSuit 浇铸成镶板盔甲后穿戴可能死档
- **TACZ**：1.1.x 与 1.2.x 均可用（对 1.2.0 的支持自 X16.1 起）

---

MIT 许可。代码原作者 [shuangfa114](https://github.com/shuangfas)，X16 贴图 Basematter。

<sub>注：本分支原本的 README 只有一句 —— *It seems like I made a very foolish decision*（原作者 shuangfa114，2025-07-01）。留此存档。</sub>
