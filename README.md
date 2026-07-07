<p align="center">
<img src="https://socialify.git.ci/LanceHE6/fabric-allay/image?font=JetBrains+Mono&forks=1&issues=1&language=1&logo=https%3A%2F%2Fr2-api.hycer.cn%2Fimg%2Ffabric-allay-icon.png&name=1&pattern=Floating+Cogs&stargazers=1&theme=Dark" alt="fabric-allay" width="640" height="320" />
</p>

一个 Minecraft Fabric 服务端辅助模组，集成 AdvancedScoreboard、CarpetBotManager、TrialKeeper 三大模块，通过统一的 `/allay` 指令系统提供计分板管理、假人管理、试炼方块保存功能，以及 Carpet 式特性开关。

[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE.txt)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.1~26.2-blue)](gradle.properties)
[![Fabric](https://img.shields.io/badge/Fabric_API-0.145.1~0.154.0-yellow)](gradle.properties)

---

## 模块概览

### AdvancedScoreboard (`/allay asb`)

多维度计分板统计，每玩家独立轮播、自动同步、实时刷新。

**统计项**：挖掘量、放置量、在线时长、飞行距离、受到伤害、死亡次数、击杀生物、延迟显示

**特性**：
- 前三名金/银/铜色玩家名
- 双层榜单隐藏：全局隐藏（OP） + 个人隐藏（全员，持久化）
- 积分跳过（按玩家名前缀过滤假人）
- 每玩家独立自定义显示榜单

### CarpetBotManager (`/allay cbot`)

Carpet 假人管理，保存/召唤/分组/批量操作，聊天交互菜单驱动。

### TrialKeeper (`/allay tk`)

试炼区块（vault / trial spawner / ominous vault）保存与恢复，敏感操作需两步确认。

### 特性开关 (`/allay features`)

Carpet 式规则开关，临时切换重启后恢复默认。

| 特性 | 说明 |
|------|------|
| fragileObsidian | 黑曜石/哭泣黑曜石挖掘速度等同石头 |
| superTNT | TNT 可破坏黑曜石、哭泣黑曜石和刷怪笼 |

- `/allay <规则> true|false` — 临时切换（反馈含"永久更改？"可点击链接）
- `/allay setDefault <规则> true|false` — 永久设置
- `/allay removeDefault <规则>` — 移除永久设置
- `/allay features` — 查看所有特性状态

---

## 指令系统

```
/allay
├── help / config / features
├── fragileObsidian <true|false>
├── superTNT <true|false>
├── setDefault <规则> <true|false>
├── removeDefault <规则>
├── asb                                # AdvancedScoreboard
│   ├── ui                             # 交互菜单（个人隐藏/查看）
│   ├── hide <榜单名>                  # 个人隐藏/显示（无需权限）
│   ├── set <配置项> <值>              # 修改配置（需 OP）
│   │   ├── switchInterval / saveInterval / maxDisplayNum
│   │   ├── border / skipScore / skipPrefix
│   │   └── notDisplay <榜单名>        # 全局隐藏/显示
│   └── scoreboard <榜单名>            # 查询榜单
├── cbot                               # CarpetBotManager
│   ├── ui / help / list
│   ├── add|remove|load <名称>
│   ├── group add|remove|load|autoload
│   ├── batch <前缀> <起> <止> spawn|save|kill|use|attack|sneak
│   └── autoload add|remove|list
└── tk                                 # TrialKeeper
    ├── ui                             # 交互菜单（确认流程）
    ├── remove <名称> <起> <止>
    ├── restore <名称>
    ├── list [名称]
    └── clear [名称]
```

快捷别名：`/asb` `/cbot` `/trialkeeper` 等价于 `/allay asb` 等。

---

## 配置文件

所有数据存储在 `config/allay/` 目录：

```
config/allay/
├── allay.json                    # 全局配置、假人预设、特性默认值
├── scoreboard_prefs.json         # 每玩家计分板隐藏偏好
└── allay_trialkeeper_data.nbt    # 试炼区块保存数据
```

`allay.json` 结构：

```json
{
  "advancedScoreboard": {
    "border": "===",
    "switchInterval": 5,
    "saveInterval": 5,
    "maxDisplayNum": 15,
    "skipScore": false,
    "skipPrefix": "bot_",
    "hiddenScoreboards": [],
    "scoreboards": [...]
  },
  "carpetBotManager": {
    "permissionLevel": 0,
    "botNamePrefix": "bot_",
    "requirePrefix": false,
    "autoLoadBots": [],
    "autoLoadGroups": [],
    "bots": {},
    "groups": {}
  },
  "trialKeeper": {
    "dataFile": "allay_trialkeeper_data.nbt"
  },
  "featureDefaults": {}
}
```

`scoreboard_prefs.json` 格式：`{"<uuid>": ["internalName1", ...]}`

新增字段自动补全默认值，无需手动迁移。

---

## 迁移

从独立 mod 切换到 Allay 时，首次启动自动迁移：

1. `advanced_scoreboard.json` → `allay.json` advancedScoreboard 段
2. `carpetbotmanager.json` → `allay.json` carpetBotManager 段
3. `carpetbotmanager_bots.json` / `carpetbotmanager_groups.json` → `allay.json` bots / groups
4. `trialkeeper_data.nbt` → `allay/allay_trialkeeper_data.nbt`
5. 旧版 `allay.json` → `allay/allay.json`
6. 旧文件加 `.bak` 后缀保留

---

## 前置依赖

- Minecraft 26.1 ~ 26.2
- Fabric Loader >= 0.19.3
- Fabric API

---

## 构建

```bash
./gradlew buildAll          # 构建全部版本
./gradlew :mc-26.2:build    # 构建单个版本
./gradlew collectJars       # 收集产物到 dist/
```

产物：`dist/allay-mc<版本>-<模组版本>.jar`

---

## 项目结构

```
allay/
├── common/src/main/java/cn/hycer/allay/
│   ├── Allay.java                         # 主入口
│   ├── command/                           # 根指令 + 聊天UI
│   ├── config/                            # AllayConfig + 各段配置类
│   ├── feature/                           # 特性管理器 (Carpet式)
│   ├── mixin/                             # Mixin
│   ├── asb/                               # AdvancedScoreboard
│   │   └── PlayerScoreboardPrefs.java     # 玩家偏好持久化
│   ├── cbm/                               # CarpetBotManager
│   └── tk/                                # TrialKeeper
├── mc-26.2/ mc-26.1.2/ mc-26.1.1/ mc-26.1/  # 多版本子项目
├── build.gradle / settings.gradle
└── .github/workflows/build-release.yml    # CI/CD
```

---

## 鸣谢

- **[Fabric](https://fabricmc.net/)** — 模组加载框架
- **[Carpet](https://github.com/gnembon/fabric-carpet)** — 假人系统与规则开关灵感来源
- 模组名 "Allay" 取自 Minecraft 中的 **悦灵**，代表它会像悦灵一样帮助玩家。
