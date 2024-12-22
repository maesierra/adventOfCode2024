package net.maesierra.adventOfCode2024.solutions.day22;

import net.maesierra.adventOfCode2024.solutions.day22.Day22.SecretGenerator;
import net.maesierra.adventOfCode2024.solutions.day22.Day22.SecretGenerator.Sequence;
import org.junit.jupiter.api.Test;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

class Day22Test {

    @Test
    void testSequence() {
        String[] values = {"15887950","16495136","527345","704524","1553684","12683156","11100544","12249484","7753432","5908254"};
        var generator = new SecretGenerator(123);
        for (var value:values) {
            assertThat(generator.next(), equalTo(Long.parseLong(value)));
        }
        generator = new SecretGenerator(123);
        assertThat(generator.next(10), equalTo(Long.parseLong(values[9])));
        assertThat(generator.getBananasPerSequence(),
                hasEntry(equalTo(new Sequence(-3, 6, -1, -1)), equalTo(4)));
        assertThat(generator.getBananasPerSequence(),
                hasEntry(equalTo(new Sequence(6, -1, -1, 0)), equalTo(4)));
    }

    @Test
    void  testPart1() {
        String expected = "37327623";
        assertThat(part1(new Day22(), "input_22"), equalTo(expected));
    }

    @Test
    void  testPart2() {
        String expected = "23";
        assertThat(part2(new Day22(), "input_22_2"), equalTo(expected));
    }
}