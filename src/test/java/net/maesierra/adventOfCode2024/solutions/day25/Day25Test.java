package net.maesierra.adventOfCode2024.solutions.day25;

import org.junit.jupiter.api.Test;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day25Test {

    @Test
    void  testPart1() {
        String expected = "3";
        assertThat(part1(new Day25(), "input_25"), equalTo(expected));
    }

}