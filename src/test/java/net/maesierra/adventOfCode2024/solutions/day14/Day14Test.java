package net.maesierra.adventOfCode2024.solutions.day14;

import org.junit.jupiter.api.Test;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day14Test {

    @Test
    void  testPart1() {
        String expected = "12";
        assertThat(part1(new Day14(), "input_14", "11", "7"), equalTo(expected));
    }
}