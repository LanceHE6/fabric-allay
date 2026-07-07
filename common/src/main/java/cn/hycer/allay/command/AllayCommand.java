package cn.hycer.allay.command;

import cn.hycer.allay.asb.command.NotDisplayCommand;
import cn.hycer.allay.asb.command.ScoreboardCommand;
import cn.hycer.allay.asb.command.SetCommand;
import cn.hycer.allay.cbm.command.handler.*;
import cn.hycer.allay.cbm.command.tree.*;
import cn.hycer.allay.config.AllayConfig;
import cn.hycer.allay.feature.FeatureManager;
import cn.hycer.allay.tk.TrialKeeperCommand;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class AllayCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // ── Sub-trees ────────────────────────────────

            var asb = literal("asb");
            asb.then(literal("ui").executes(AllayChatInterface::showAsbMenu));
            asb.then(SetCommand.build());
            asb.then(ScoreboardCommand.build());
            asb.then(NotDisplayCommand.build());

            var cbot = literal("cbot").executes(ChatInterface::showMainMenu);
            BotCommandTree.addNodes(cbot);
            BatchCommandTree.addNodes(cbot);
            UiCommandTree.addNodes(cbot);
            AutoLoadCommandTree.addNodes(cbot);
            GroupCommandTree.addNodes(cbot);

            var tk = literal("tk");
            tk.then(literal("ui")
                    .executes(AllayChatInterface::showTkMenu)
                    .then(literal("confirm-restore")
                            .then(argument("name", StringArgumentType.word())
                                    .executes(ctx -> AllayChatInterface.confirmRestore(
                                            ctx, StringArgumentType.getString(ctx, "name")))))
                    .then(literal("confirm-delete")
                            .then(argument("name", StringArgumentType.word())
                                    .executes(ctx -> AllayChatInterface.confirmDelete(
                                            ctx, StringArgumentType.getString(ctx, "name")))))
                    .then(literal("confirm-clear-all")
                            .executes(AllayChatInterface::confirmClearAll)));
            tk.then(TrialKeeperCommand.buildRemove());
            tk.then(TrialKeeperCommand.buildRestore());
            tk.then(TrialKeeperCommand.buildList());
            tk.then(TrialKeeperCommand.buildClear());

            // ── Feature toggles ──────────────────────────

            // Temporary toggle: /allay fragileObsidian true|false
            var fragileObsidian = literal("fragileObsidian")
                    .then(argument("value", BoolArgumentType.bool())
                            .executes(ctx -> featureToggle(ctx, "fragileObsidian",
                                    "易碎黑曜石", BoolArgumentType.getBool(ctx, "value"))));

            var superTNT = literal("superTNT")
                    .then(argument("value", BoolArgumentType.bool())
                            .executes(ctx -> featureToggle(ctx, "superTNT",
                                    "超级TNT", BoolArgumentType.getBool(ctx, "value"))));

            // Permanent: /allay setDefault <rule> true|false
            var setDefault = literal("setDefault")
                    .then(argument("rule", StringArgumentType.word())
                            .then(argument("value", BoolArgumentType.bool())
                                    .executes(ctx -> setFeatureDefault(ctx,
                                            StringArgumentType.getString(ctx, "rule"),
                                            BoolArgumentType.getBool(ctx, "value")))));

            // Remove permanent: /allay removeDefault <rule>
            var removeDefault = literal("removeDefault")
                    .then(argument("rule", StringArgumentType.word())
                            .executes(ctx -> removeFeatureDefault(ctx,
                                    StringArgumentType.getString(ctx, "rule"))));

            LiteralCommandNode<CommandSourceStack> allayNode = dispatcher.register(
                literal("allay")
                    .executes(AllayChatInterface::showMainMenu)
                    .then(literal("help").executes(AllayChatInterface::showHelp))
                    .then(literal("config").executes(AllayChatInterface::showConfig))
                    .then(fragileObsidian)
                    .then(superTNT)
                    .then(setDefault)
                    .then(removeDefault)
                    .then(asb)
                    .then(cbot)
                    .then(tk)
            );

            dispatcher.register(literal("asb").redirect(
                    dispatcher.getRoot().getChild("allay").getChild("asb")));
            dispatcher.register(literal("cbot").redirect(
                    dispatcher.getRoot().getChild("allay").getChild("cbot")));
            dispatcher.register(literal("trialkeeper").redirect(
                    dispatcher.getRoot().getChild("allay").getChild("tk")));
        });
    }

    // ── Feature toggle helpers ──────────────────────────────────

    /** Temporary toggle: changes runtime only, shows "永久更改？" clickable link. */
    private static int featureToggle(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx,
                                      String ruleName, String displayName, boolean value) {
        var src = ctx.getSource();
        var mgr = FeatureManager.getInstance();

        if ("fragileObsidian".equals(ruleName)) mgr.setFragileObsidian(value);
        else if ("superTNT".equals(ruleName)) mgr.setSuperTNT(value);

        String permCmd = "/allay setDefault " + ruleName + " " + value;
        MutableComponent msg = Component.literal(displayName + "已" + (value ? "开启" : "关闭") + "（重启后失效）")
                .append(Component.literal("  [永久更改？]")
                        .withStyle(s -> s
                                .withClickEvent(new ClickEvent.SuggestCommand(permCmd))
                                .withHoverEvent(new HoverEvent.ShowText(Component.literal(permCmd)))
                                .withColor(TextColor.fromRgb(0x5555FF))));

        src.sendSuccess(() -> msg, false);
        return 1;
    }

    /** Permanently set a feature default (persisted to config + updates runtime). */
    private static int setFeatureDefault(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx,
                                          String rule, boolean value) {
        var src = ctx.getSource();
        var cfg = AllayConfig.getInstance();
        var mgr = FeatureManager.getInstance();

        cfg.setFeatureDefault(rule, value);
        mgr.reloadDefaults();

        src.sendSuccess(() -> Component.literal("已将 " + rule + " 永久设为 " + value), true);
        return 1;
    }

    /** Remove a persisted default, reverting to false. */
    private static int removeFeatureDefault(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx,
                                             String rule) {
        var src = ctx.getSource();
        var cfg = AllayConfig.getInstance();
        var mgr = FeatureManager.getInstance();

        cfg.removeFeatureDefault(rule);
        mgr.reloadDefaults();

        src.sendSuccess(() -> Component.literal("已移除 " + rule + " 的永久设置，恢复为 false"), true);
        return 1;
    }
}
