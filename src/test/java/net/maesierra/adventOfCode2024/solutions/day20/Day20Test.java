package net.maesierra.adventOfCode2024.solutions.day20;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day20Test {

    @ValueSource(strings = {
           "44,2",
           "30,4",
           "16,6",
           "14,8",
           "10,10",
           "8,12",
           "5,20",
           "4,36",
           "3,38",
           "2,40",
           "1,64"
    })
    @ParameterizedTest
    void  testPart1(String expectedAnSeconds) {
        String[] parts = expectedAnSeconds.split(",");
        String expected = parts[0];
        String seconds = parts[1];
        assertThat(part1(new Day20(), "input_20", seconds), equalTo(expected));
    }

    @Test
    void  testPart2() {
        String expected = "09:30, 4h 20min, 13:50, 46.00€";
        assertThat(part2(new Day20(), "input_20"), equalTo(expected));
    }
}