package net.maesierra.adventOfCode2024.testUtils;

import net.maesierra.adventOfCode2024.Runner;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

public class TestHelper {

    public static String runWithInput(String name, Function<InputStream, String> run) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream input = classLoader.getResourceAsStream(name)) {
            return run.apply(input);
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }
    }
    public static String part1(Runner.Solution solution, String name, String...params) {
        return runWithInput(name, (input) -> solution.part1(input, params));
    }

    public static String part2(Runner.Solution solution, String name, String...params) {
        return runWithInput(name, (input) -> solution.part2(input, params));
    }


}
