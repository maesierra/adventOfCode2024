package net.maesierra.adventOfCode2024.solutions.day0;

import org.junit.jupiter.api.Test;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day0Test {

    @Test
    void  testPart1() {
        String expected = "09:30, 4H 20MIN, 13:50, 46.00€";
        assertThat(part1(new Day0(), "input_0"), equalTo(expected));
    }

    @Test
    void  testPart2() {
        String expected = "09:30, 4h 20min, 13:50, 46.00€";
        assertThat(part2(new Day0(), "input_0"), equalTo(expected));
    }
}