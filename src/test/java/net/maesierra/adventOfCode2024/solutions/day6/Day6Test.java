package net.maesierra.adventOfCode2024.solutions.day6;

import org.junit.jupiter.api.Test;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day6Test {

    @Test
    void  testPart1() {
        String expected = "41";
        assertThat(part1(new Day6(), "input_6"), equalTo(expected));
    }

    @Test
    void  testPart2() {
        /*
         *  This should return 1
         *
         * .##..
         * ....#
         * .....
         * .^.#.
         * .....
         */
        String expected = "6";
        assertThat(part2(new Day6(), "input_6"), equalTo(expected));
    }
}