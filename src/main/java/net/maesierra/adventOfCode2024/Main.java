package net.maesierra.adventOfCode2024;

import net.maesierra.adventOfCode2024.utils.Logger;
import net.maesierra.adventOfCode2024.utils.Logger.Level;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static final Pattern DAY_PATTERN = Pattern.compile("^day(\\d+)");
    public static final Pattern PART_PATTERN = Pattern.compile("^part([12])$");

    public interface Solution {
        String part1(InputStream input, String...params);
        String part2(InputStream input, String...params);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            showUsage();
        }
        if (Boolean.parseBoolean(System.getProperty("debug", "false"))) {
            Logger.setLevel(Level.DEBUG);
        } else {
            Logger.setLevel(Level.INFO);
        }
        String day = parseArgument(args[0], DAY_PATTERN);
        String part = parseArgument(args[1], PART_PATTERN);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream input = classLoader.getResourceAsStream("input_%s".formatted(day))) {
            if (input == null) {
                System.err.println("input_% not found".formatted(day));
            }
            Solution solution = (Solution) Class.forName("net.maesierra.adventOfCode2024.solutions.day%s.Day%s".formatted(day, day)).getDeclaredConstructor().newInstance();
            String result = switch (part) {
                case "1" -> solution.part1(input);
                case "2" -> solution.part2(input);
                default -> throw new IllegalStateException("Unexpected value: " + part);
            };
            System.out.println(result);
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