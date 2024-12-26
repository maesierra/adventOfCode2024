package net.maesierra.adventOfCode2024.solutions.day24;

import net.maesierra.adventOfCode2024.solutions.day24.Day24.*;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.maesierra.adventOfCode2024.solutions.day24.Day24.parseConnections;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day24Test {

    @Test
    void testFullAdder() {
        String diagram = """
y22 AND x22 -> hwq
x22 XOR y22 -> cdf
cdf AND cmn -> knw
cmn XOR cdf -> z22
knw OR hwq -> fjs
""";
        HashMap<String, Wire> wires = new HashMap<>();
        var connections = parseConnections(Stream.of(diagram.split("\n")), wires
        );
        Circuit circuit = new Circuit(wires, connections);
        assertThat(Day24.testFullAdder(circuit, "x22", "y22", "cmn", "z22", "fjs"), equalTo(true));
    }

    @Test
    void  testPart1() {
        assertThat(part1(new Day24(), "input_24_1"), equalTo("4"));
        assertThat(part1(new Day24(), "input_24_2"), equalTo("2024"));
    }

}