package net.maesierra.adventOfCode2024.solutions.day5;

import org.junit.jupiter.api.Test;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day5Test {

    @Test
    void  testPart1() {
        String expected = "143";
        assertThat(part1(new Day5(), "input_5"), equalTo(expected));
    }

    @Test
    void  testPart2() {
        String expected = "123";
        assertThat(part2(new Day5(), "input_5"), equalTo(expected));
    }
}