package net.maesierra.adventOfCode2024.solutions.day7;

import org.junit.jupiter.api.Test;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day7Test {

    @Test
    void  testPart1() {
        String expected = "3749";
        assertThat(part1(new Day7(), "input_7"), equalTo(expected));
    }

    @Test
    void  testPart2() {
        String expected = "11387";
        assertThat(part2(new Day7(), "input_7"), equalTo(expected));
    }
}