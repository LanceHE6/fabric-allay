package cn.hycer.allay.cbm.command;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.network.chat.Component;

public final class CommandExceptions {

    public static final SimpleCommandExceptionType NOT_BOT_PREFIX =
            new SimpleCommandExceptionType(Component.translatableWithFallback(
                    "carpetbotmanager.error.not_bot_prefix", "玩家名必须以配置的 bot 前缀开头才能注册为 bot。"));
    public static final SimpleCommandExceptionType BOT_NOT_FOUND =
            new SimpleCommandExceptionType(Component.translatableWithFallback(
                    "carpetbotmanager.error.bot_not_found", "未找到该 bot 预设。"));
    public static final SimpleCommandExceptionType BOT_ALREADY_EXISTS =
            new SimpleCommandExceptionType(Component.translatableWithFallback(
                    "carpetbotmanager.error.bot_already_exists", "该名称的 bot 预设已存在。"));
    public static final SimpleCommandExceptionType GROUP_NOT_FOUND =
            new SimpleCommandExceptionType(Component.translatableWithFallback(
                    "carpetbotmanager.error.group_not_found", "未找到该 bot 组。"));
    public static final SimpleCommandExceptionType GROUP_ALREADY_EXISTS =
            new SimpleCommandExceptionType(Component.translatableWithFallback(
                    "carpetbotmanager.error.group_already_exists", "该名称的 bot 组已存在。"));
    public static final SimpleCommandExceptionType BOTS_NOT_FOUND_FOR_GROUP =
            new SimpleCommandExceptionType(Component.translatableWithFallback(
                    "carpetbotmanager.error.bots_not_found_for_group", "未找到任何指定的 bot。请先添加 bot 预设。"));
    public static final SimpleCommandExceptionType ALREADY_IN_AUTOLOAD =
            new SimpleCommandExceptionType(Component.translatableWithFallback(
                    "carpetbotmanager.error.already_in_autoload", "该 bot 已在自动加载列表中。"));
    public static final SimpleCommandExceptionType NOT_IN_AUTOLOAD =
            new SimpleCommandExceptionType(Component.translatableWithFallback(
                    "carpetbotmanager.error.not_in_autoload", "该 bot 不在自动加载列表中。"));
    public static final SimpleCommandExceptionType GROUP_ALREADY_IN_AUTOLOAD =
            new SimpleCommandExceptionType(Component.translatableWithFallback(
                    "carpetbotmanager.error.group_already_in_autoload", "该组已在自动加载列表中。"));
    public static final SimpleCommandExceptionType GROUP_NOT_IN_AUTOLOAD =
            new SimpleCommandExceptionType(Component.translatableWithFallback(
                    "carpetbotmanager.error.group_not_in_autoload", "该组不在自动加载列表中。"));

    private CommandExceptions() {}
}
