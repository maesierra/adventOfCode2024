package net.maesierra.adventOfCode2024.solutions.day13;

import org.junit.jupiter.api.Test;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day13Test {

    @Test
    void  testPart1() {
        String expected = "480";
        assertThat(part1(new Day13(), "input_13"), equalTo(expected));
    }
}