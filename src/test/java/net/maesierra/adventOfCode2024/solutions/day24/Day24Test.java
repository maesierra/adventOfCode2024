package net.maesierra.adventOfCode2024.solutions.day24;

import org.junit.jupiter.api.Test;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day24Test {

    @Test
    void  testPart1() {
        assertThat(part1(new Day24(), "input_24_1"), equalTo("4"));
        assertThat(part1(new Day24(), "input_24_2"), equalTo("2024"));
    }

    @Test
    void  testPart2() {
        String expected = "09:30, 4h 20min, 13:50, 46.00€";
        assertThat(part2(new Day24(), "input_24_1"), equalTo(expected));
    }
}