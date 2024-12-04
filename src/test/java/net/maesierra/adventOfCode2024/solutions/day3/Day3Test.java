package net.maesierra.adventOfCode2024.solutions.day3;

import org.junit.jupiter.api.Test;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day3Test {

    @Test
    void  testPart1() {
        String expected = "161";
        assertThat(part1(new Day3(), "input_3"), equalTo(expected));
    }

    @Test
    void  testPart2() {
        String expected = "48";
        assertThat(part2(new Day3(), "input_3_2"), equalTo(expected));
    }
}