package net.maesierra.adventOfCode2024.solutions.day2;

import org.junit.jupiter.api.Test;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day2Test {

    @Test
    void  testPart1() {
        String expected = "2";
        assertThat(part1(new Day2(), "input_2"), equalTo(expected));
    }

    @Test
    void  testPart2() {
        String expected = "4";
        assertThat(part2(new Day2(), "input_2"), equalTo(expected));
    }
}