<p align="center">
<img src="https://socialify.git.ci/LanceHE6/fabric-allay/image?font=JetBrains+Mono&forks=1&issues=1&language=1&logo=https%3A%2F%2Fr2-api.hycer.cn%2Fimg%2Ffabric-allay-icon.png&name=1&pattern=Floating+Cogs&stargazers=1&theme=Dark" alt="fabric-allay" width="640" height="320" />
</p>

一个 Minecraft Fabric 服务端辅助模组，提供计分板统计、假人管理、试炼区块保护、位置共享、空间测距以及内置特性开关。全部功能通过 `/allay` 指令系统统一操作，支持聊天交互 UI，无需客户端安装任何 mod。

[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE.txt)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.1~26.3-blue)](gradle.properties)
[![Fabric](https://img.shields.io/badge/Fabric_API-0.145.1~0.159.3-yellow)](gradle.properties)

---

## 功能概览

### 计分板 (`/allay asb`)

多维度统计，每玩家独立轮播、自动同步、实时刷新。

- 挖掘量、放置量、在线时长、飞行距离、受到伤害、死亡次数、击杀生物、经验等级、延迟
- 前三名金/银/铜色玩家名
- 双层榜单隐藏：全局（OP）+ 个人
- 积分跳过（名前缀过滤）
- 每玩家独立自定义显示

### 假人管理 (`/allay cbot`)

保存/召唤/分组/批量操作，聊天交互菜单驱动。

### 试炼区块 (`/allay tk`)

vault / trial spawner / ominous vault 保存与恢复，敏感操作两步确认。

### 位置共享 (`/here [to <玩家>]`)

- 全服广播坐标和维度
- 自身发光 10 秒
- 同维度玩家 ActionBar 实时显示目标方向箭头 + 距离 + 倒计时
- `to <玩家>` 仅对指定玩家显示

### 空间测距 (`/dist [<pos1>] [<pos2>]`)

纯服务端实现的 3D 空间测距工具，支持服务端粒子线视觉反馈：

- **射线模式 (`/dist`)**：以玩家脚底为起点，向准星方向发射射线检测目标方块（最大 128 格）。
- **点选模式 (`/dist <pos1> <pos2>`)**：输入指令时自动提示视角指向方块坐标，一步直达两点测距。
- **结构化输出**：直线欧氏距离、水平距离 (XZ)、轴向跨度 ($\Delta X, \Delta Y, \Delta Z$) 以及包围盒方块总数（含端点）。
- **视觉反馈**：两点间生成末地烛粒子连线，并在端点产生高亮标记粒子。

### 特性开关与通知系统

全局特性（临时/永久）：

| 特性 | 说明 |
|------|------|
| fragileObsidian | 黑曜石/哭泣黑曜石挖掘速度等同石头 |
| superTNT | TNT 可破坏黑曜石、哭泣黑曜石和刷怪笼 |
| fragileGlass | 所有玻璃方块/板可被秒破 |
| experienceBottle | Shift+右键玻璃瓶将经验点转换为附魔瓶 |
| phantomSuppressor | 阻止幻翼自然生成 |
| patrolSuppressor | 阻止灾厄巡逻队自然生成 |
| jungleSaplingDropRate | 提高丛林树叶掉落树苗几率至常规树叶水平（5%） |
| elytraRecipe | 工作台合成鞘翅（幻翼膜×6+线×1+下界合金锭×1） |
| spongeRecipe | 工作台合成海绵（沙子×4+纸×4+黄色羊毛×1） |

个人特性（每玩家独立）：

| 特性 | 说明 |
|------|------|
| damageIndicator | 伤害跳字（普通红色，暴击深红加粗 *） |

**安全与提示机制**：
- **变更全服广播**：任意玩家切换全局特性或修改永久默认值时，全服聊天框同步高亮广播。
- **进服状态提醒**：玩家登录时，若服务器当前启用了特殊特性，单播提醒已启用的特性列表。
- **准星 Action Bar 防误触预警**：当开启 `易碎玻璃` 或 `易碎黑曜石` 时，玩家手持镐子瞄准相应方块，屏幕下方动作栏实时弹出红色预警，移开后自动消失。

---

## 指令系统

```
/allay
├── here [to <玩家>]                    # 位置共享
├── dist [<pos1>] [<pos2>]              # 空间测距 / 射线测距
├── help / config / features
├── fragileObsidian <true|false>        # 全局特性
├── superTNT <true|false>
├── fragileGlass <true|false>
├── experienceBottle <true|false>
├── phantomSuppressor <true|false>
├── patrolSuppressor <true|false>
├── jungleSaplingDropRate <true|false>
├── elytraRecipe <true|false>
├── spongeRecipe <true|false>
├── damageIndicator <true|false>         # 个人特性
├── setDefault <规则> <true|false>       # 永久设置
├── removeDefault <规则>
├── asb                                  # 计分板
│   ├── ui / scoreboard <榜单>
│   ├── hide <榜单> / set <配置项> <值>
│   └── ...
├── cbot                                 # 假人管理
│   ├── ui / help / list
│   ├── add|remove|load / group / batch
│   └── ...
└── tk                                   # 试炼区块
    ├── ui / remove / restore / list / clear
    └── ...
```

快捷别名：`/asb` `/cbot` `/trialkeeper` `/here` `/dist`

---

## 配置文件

```
config/allay/
├── allay.json                    # 全局配置、假人预设、特性默认值
├── player_prefs.json             # 每玩家偏好（计分板隐藏 + 伤害跳字）
└── allay_trialkeeper_data.nbt    # 试炼区块数据
```

新增字段自动补全默认值。

---

## 迁移

从独立 mod 切换到 Allay 时，首次启动自动迁移至 `config/allay/`，旧文件加 `.bak` 保留。

---

## 前置依赖

- Minecraft 26.1 ~ 26.3
- Fabric Loader >= 0.19.5
- Fabric API

---

## 构建

```bash
./gradlew buildAll          # 构建全部版本
./gradlew :mc-26.3:build    # 构建单个版本
./gradlew collectJars       # 收集产物到 dist/
```

---

## 项目结构

```
allay/
├── common/src/main/java/cn/hycer/allay/
│   ├── Allay.java                         # 主入口
│   ├── command/                           # 根指令 + 聊天UI
│   ├── config/                            # 全局配置类
│   ├── feature/                           # 特性管理 + 测距 + 提醒 + 伤害跳字 + 位置共享
│   ├── mixin/                             # Mixin
│   ├── asb/                               # 计分板
│   ├── cbm/                               # 假人管理
│   └── tk/                                # 试炼区块
├── mc-26.3/ mc-26.2/ mc-26.1.2/ mc-26.1.1/ mc-26.1/  # 多版本子项目
├── build.gradle / settings.gradle
└── .github/workflows/build-release.yml    # CI/CD
```

---

## 鸣谢

- **[Fabric](https://fabricmc.net/)** — 模组加载框架
- **[Carpet](https://github.com/gnembon/fabric-carpet)** — 假人系统与规则开关灵感来源
- 模组名 "Allay" 取自 Minecraft 中的 **悦灵**
