package cn.hycer.allay.cbm.command.handler;

import cn.hycer.allay.config.AllayConfig;
import cn.hycer.allay.cbm.data.BotDataManager;
import cn.hycer.allay.cbm.model.BotGroup;
import cn.hycer.allay.cbm.model.BotPreset;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;

public final class ChatInterface {

    private static final String ALLAY_CBOT = "allay cbot ";

    private ChatInterface() {}

    public static int showMainMenu(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(title(Component.literal("Allay CarpetBotManager")));
        src.sendSystemMessage(Component.literal("")
                .append(btn(Component.translatableWithFallback("allay.carpetbotmanager.ui.btn_bot_list", " [Bot 列表] "), ALLAY_CBOT + "ui bots"))
                .append(Component.literal("  "))
                .append(btn(Component.translatableWithFallback("allay.carpetbotmanager.ui.btn_group_list", " [分组列表] "), ALLAY_CBOT + "ui groups"))
                .append(Component.literal("  "))
                .append(btn(Component.translatableWithFallback("allay.carpetbotmanager.ui.btn_autoload", " [自动加载] "), ALLAY_CBOT + "ui autoload")));
        src.sendSystemMessage(Component.literal("")
                .append(btn(Component.translatableWithFallback("allay.carpetbotmanager.ui.btn_add_bot", " [新增 Bot] "), ALLAY_CBOT + "ui add"))
                .append(Component.literal("  "))
                .append(btn(Component.translatableWithFallback("allay.carpetbotmanager.ui.btn_batch", " [批量操作] "), ALLAY_CBOT + "ui batch"))
                .append(Component.literal("  "))
                .append(btn(Component.translatableWithFallback("allay.carpetbotmanager.ui.btn_help", " [帮助] "), ALLAY_CBOT + "help")));
        return 1;
    }

    public static int showBotList(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        BotDataManager dm = BotDataManager.getInstance();
        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(title(Component.translatableWithFallback("allay.carpetbotmanager.ui.bot_list", "Bot 列表")));
        if (dm.getAllBotPresets().isEmpty()) {
            src.sendSystemMessage(Component.translatableWithFallback("allay.carpetbotmanager.ui.no_bots", "  暂无已保存的 Bot，使用 /allay cbot add <玩家名> 添加"));
        } else for (BotPreset b : dm.getAllBotPresets()) {
            String d = b.getDescription() != null && !b.getDescription().isEmpty()
                    ? " - " + b.getDescription() : "";
            src.sendSystemMessage(Component.literal("  " + b.getName() + d));
            src.sendSystemMessage(Component.literal("    ")
                    .append(btn(Component.translatableWithFallback("allay.carpetbotmanager.ui.btn_spawn", "[召唤]"), ALLAY_CBOT + "load " + b.getName()))
                    .append(Component.literal(" "))
                    .append(btn(Component.translatableWithFallback("allay.carpetbotmanager.ui.btn_delete", "[删除]"), ALLAY_CBOT + "remove " + b.getName())));
        }
        src.sendSystemMessage(Component.literal("").append(back()));
        return 1;
    }

    public static int showGroupList(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        BotDataManager dm = BotDataManager.getInstance();
        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(title(Component.translatableWithFallback("allay.carpetbotmanager.ui.group_list", "分组列表")));
        if (dm.getAllBotGroups().isEmpty()) {
            src.sendSystemMessage(Component.translatableWithFallback("allay.carpetbotmanager.ui.no_groups", "  暂无分组，使用 /allay cbot group add ... 创建"));
        } else for (BotGroup g : dm.getAllBotGroups()) {
            String d = g.getDescription() != null && !g.getDescription().isEmpty()
                    ? " - " + g.getDescription() : "";
            src.sendSystemMessage(Component.literal("  " + g.getName() + d + " [" + String.join(", ", g.getBots()) + "]"));
            src.sendSystemMessage(Component.literal("    ")
                    .append(btn(Component.translatableWithFallback("allay.carpetbotmanager.ui.btn_load", "[加载]"), ALLAY_CBOT + "group load " + g.getName()))
                    .append(Component.literal(" "))
                    .append(btn(Component.translatableWithFallback("allay.carpetbotmanager.ui.btn_delete", "[删除]"), ALLAY_CBOT + "group remove " + g.getName())));
        }
        src.sendSystemMessage(Component.literal("").append(back()));
        return 1;
    }

    public static int showAutoLoad(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        AllayConfig cfg = AllayConfig.getInstance();
        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(title(Component.translatableWithFallback("allay.carpetbotmanager.ui.autoload_settings", "自动加载设置")));
        src.sendSystemMessage(Component.translatableWithFallback("allay.carpetbotmanager.ui.auto_bots", "  自动加载 Bot："));
        if (cfg.getAutoLoadBots().isEmpty()) {
            src.sendSystemMessage(Component.translatableWithFallback("allay.carpetbotmanager.ui.no_autoload", "    （无）"));
        } else for (String n : cfg.getAutoLoadBots()) {
            src.sendSystemMessage(Component.literal("    " + n + "  ")
                    .append(btn(Component.translatableWithFallback("allay.carpetbotmanager.ui.btn_remove", "[移除]"), ALLAY_CBOT + "autoload remove " + n)));
        }
        src.sendSystemMessage(Component.translatableWithFallback("allay.carpetbotmanager.ui.auto_groups", "  自动加载分组："));
        if (cfg.getAutoLoadGroups().isEmpty()) {
            src.sendSystemMessage(Component.translatableWithFallback("allay.carpetbotmanager.ui.no_autoload", "    （无）"));
        } else for (String n : cfg.getAutoLoadGroups()) {
            src.sendSystemMessage(Component.literal("    " + n + "  ")
                    .append(btn(Component.translatableWithFallback("allay.carpetbotmanager.ui.btn_remove", "[移除]"), ALLAY_CBOT + "group autoload remove " + n)));
        }
        src.sendSystemMessage(Component.literal("").append(back()));
        return 1;
    }

    public static int showAddHelp(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(title(Component.translatableWithFallback("allay.carpetbotmanager.ui.add_bot", "新增 Bot")));
        src.sendSystemMessage(Component.translatableWithFallback("allay.carpetbotmanager.ui.usage", "  用法: /allay cbot add <玩家名> [描述]"));
        src.sendSystemMessage(Component.translatableWithFallback("allay.carpetbotmanager.ui.add_tip1", "  - 玩家必须在线"));
        src.sendSystemMessage(Component.translatableWithFallback("allay.carpetbotmanager.ui.add_tip2", "  - 名称必须以 'bot_' 开头"));
        src.sendSystemMessage(Component.translatableWithFallback("allay.carpetbotmanager.ui.add_example", "  示例: /allay cbot add bot_miner 挖矿机器人"));
        src.sendSystemMessage(Component.literal("").append(back()));
        return 1;
    }

    public static int showBatchMenu(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        String pre = ALLAY_CBOT + "batch ";
        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(title(Component.translatableWithFallback("allay.carpetbotmanager.ui.batch_menu", "批量操作")));
        src.sendSystemMessage(Component.translatableWithFallback("allay.carpetbotmanager.ui.batch_hint",
                "  用法: /allay cbot batch <前缀> <起始> <结束> <动作>"));
        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(Component.translatableWithFallback("allay.carpetbotmanager.ui.batch_spawn", "  ▸ 召唤"));
        src.sendSystemMessage(Component.literal("    ")
                .append(suggest(Component.translatableWithFallback("allay.carpetbotmanager.ui.batch_btn_spawn", "[召唤]"), pre + "<前缀> <起始> <结束> spawn"))
                .append(Component.literal("  "))
                .append(suggest(Component.translatableWithFallback("allay.carpetbotmanager.ui.batch_btn_spawn_at", "[召唤 at]"), pre + "<前缀> <起始> <结束> spawn at ~ ~ ~")));
        src.sendSystemMessage(Component.translatableWithFallback("allay.carpetbotmanager.ui.batch_manage", "  ▸ 管理"));
        src.sendSystemMessage(Component.literal("    ")
                .append(suggest(Component.translatableWithFallback("allay.carpetbotmanager.ui.batch_btn_save", "[保存]"), pre + "<前缀> <起始> <结束> save"))
                .append(Component.literal("  "))
                .append(suggest(Component.translatableWithFallback("allay.carpetbotmanager.ui.batch_btn_kill", "[下线]"), pre + "<前缀> <起始> <结束> kill"))
                .append(Component.literal("  "))
                .append(suggest(Component.translatableWithFallback("allay.carpetbotmanager.ui.batch_btn_sneak", "[潜行]"), pre + "<前缀> <起始> <结束> sneak")));
        src.sendSystemMessage(Component.translatableWithFallback("allay.carpetbotmanager.ui.batch_interact", "  ▸ 交互"));
        src.sendSystemMessage(Component.literal("    ")
                .append(suggest(Component.translatableWithFallback("allay.carpetbotmanager.ui.batch_btn_use", "[使用]"), pre + "<前缀> <起始> <结束> use"))
                .append(Component.literal("  "))
                .append(suggest(Component.translatableWithFallback("allay.carpetbotmanager.ui.batch_btn_use_cont", "[持续使用]"), pre + "<前缀> <起始> <结束> use continuous"))
                .append(Component.literal("  "))
                .append(suggest(Component.translatableWithFallback("allay.carpetbotmanager.ui.batch_btn_use_int", "[间隔使用]"), pre + "<前缀> <起始> <结束> use interval <tick>")));
        src.sendSystemMessage(Component.literal("    ")
                .append(suggest(Component.translatableWithFallback("allay.carpetbotmanager.ui.batch_btn_attack", "[攻击]"), pre + "<前缀> <起始> <结束> attack"))
                .append(Component.literal("  "))
                .append(suggest(Component.translatableWithFallback("allay.carpetbotmanager.ui.batch_btn_attack_cont", "[持续攻击]"), pre + "<前缀> <起始> <结束> attack continuous"))
                .append(Component.literal("  "))
                .append(suggest(Component.translatableWithFallback("allay.carpetbotmanager.ui.batch_btn_attack_int", "[间隔攻击]"), pre + "<前缀> <起始> <结束> attack interval <tick>")));
        src.sendSystemMessage(Component.literal("").append(back()));
        return 1;
    }

    public static int showAutoLoadAddBot(CommandContext<CommandSourceStack> ctx, String name) {
        CommandSourceStack src = ctx.getSource();
        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(Component.translatableWithFallback("allay.carpetbotmanager.ui.confirm_autoload_bot", "  将 '%s' 加入自动加载？", name));
        src.sendSystemMessage(Component.literal("")
                .append(btn(Component.translatableWithFallback("allay.carpetbotmanager.ui.btn_yes", " [确认] "), ALLAY_CBOT + "autoload add " + name))
                .append(Component.literal("  "))
                .append(btn(Component.translatableWithFallback("allay.carpetbotmanager.ui.btn_no", " [取消] "), ALLAY_CBOT + "ui bots")));
        return 1;
    }

    public static int showAutoLoadAddGroup(CommandContext<CommandSourceStack> ctx, String name) {
        CommandSourceStack src = ctx.getSource();
        src.sendSystemMessage(Component.literal(""));
        src.sendSystemMessage(Component.translatableWithFallback("allay.carpetbotmanager.ui.confirm_autoload_group", "  将分组 '%s' 加入自动加载？", name));
        src.sendSystemMessage(Component.literal("")
                .append(btn(Component.translatableWithFallback("allay.carpetbotmanager.ui.btn_yes", " [确认] "), ALLAY_CBOT + "group autoload add " + name))
                .append(Component.literal("  "))
                .append(btn(Component.translatableWithFallback("allay.carpetbotmanager.ui.btn_no", " [取消] "), ALLAY_CBOT + "ui groups")));
        return 1;
    }

    private static MutableComponent title(MutableComponent c) {
        return Component.literal("  ==== ").append(c).append(Component.literal(" ===="))
                .withStyle(s -> s.withColor(TextColor.fromRgb(0xFFAA00)).withBold(true));
    }

    private static MutableComponent btn(MutableComponent label, String cmd) {
        return label
                .withStyle(s -> s
                        .withClickEvent(new ClickEvent.RunCommand(cmd))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal(cmd)))
                        .withColor(TextColor.fromRgb(0x55FFFF)));
    }

    private static MutableComponent suggest(MutableComponent label, String cmd) {
        return label
                .withStyle(s -> s
                        .withClickEvent(new ClickEvent.SuggestCommand("/" + cmd))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal(cmd)))
                        .withColor(TextColor.fromRgb(0x55FF55)));
    }

    private static MutableComponent back() {
        return btn(Component.translatableWithFallback("allay.carpetbotmanager.ui.btn_back", " [返回] "), ALLAY_CBOT + "ui");
    }
}
