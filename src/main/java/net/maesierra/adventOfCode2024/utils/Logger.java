package net.maesierra.adventOfCode2024.utils;

import java.util.function.Supplier;

public class Logger {

    public enum Level {
        DEBUG,
        INFO,
        NONE
    }

    private static Level level = Level.DEBUG;

    public static void info(String message, Object... params) {
        switch (level) {
            case INFO -> System.out.println(message.formatted(params));
            case DEBUG, NONE -> {}
        }
    }
    public static void debug(String message, Object... params) {
        switch (level) {
            case DEBUG ->  System.out.println(message.formatted(params));
            case INFO, NONE ->  {}
        }
    }

    public static void debug(String message, Supplier<Object[]> params) {
        switch (level) {
            case DEBUG ->  System.out.println(message.formatted(params.get()));
            case INFO, NONE ->  {}
        }
    }

    public static void setLevel(Level level) {
        Logger.level = level;
    }

    public static Level getLevel() {
        return level;
    }
}
