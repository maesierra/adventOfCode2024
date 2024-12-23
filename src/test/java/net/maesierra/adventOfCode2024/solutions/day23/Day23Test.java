package net.maesierra.adventOfCode2024.solutions.day23;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

class Day23Test {

    @Test
    void testCombinations() {
        Set<String> set = Set.of("a", "b", "c", "d", "e");
        assertThat(Day23.combinations(set, 4), containsInAnyOrder(
                Set.of("a", "b", "c", "d"),
                Set.of("a", "c", "d", "e"),
                Set.of("b", "c", "d", "e")));
        assertThat(Day23.combinations(set, 3), containsInAnyOrder(
                Set.of("a", "b", "c"),
                Set.of("a", "c", "d"),
                Set.of("a", "d", "e"),
                Set.of("b", "c", "d"),
                Set.of("b", "d", "e"),
                Set.of("c", "d", "e")));
        assertThat(Day23.combinations(set, 5), equalTo(List.of(Set.of("a", "b", "c", "d", "e"))));
    }

    @Test
    void  testPart1() {
        String expected = "7";
        assertThat(part1(new Day23(), "input_23"), equalTo(expected));
    }

    @Test
    void  testPart2() {
        String expected = "co,de,ka,ta";
        assertThat(part2(new Day23(), "input_23"), equalTo(expected));
    }
}