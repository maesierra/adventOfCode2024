package net.maesierra.adventOfCode2024.solutions.day0;

import net.maesierra.adventOfCode2024.Main;

import java.io.InputStream;

import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsString;

public class Day0 implements Main.Solution {

    @Override
    public String part1(InputStream input, String... params) {
        return inputAsString(input).toUpperCase();
    }

    @Override
    public String part2(InputStream input, String... params) {
        return inputAsString(input).toLowerCase();
    }
}
