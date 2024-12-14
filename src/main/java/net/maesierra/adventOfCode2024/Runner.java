package net.maesierra.adventOfCode2024;

import net.maesierra.adventOfCode2024.utils.Logger;

import java.awt.*;
import java.io.InputStream;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Runner {
    public static final Pattern DAY_PATTERN = Pattern.compile("^day(\\d+)");
    public static final Pattern PART_PATTERN = Pattern.compile("^part([12])$");

    public interface Solution {
        String part1(InputStream input, String...params);
        String part2(InputStream input, String...params);
        default Consumer<Graphics2D> visualisePart1(InputStream input, String...params) {
            return c -> {};
        }
        default Consumer<Graphics2D> visualisePart2(InputStream input, String...params) {
            return c -> {};
        }
    }

    protected record RunData(Solution solution, String day, String part) {

    }

    protected static void run(String[] args, BiConsumer<RunData, InputStream> runWithInput) throws Exception {
        if (args.length < 2) {
            showUsage();
        }
        if (Boolean.parseBoolean(System.getProperty("debug", "false"))) {
            Logger.setLevel(Logger.Level.DEBUG);
        } else {
            Logger.setLevel(Logger.Level.INFO);
        }
        String day = parseArgument(args[0], DAY_PATTERN);
        String part = parseArgument(args[1], PART_PATTERN);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream input = classLoader.getResourceAsStream("input_%s".formatted(day))) {
            if (input == null) {
                System.err.println("input_% not found".formatted(day));
            }
            Runner.Solution solution = (Runner.Solution) Class.forName("net.maesierra.adventOfCode2024.solutions.day%s.Day%s".formatted(day, day)).getDeclaredConstructor().newInstance();
            runWithInput.accept(new RunData(solution, day, part), input);
        }

    }

    private static void showUsage() {
        System.err.println("Usage java -jar <file> dayN part1|part2");
    }

    private static String parseArgument(String arg, Pattern pattern) {
        Matcher matcher = pattern.matcher(arg);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            showUsage();
            System.exit(-1);
        }
        return "";
    }
}
