package cn.hycer.allay.asb.command;

import cn.hycer.allay.asb.AdvancedScoreboardModule;
import cn.hycer.allay.config.AllayConfig;
import cn.hycer.allay.asb.config.ScoreboardItem;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * /allay asb hide <displayName> — per-player toggle, no permission needed.
 */
public class HideCommand {

    static final SuggestionProvider<CommandSourceStack> DISPLAY_NAME_SUGGESTIONS =
        (context, builder) -> {
            for (ScoreboardItem item : AllayConfig.getInstance().getScoreboards()) {
                builder.suggest(item.getDisplayName());
            }
            return builder.buildFuture();
        };

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return literal("hide")
            .then(argument("displayName", StringArgumentType.greedyString())
                .suggests(DISPLAY_NAME_SUGGESTIONS)
                .executes(context -> {
                    var player = context.getSource().getPlayerOrException();
                    String displayName = StringArgumentType.getString(context, "displayName");
                    ScoreboardItem item = AllayConfig.getInstance().getScoreboards().stream()
                        .filter(sb -> displayName.equals(sb.getDisplayName()))
                        .findFirst().orElse(null);

                    if (item == null) {
                        context.getSource().sendFailure(Component.literal("未找到榜单: " + displayName));
                        return 0;
                    }

                    boolean nowHidden = AdvancedScoreboardModule.togglePlayerHidden(
                            player.getUUID(), item.getInternalName());
                    context.getSource().sendSuccess(
                        () -> Component.literal(item.getDisplayName()
                                + (nowHidden ? " 已隐藏" : " 已显示")), false);
                    return 1;
                }));
    }
}
