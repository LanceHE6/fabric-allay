<p align="center">
<img src="https://socialify.git.ci/LanceHE6/fabric-allay/image?font=JetBrains+Mono&forks=1&issues=1&language=1&logo=https%3A%2F%2Fr2-api.hycer.cn%2Fimg%2Ffabric-allay-icon.png&name=1&pattern=Floating+Cogs&stargazers=1&theme=Dark" alt="fabric-allay" width="640" height="320" />
</p>

一个 Minecraft Fabric 服务端辅助模组，提供计分板统计、假人管理、试炼区块保护，以及内置特性开关。全部功能通过 `/allay` 指令系统统一操作，支持聊天交互 UI，无需客户端安装任何 mod。

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
- 双层榜单隐藏：全局隐藏（OP）+ 个人隐藏（全员，持久化）
- 积分跳过（按玩家名前缀过滤假人）
- 每玩家独立自定义显示榜单

### CarpetBotManager (`/allay cbot`)

Carpet 假人管理，保存/召唤/分组/批量操作，聊天交互菜单驱动。

### TrialKeeper (`/allay tk`)

试炼区块（vault / trial spawner / ominous vault）保存与恢复，敏感操作需两步确认。

### 特性开关

Carpet 式规则系统，分全局（临时/永久）和个人（持久化）两类。

**全局特性** (`/allay features`)：

| 特性 | 说明 |
|------|------|
| fragileObsidian | 黑曜石/哭泣黑曜石挖掘速度等同石头 |
| superTNT | TNT 可破坏黑曜石、哭泣黑曜石和刷怪笼 |

**个人特性**（每玩家独立开关）：

| 特性 | 说明 |
|------|------|
| damageIndicator | 攻击时在目标上方显示伤害数值跳字 |

- 普通伤害：红色 `§c`，保留 1 位小数
- 暴击：深红加粗 `§4§l`
- 3 秒持续，新伤害刷新重置计时器

---

## 指令系统

```
/allay
├── help / config / features
├── fragileObsidian <true|false>        # 全局特性临时切换
├── superTNT <true|false>
├── damageIndicator <true|false>         # 个人特性开关
├── setDefault <规则> <true|false>       # 全局特性永久设置
├── removeDefault <规则>                 # 移除全局永久设置
├── asb                                  # AdvancedScoreboard
│   ├── ui                               # 交互菜单
│   ├── hide <榜单名>                    # 个人隐藏/显示
│   ├── set <配置项> <值>                # 配置修改（需 OP）
│   └── scoreboard <榜单名>              # 查询榜单
├── cbot                                 # CarpetBotManager
│   ├── ui / help / list
│   ├── add|remove|load <名称>
│   ├── group / batch / autoload
│   └── ...
└── tk                                   # TrialKeeper
    ├── ui                               # 交互菜单
    ├── remove <名称> <起> <止>
    ├── restore <名称>
    ├── list [名称]
    └── clear [名称]
```

快捷别名：`/asb` `/cbot` `/trialkeeper`

---

## 配置文件

```
config/allay/
├── allay.json                    # 全局配置、假人预设、特性默认值
├── player_prefs.json             # 每玩家偏好（计分板隐藏 + 伤害跳字）
└── allay_trialkeeper_data.nbt    # 试炼区块数据
```

`allay.json`：

```json
{
  "advancedScoreboard": { ... },
  "carpetBotManager": { ... },
  "trialKeeper": { "dataFile": "allay_trialkeeper_data.nbt" },
  "featureDefaults": {}
}
```

`player_prefs.json`：

```json
{
  "scoreboardHidden": { "<uuid>": ["internalName1"] },
  "damageIndicator": { "<uuid>": true }
}
```

新增字段自动补全默认值。

---

## 迁移

从独立 mod 切换到 Allay 时，首次启动自动迁移配置文件至 `config/allay/`，旧文件加 `.bak` 保留。

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

---

## 项目结构

```
allay/
├── common/src/main/java/cn/hycer/allay/
│   ├── Allay.java                         # 主入口
│   ├── command/                           # 根指令 + 聊天UI
│   ├── config/                            # 全局配置类
│   ├── feature/                           # 特性管理器 + 玩家偏好 + 伤害跳字
│   ├── mixin/                             # Mixin
│   ├── asb/                               # AdvancedScoreboard
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
