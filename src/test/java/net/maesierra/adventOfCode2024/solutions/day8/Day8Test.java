package net.maesierra.adventOfCode2024.solutions.day8;

import org.junit.jupiter.api.Test;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day8Test {

    @Test
    void  testPart1() {
        assertThat(part1(new Day8(), "input_8.1"), equalTo("2"));
        assertThat(part1(new Day8(), "input_8.2"), equalTo("4"));
        assertThat(part1(new Day8(), "input_8"), equalTo("14"));
    }

    @Test
    void  testPart2() {
        assertThat(part2(new Day8(), "input_8.3"), equalTo("9"));
        assertThat(part2(new Day8(), "input_8"), equalTo("34"));
    }
}