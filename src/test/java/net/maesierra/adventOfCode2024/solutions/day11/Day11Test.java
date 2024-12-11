package net.maesierra.adventOfCode2024.solutions.day11;

import org.junit.jupiter.api.Test;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day11Test {

    @Test
    void  testPart1() {
        String expected = "55312";
        assertThat(part1(new Day11(), "input_11"), equalTo(expected));
    }

    @Test
    void  testPart2() {
        String expected = "?????";
        assertThat(part2(new Day11(), "input_11"), equalTo(expected));
    }
}