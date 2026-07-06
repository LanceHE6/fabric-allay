package cn.hycer.allay.command;

import cn.hycer.allay.asb.command.NotDisplayCommand;
import cn.hycer.allay.asb.command.ScoreboardCommand;
import cn.hycer.allay.asb.command.SetCommand;
import cn.hycer.allay.cbm.command.handler.*;
import cn.hycer.allay.cbm.command.tree.*;
import cn.hycer.allay.tk.TrialKeeperCommand;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class AllayCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // ── Sub-trees ──────────────────────────────────────

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

            // ── /allay root + standalone aliases ───────────────

            LiteralCommandNode<CommandSourceStack> allayNode = dispatcher.register(
                literal("allay")
                    .executes(AllayChatInterface::showMainMenu)
                    .then(literal("help").executes(AllayChatInterface::showHelp))
                    .then(literal("config").executes(AllayChatInterface::showConfig))
                    .then(asb)
                    .then(cbot)
                    .then(tk)
            );

            // Standalone redirects so /asb /cbot /trialkeeper also work
            dispatcher.register(literal("asb").redirect(
                    dispatcher.getRoot().getChild("allay").getChild("asb")));
            dispatcher.register(literal("cbot").redirect(
                    dispatcher.getRoot().getChild("allay").getChild("cbot")));
            dispatcher.register(literal("trialkeeper").redirect(
                    dispatcher.getRoot().getChild("allay").getChild("tk")));
        });
    }
}
