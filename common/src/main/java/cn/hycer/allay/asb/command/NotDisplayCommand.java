package cn.hycer.allay.asb.command;

import cn.hycer.allay.AllayConfig;
import cn.hycer.allay.asb.config.ScoreboardItem;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class NotDisplayCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return literal("notDisplay")
            .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
            .then(argument("displayName", StringArgumentType.greedyString())
                .suggests(ScoreboardCommand.DISPLAY_NAME_SUGGESTIONS)
                .executes(context -> {
                    String displayName = StringArgumentType.getString(context, "displayName");
                    ScoreboardItem item = AllayConfig.getInstance().getScoreboards().stream()
                        .filter(sb -> displayName.equals(sb.getDisplayName()))
                        .findFirst().orElse(null);

                    if (item == null) {
                        context.getSource().sendFailure(Component.literal("未找到榜单: " + displayName));
                        return 0;
                    }

                    boolean hidden = AllayConfig.getInstance().toggleScoreboardVisibility(item.getInternalName());
                    AllayConfig.getInstance().saveConfig();

                    String message = hidden
                        ? "已全局隐藏榜单: " + item.getDisplayName()
                        : "已全局显示榜单: " + item.getDisplayName();
                    context.getSource().sendSuccess(() -> Component.literal(message), false);
                    return 1;
                }));
    }
}
