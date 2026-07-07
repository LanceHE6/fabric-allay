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

多维度计分板统计，支持每玩家独立轮播、自动同步、实时刷新。

| 统计项 | 说明 |
|--------|------|
| 挖掘量 | 玩家破坏方块数量 |
| 放置量 | 玩家放置方块数量 |
| 在线时长 | 玩家在线小时数 |
| 飞行距离 | 鞘翅飞行公里数 |
| 受到伤害 | 累计伤害值 |
| 死亡次数 | 死亡次数统计 |
| 击杀生物 | 击杀生物数（不含玩家） |
| 延迟显示 | TAB 列表按颜色显示延迟 |

前三名玩家名显示金/银/铜色。支持积分跳过（按玩家名前缀过滤假人）。

### CarpetBotManager (`/allay cbot`)

Carpet 假人管理，支持保存预设、分组管理、批量操作、自动部署。

- 假人预设创建/删除/召唤
- 假人分组管理（批量加载）
- 批量操作：批量召唤/保存/下线/使用/攻击/潜行
- 自动加载：服务端启动时自动召唤指定假人/分组
- 聊天交互菜单（UI）：点击按钮即可操作

### TrialKeeper (`/allay tk`)

试炼区块方块保存与恢复，适用于试炼大厅维护。

- 移除并保存指定区域的 vault / trial spawner / ominous vault
- 按名称恢复已保存的区块
- 列表查询、详情查看、清空管理
- 敏感操作（恢复/删除/清空）需两步确认

### 特性开关 (`/allay features`)

Carpet 式规则开关，临时切换重启后自动恢复为默认值。

| 特性 | 说明 |
|------|------|
| fragileObsidian | 黑曜石/哭泣黑曜石挖掘速度等同石头 |
| superTNT | TNT 可破坏黑曜石、哭泣黑曜石和刷怪笼 |

- `/allay <规则> true|false` — 临时切换，反馈消息含"永久更改？"可点击链接
- `/allay setDefault <规则> true|false` — 永久设置
- `/allay removeDefault <规则>` — 移除永久设置，恢复 false
- `/allay features` — 查看所有特性状态

---

## 指令系统

```
/allay
├── help / config / features
├── fragileObsidian <true|false>       # 临时切换易碎黑曜石
├── superTNT <true|false>              # 临时切换超级TNT
├── setDefault <规则> <true|false>     # 永久设置
├── removeDefault <规则>               # 移除永久设置
├── asb                                # AdvancedScoreboard
│   ├── ui                             # 交互菜单
│   ├── set <配置项> <值>              # 修改配置（需 OP）
│   ├── scoreboard <榜单名>            # 查询榜单
│   └── notDisplay <榜单名>            # 隐藏/显示榜单
├── cbot                               # CarpetBotManager
│   ├── ui / help / list
│   ├── add|remove|load <名称>
│   ├── group add|remove|load|autoload
│   ├── batch <前缀> <起> <止> spawn|save|kill|use|attack|sneak
│   └── autoload add|remove|list
└── tk                                 # TrialKeeper
    ├── ui                             # 交互菜单
    ├── remove <名称> <起> <止>
    ├── restore <名称>
    ├── list [名称]
    └── clear [名称]
```

快捷别名：`/asb` `/cbot` `/trialkeeper` 可替代 `/allay asb` 等。

---

## 配置文件

服务端首次启动后自动生成 `config/allay.json`：

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
    "dataFile": "config/allay_trialkeeper_data.nbt"
  },
  "featureDefaults": {}
}
```

所有模块配置集中在同一文件，新增字段自动补全默认值。

---

## 迁移

从独立 mod（AdvancedScoreboard / CarpetBotManager / TrialKeeper）切换到 Allay 时，首次启动会自动：

1. 读取 `advanced_scoreboard.json` → 导入 `advancedScoreboard` 段
2. 读取 `carpetbotmanager.json` → 导入 `carpetBotManager` 段
3. 读取 `carpetbotmanager_bots.json` / `carpetbotmanager_groups.json` → 导入 `carpetBotManager.bots` / `groups`
4. 重命名 `trialkeeper_data.nbt` → `allay_trialkeeper_data.nbt`
5. 写入 `allay.json`，旧文件加 `.bak` 后缀保留

---

## 前置依赖

- Minecraft 26.1 ~ 26.2
- Fabric Loader >= 0.19.3
- Fabric API

---

## 构建

```bash
# 构建全部版本
./gradlew buildAll

# 构建单个版本
./gradlew :mc-26.2:build

# 收集产物到 dist/
./gradlew collectJars
```

产物位于 `dist/` 目录，文件名为 `allay-mc<版本>-<模组版本>.jar`。

---

## 项目结构

```
allay/
├── common/src/main/java/cn/hycer/allay/
│   ├── Allay.java                         # 主入口
│   ├── command/                           # 根指令 + 聊天UI
│   ├── config/                            # AllayConfig + 各段配置类
│   ├── feature/                           # 特性管理器 (Carpet式)
│   ├── mixin/                             # Mixin（计分板 + 特性）
│   ├── asb/                               # AdvancedScoreboard 模块
│   ├── cbm/                               # CarpetBotManager 模块
│   └── tk/                                # TrialKeeper 模块
├── mc-26.2/                               # Minecraft 26.2 子项目
├── mc-26.1.2/                             # Minecraft 26.1.2 子项目
├── mc-26.1.1/                             # Minecraft 26.1.1 子项目
├── mc-26.1/                               # Minecraft 26.1 子项目
├── build.gradle                           # 根构建脚本
├── settings.gradle                        # 子项目注册
└── .github/workflows/build-release.yml    # CI/CD 工作流
```

---

## 鸣谢

- **[Fabric](https://fabricmc.net/)** — 模组加载框架
- **[Carpet](https://github.com/gnembon/fabric-carpet)** — 假人系统与规则开关灵感来源
- 模组名 "Allay" 取自 Minecraft 中的 **悦灵**，代表它会像悦灵一样帮助玩家。
