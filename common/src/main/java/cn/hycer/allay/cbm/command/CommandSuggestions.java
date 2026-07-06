package cn.hycer.allay.cbm.command;

import cn.hycer.allay.AllayConfig;
import cn.hycer.allay.cbm.data.BotDataManager;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import java.util.Locale;

public final class CommandSuggestions {

    public static final SuggestionProvider<CommandSourceStack> BOT_NAME_SUGGESTIONS =
            (ctx, b) -> {
                String r = b.getRemaining().toLowerCase(Locale.ROOT);
                BotDataManager.getInstance().getAllBotPresets().stream()
                        .filter(p -> p.getName().toLowerCase(Locale.ROOT).contains(r))
                        .forEach(p -> b.suggest(p.getName(),
                                p.getDescription() != null && !p.getDescription().isEmpty()
                                        ? Component.literal(p.getDescription()) : null));
                return b.buildFuture();
            };

    public static final SuggestionProvider<CommandSourceStack> GROUP_NAME_SUGGESTIONS =
            (ctx, b) -> {
                String r = b.getRemaining().toLowerCase(Locale.ROOT);
                BotDataManager.getInstance().getAllBotGroups().stream()
                        .filter(g -> g.getName().toLowerCase(Locale.ROOT).contains(r))
                        .forEach(g -> b.suggest(g.getName()));
                return b.buildFuture();
            };

    public static final SuggestionProvider<CommandSourceStack> AUTOLOAD_BOT_SUGGESTIONS =
            (ctx, b) -> {
                String r = b.getRemaining().toLowerCase(Locale.ROOT);
                AllayConfig.getInstance().getAutoLoadBots().stream()
                        .filter(s -> s.toLowerCase(Locale.ROOT).contains(r))
                        .forEach(s -> b.suggest(s));
                return b.buildFuture();
            };

    public static final SuggestionProvider<CommandSourceStack> AUTOLOAD_GROUP_SUGGESTIONS =
            (ctx, b) -> {
                String r = b.getRemaining().toLowerCase(Locale.ROOT);
                AllayConfig.getInstance().getAutoLoadGroups().stream()
                        .filter(s -> s.toLowerCase(Locale.ROOT).contains(r))
                        .forEach(s -> b.suggest(s));
                return b.buildFuture();
            };

    private CommandSuggestions() {}
}
