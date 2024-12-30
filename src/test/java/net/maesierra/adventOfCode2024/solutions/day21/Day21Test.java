package net.maesierra.adventOfCode2024.solutions.day21;

import org.junit.jupiter.api.Test;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day21Test {

    @Test
    void  testPart1() {
        String expected = "126384";
        assertThat(part1(new Day21(), "input_21"), equalTo(expected));
    }

    @Test
    void  testPart2() {
        String expected = "154115708116294";
        assertThat(part2(new Day21(), "input_21"), equalTo(expected));
    }
}