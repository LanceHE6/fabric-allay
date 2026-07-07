package cn.hycer.allay.command;

import cn.hycer.allay.Allay;
import cn.hycer.allay.config.AllayConfig;
import cn.hycer.allay.feature.FeatureManager;
import cn.hycer.allay.asb.config.ScoreboardItem;
import cn.hycer.allay.tk.TrialStorage;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;

/**
 * Unified chat-based UI for /allay, accessible entirely server-side.
 * Pattern: clickable text buttons — no client mod required.
 */
public final class AllayChatInterface {

    private static final String ALLAY = "/allay ";
    private static final String ASB = ALLAY + "asb ";
    private static final String CBOT = ALLAY + "cbot ";
    private static final String TK = ALLAY + "tk ";

    private AllayChatInterface() {}

    // ═══════════════════════════════════════════════════════════
    //  Main menu  (/allay)
    // ═══════════════════════════════════════════════════════════

    public static int showMainMenu(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(title("Allay"));
        src.sendSystemMessage(Component.literal("")
                .append(btn(" [计分板] ", ASB + "ui"))
                .append(btn(" [假人] ", CBOT + "ui"))
                .append(btn(" [试炼] ", TK + "ui")));
        src.sendSystemMessage(Component.literal("")
                .append(btn(" [设置] ", ALLAY + "config"))
                .append(btn(" [特性] ", ALLAY + "features"))
                .append(btn(" [帮助] ", ALLAY + "help")));
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    //  Help  (/allay help)
    // ═══════════════════════════════════════════════════════════

    public static int showHelp(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(title("帮助"));
        src.sendSystemMessage(Component.literal("  /allay asb scoreboard <名称>  查看计分板"));
        src.sendSystemMessage(Component.literal("  /allay asb set <项> <值>    修改计分板设置"));
        src.sendSystemMessage(Component.literal("  /allay asb notDisplay <名称> 隐藏/显示榜单"));
        src.sendSystemMessage(Component.literal("  /allay cbot add <玩家>      保存假人预设"));
        src.sendSystemMessage(Component.literal("  /allay cbot load <名称>     召唤假人"));
        src.sendSystemMessage(Component.literal("  /allay cbot batch ...       批量操作"));
        src.sendSystemMessage(Component.literal("  /allay tk remove <名> <起> <止>  移除并保存试炼区块"));
        src.sendSystemMessage(Component.literal("  /allay tk restore <名称>     恢复试炼区块"));
        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(back(ALLAY));
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    //  Config  (/allay config)
    // ═══════════════════════════════════════════════════════════

    public static int showConfig(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        var cfg = AllayConfig.getInstance();
        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(title("设置"));

        src.sendSystemMessage(subtitle("计分板"));
        src.sendSystemMessage(Component.literal("  轮播间隔: " + cfg.getSwitchInterval() + "s  ")
                .append(suggestBtn("[改]", ASB + "set switchInterval ")));
        src.sendSystemMessage(Component.literal("  保存间隔: " + cfg.getSaveInterval() + "s  ")
                .append(suggestBtn("[改]", ASB + "set saveInterval ")));
        src.sendSystemMessage(Component.literal("  最大显示: " + cfg.getMaxDisplayNum() + "  ")
                .append(suggestBtn("[改]", ASB + "set maxDisplayNum ")));
        src.sendSystemMessage(Component.literal("  边框: " + cfg.getBorder() + "  ")
                .append(suggestBtn("[改]", ASB + "set border ")));
        src.sendSystemMessage(Component.literal("  积分跳过: " + (cfg.isSkipScore() ? "开" : "关") + "  ")
                .append(suggestBtn("[改]", ASB + "set skipScore ")));
        if (cfg.isSkipScore()) {
            src.sendSystemMessage(Component.literal("  跳过前缀: " + cfg.getSkipPrefix() + "  ")
                    .append(suggestBtn("[改]", ASB + "set skipPrefix ")));
        }

        src.sendSystemMessage(subtitle("假人"));
        src.sendSystemMessage(Component.literal("  名前缀: " + cfg.getBotNamePrefix() + "  ")
                .append(suggestBtn("[改]", CBOT + "...")));
        src.sendSystemMessage(Component.literal("  权限等级: " + cfg.getPermissionLevel()));

        src.sendSystemMessage(subtitle("试炼"));
        src.sendSystemMessage(Component.literal("  数据文件: " + cfg.getTkDataFile()));

        var mgr = FeatureManager.getInstance();
        src.sendSystemMessage(subtitle("功能开关"));
        src.sendSystemMessage(Component.literal("  易碎黑曜石: " + (mgr.isFragileObsidian() ? "开" : "关") + "  ")
                .append(suggestBtn("[改]", ALLAY + "fragileObsidian ")));
        src.sendSystemMessage(Component.literal("  超级TNT: " + (mgr.isSuperTNT() ? "开" : "关") + "  ")
                .append(suggestBtn("[改]", ALLAY + "superTNT ")));

        src.sendSystemMessage(Component.literal("").append(back(ALLAY)));
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    //  Feature list  (/allay features)
    // ═══════════════════════════════════════════════════════════

    private static final String[][] ALL_FEATURES = {
            {"fragileObsidian", "易碎黑曜石", "黑曜石挖掘速度等同石头"},
            {"superTNT", "超级TNT", "TNT可破坏黑曜石和刷怪笼"},
    };

    public static int showFeatureList(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        var mgr = FeatureManager.getInstance();
        var cfg = AllayConfig.getInstance();

        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(title("功能开关"));

        for (String[] f : ALL_FEATURES) {
            String name = f[0], display = f[1], desc = f[2];
            boolean current = "fragileObsidian".equals(name) ? mgr.isFragileObsidian() : mgr.isSuperTNT();
            boolean perm = cfg.hasFeatureDefault(name);
            String status = current ? "§a开" : "§c关";
            if (!perm) status += " §7(临时)";

            MutableComponent line = Component.literal("");
            line.append(Component.literal("  " + display + " " + status));
            line.append(Component.literal("  ").withStyle(s -> s.withColor(TextColor.fromRgb(0x888888)))
                    .append(suggestBtn(current ? "[关闭]" : "[开启]",
                            ALLAY + name + " " + !current)));
            if (perm) {
                line.append(btn(" [移除永久]", ALLAY + "removeDefault " + name));
            }

            src.sendSystemMessage(line);
        }

        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(Component.literal("  使用 /allay <规则> true|false 临时切换")
                .withStyle(s -> s.withColor(TextColor.fromRgb(0x888888))));
        src.sendSystemMessage(Component.literal("  使用 /allay setDefault|removeDefault 永久设置")
                .withStyle(s -> s.withColor(TextColor.fromRgb(0x888888))));
        src.sendSystemMessage(Component.literal("").append(back(ALLAY)));
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    //  ASB UI  (/allay asb ui)
    // ═══════════════════════════════════════════════════════════

    public static int showAsbMenu(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        var cfg = AllayConfig.getInstance();
        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(title("计分板管理"));

        var boards = cfg.getScoreboards();
        var hidden = cfg.getHiddenScoreboards();
        if (boards.isEmpty()) {
            src.sendSystemMessage(Component.literal("  暂无计分板"));
        } else {
            for (var sb : boards) {
                boolean isHidden = hidden.contains(sb.getInternalName());
                String marker = isHidden ? " [已隐藏]" : "";
                int count = sb.getData().size();
                src.sendSystemMessage(Component.literal("  " + sb.getDisplayName() + marker + " (" + count + "人)")
                        .append(btn(isHidden ? " [显示]" : " [隐藏]",
                                ASB + "notDisplay " + sb.getDisplayName()))
                        .append(btn(" [查看]", ASB + "scoreboard " + sb.getDisplayName())));
            }
        }

        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(Component.literal("")
                .append(suggestBtn("[轮播间隔]", ASB + "set switchInterval "))
                .append(suggestBtn("[保存间隔]", ASB + "set saveInterval "))
                .append(suggestBtn("[最大显示]", ASB + "set maxDisplayNum "))
                .append(suggestBtn("[边框]", ASB + "set border ")));

        src.sendSystemMessage(Component.literal("").append(back(ALLAY)));
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    //  TK UI  (/allay tk ui)
    // ═══════════════════════════════════════════════════════════

    public static int showTkMenu(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(title("试炼区块管理"));

        var regions = TrialStorage.getAllRegions();
        if (regions.isEmpty()) {
            src.sendSystemMessage(Component.literal("  暂无保存的区块"));
        } else {
            src.sendSystemMessage(Component.literal("  已保存 " + regions.size() + " 个区域（共 " + TrialStorage.totalStored() + " 个方块）:"));
            for (var entry : regions.entrySet()) {
                String name = entry.getKey();
                int count = entry.getValue().size();
                src.sendSystemMessage(Component.literal("    " + name + " (" + count + " 方块)")
                        .append(btn(" [恢复]", TK + "ui confirm-restore " + name))
                        .append(btn(" [详情]", TK + "list " + name))
                        .append(warnBtn(" [删除]", TK + "ui confirm-delete " + name)));
            }
        }

        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(Component.literal("")
                .append(suggestBtn("[移除区块]", TK + "remove <名称> <起> <止>"))
                .append(warnBtn(" [清空全部]", TK + "ui confirm-clear-all")));

        src.sendSystemMessage(Component.literal("").append(back(ALLAY)));
        return 1;
    }

    // ── TK confirmation pages ─────────────────────────────────

    public static int confirmRestore(CommandContext<CommandSourceStack> ctx, String name) {
        var src = ctx.getSource();
        int count = TrialStorage.size(name);
        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(title("确认恢复"));
        src.sendSystemMessage(Component.literal("  将在原位置恢复区域 '" + name + "' 的 " + count + " 个方块"));
        src.sendSystemMessage(Component.literal("  ⚠ 此操作不可撤销"));
        src.sendSystemMessage(Component.literal("")
                .append(warnBtn(" [确认恢复] ", TK + "restore " + name))
                .append(Component.literal("  "))
                .append(btn(" [取消] ", TK + "ui")));
        return 1;
    }

    public static int confirmDelete(CommandContext<CommandSourceStack> ctx, String name) {
        var src = ctx.getSource();
        int count = TrialStorage.size(name);
        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(title("确认删除"));
        src.sendSystemMessage(Component.literal("  将永久删除区域 '" + name + "' 的 " + count + " 个方块记录"));
        src.sendSystemMessage(Component.literal("  ⚠ 删除后无法恢复"));
        src.sendSystemMessage(Component.literal("")
                .append(warnBtn(" [确认删除] ", TK + "clear " + name))
                .append(Component.literal("  "))
                .append(btn(" [取消] ", TK + "ui")));
        return 1;
    }

    public static int confirmClearAll(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        int count = TrialStorage.totalStored();
        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(title("确认清空"));
        src.sendSystemMessage(Component.literal("  将删除全部 " + TrialStorage.getAllRegions().size() + " 个区域、共 " + count + " 个方块记录"));
        src.sendSystemMessage(Component.literal("  ⚠ 此操作不可撤销"));
        src.sendSystemMessage(Component.literal("")
                .append(warnBtn(" [确认清空] ", TK + "clear"))
                .append(Component.literal("  "))
                .append(btn(" [取消] ", TK + "ui")));
        return 1;
    }

    // ═══════════════════════════════════════════════════════════
    //  UI component helpers
    // ═══════════════════════════════════════════════════════════

    private static MutableComponent title(String text) {
        return Component.literal("  ==== " + text + " ====")
                .withStyle(s -> s.withColor(TextColor.fromRgb(0xFFAA00)).withBold(true));
    }

    private static MutableComponent subtitle(String text) {
        return Component.literal("  " + text)
                .withStyle(s -> s.withColor(TextColor.fromRgb(0xFFCC66)));
    }

    /** Click-to-run button */
    private static MutableComponent btn(String label, String cmd) {
        return Component.literal(label)
                .withStyle(s -> s
                        .withClickEvent(new ClickEvent.RunCommand(cmd))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("执行: " + cmd)))
                        .withColor(TextColor.fromRgb(0x55FFFF)));
    }

    /** Click-to-suggest button (fills chat bar) */
    private static MutableComponent suggestBtn(String label, String cmd) {
        return Component.literal(label)
                .withStyle(s -> s
                        .withClickEvent(new ClickEvent.SuggestCommand(cmd))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("输入: " + cmd)))
                        .withColor(TextColor.fromRgb(0x55FF55)));
    }

    /** Back button */
    private static MutableComponent back(String cmd) {
        return btn(" [返回] ", cmd);
    }

    /** Danger button (red, for destructive actions) */
    private static MutableComponent warnBtn(String label, String cmd) {
        return Component.literal(label)
                .withStyle(s -> s
                        .withClickEvent(new ClickEvent.RunCommand(cmd))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("危险操作: " + cmd)))
                        .withColor(TextColor.fromRgb(0xFF5555)));
    }
}
