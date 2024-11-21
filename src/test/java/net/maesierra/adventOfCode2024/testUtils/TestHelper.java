package net.maesierra.adventOfCode2024.testUtils;

import net.maesierra.adventOfCode2024.Main;

import java.io.IOException;
import java.io.InputStream;

public class TestHelper {
    public static String part1(Main.Solution solution, String name, String...params) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream input = classLoader.getResourceAsStream(name)) {
            return solution.part1(input, params);
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }
    }

    public static String part2(Main.Solution solution, String name, String...params) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream input = classLoader.getResourceAsStream(name)) {
            return solution.part2(input, params);
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }
    }


}
