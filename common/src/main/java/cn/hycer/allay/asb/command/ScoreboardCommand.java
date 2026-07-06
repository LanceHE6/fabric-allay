package cn.hycer.allay.asb.command;

import cn.hycer.allay.AllayConfig;
import cn.hycer.allay.asb.config.ScoreboardItem;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.Map;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ScoreboardCommand {

    static final SuggestionProvider<CommandSourceStack> DISPLAY_NAME_SUGGESTIONS =
        (context, builder) -> {
            for (ScoreboardItem item : AllayConfig.getInstance().getScoreboards()) {
                builder.suggest(item.getDisplayName());
            }
            return builder.buildFuture();
        };

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return literal("scoreboard")
            .then(argument("displayName", StringArgumentType.greedyString())
                .suggests(DISPLAY_NAME_SUGGESTIONS)
                .executes(context -> {
                    String displayName = StringArgumentType.getString(context, "displayName");
                    ScoreboardItem item = AllayConfig.getInstance().getScoreboards().stream()
                        .filter(sb -> displayName.equals(sb.getDisplayName()))
                        .findFirst().orElse(null);

                    if (item == null) {
                        context.getSource().sendFailure(Component.literal("未找到榜单: " + displayName));
                        return 0;
                    }

                    context.getSource().sendSuccess(
                        () -> Component.literal(AllayConfig.getInstance().getFormattedDisplayName(item)), false);

                    if (item.getData().isEmpty()) {
                        context.getSource().sendSuccess(() -> Component.literal("暂无数据"), false);
                    } else {
                        item.getData().entrySet().stream()
                            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                            .forEach(entry -> context.getSource().sendSuccess(
                                () -> Component.literal(entry.getKey() + " - " + entry.getValue()), false));
                    }
                    return 1;
                }));
    }
}
