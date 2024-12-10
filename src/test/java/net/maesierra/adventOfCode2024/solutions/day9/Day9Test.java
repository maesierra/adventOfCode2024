package net.maesierra.adventOfCode2024.solutions.day9;

import org.junit.jupiter.api.Test;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day9Test {

    @Test
    void  testPart1() {
        String expected = "1928";
        assertThat(part1(new Day9(), "input_9"), equalTo(expected));
    }

    @Test
    void  testPart2() {
        String expected = "2858";
        assertThat(part2(new Day9(), "input_9"), equalTo(expected));
    }
}