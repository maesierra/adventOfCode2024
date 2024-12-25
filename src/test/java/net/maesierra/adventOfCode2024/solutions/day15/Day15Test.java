package net.maesierra.adventOfCode2024.solutions.day15;

import org.junit.jupiter.api.Test;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day15Test {

    @Test
    void  testPart1() {
        assertThat(part1(new Day15(), "input_15_2"), equalTo("10092"));
        assertThat(part1(new Day15(), "input_15_1"), equalTo("2028"));
    }

    @Test
    void  testPart2() {
        assertThat(part2(new Day15(), "input_15_2"), equalTo("9021"));
        assertThat(part2(new Day15(), "input_15_3"), equalTo("618"));
    }
}