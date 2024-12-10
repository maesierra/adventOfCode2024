package net.maesierra.adventOfCode2024.solutions.day10;

import org.junit.jupiter.api.Test;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day10Test {

    @Test
    void  testPart1() {
        String expected = "36";
        assertThat(part1(new Day10(), "input_10"), equalTo(expected));
    }

    @Test
    void  testPart2() {
        String expected = "81";
        assertThat(part2(new Day10(), "input_10"), equalTo(expected));
    }
}