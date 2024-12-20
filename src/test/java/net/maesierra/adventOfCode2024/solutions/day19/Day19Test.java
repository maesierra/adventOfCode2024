package net.maesierra.adventOfCode2024.solutions.day19;

import org.junit.jupiter.api.Test;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day19Test {

    @Test
    void  testPart1() {
        String expected = "6";
        assertThat(part1(new Day19(), "input_19"), equalTo(expected));
    }

    @Test
    void  testPart2() {
        String expected = "16";
        assertThat(part2(new Day19(), "input_19"), equalTo(expected));
    }
}