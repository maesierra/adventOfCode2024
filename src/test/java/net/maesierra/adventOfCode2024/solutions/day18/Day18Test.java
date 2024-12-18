package net.maesierra.adventOfCode2024.solutions.day18;

import org.junit.jupiter.api.Test;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day18Test {

    @Test
    void  testPart1() {
        String expected = "22";
        assertThat(part1(new Day18(), "input_18", "7", "12"), equalTo(expected));
    }

    @Test
    void  testPart2() {
        String expected = "6,1";
        assertThat(part2(new Day18(), "input_18", "7", "12"), equalTo(expected));
    }
}