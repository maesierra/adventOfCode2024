package net.maesierra.adventOfCode2024.solutions.day24;

import net.maesierra.adventOfCode2024.Runner;

import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsString;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsTextBlocks;

public class Day24 implements Runner.Solution {


    static class Wire {
        private Optional<Integer> value;
        private final String label;

        Wire(String label, int value) {
            this.label = label;
            this.value = Optional.of(value);
        }

        Wire(String label) {
            this.label = label;
            this.value = Optional.empty();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Wire wire = (Wire) o;
            return Objects.equals(label, wire.label);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(label);
        }

        public String label() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    static abstract class LogicalGate  {
        protected abstract int run(int i1, int i2);
        public Optional<Integer> run(Wire i1, Wire i2) {
            if (i1.value.isEmpty() || i2.value.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(run(i1.value.get(), i2.value.get()));
        }
    }
    static class And extends LogicalGate {

        @Override
        protected int run(int i1, int i2) {
            return i1 & i2;
        }
    }

    static class Or extends LogicalGate {

        @Override
        protected int run(int i1, int i2) {
            return i1 | i2;
        }
    }

    static class Xor extends LogicalGate {

        @Override
        protected int run(int i1, int i2) {
            return i1 ^ i2;
        }
    }

    record Connections(Wire input1, Wire input2, Wire output, LogicalGate gate) {

    }
    static class Circuit {
        final Map<String, Wire> wires;
        final Map<String, Wire> externalOutput;
        final Map<String, Connections> inputConnections;
        final Map<String, Set<Wire>> connectedTo = new HashMap<>();

        public Circuit(Map<String, Wire> wires, Map<String, Connections> connections) {
            this.wires = wires;
            this.externalOutput = wires.values().stream().filter(wire -> wire.label.startsWith("z")).collect(Collectors.toMap(Wire::label, Function.identity()));
            this.inputConnections = connections;
            connections.values().forEach(c -> {
                connectedTo.computeIfAbsent(c.input1.label, k -> new HashSet<>()).add(c.output);
                connectedTo.computeIfAbsent(c.input2.label, k -> new HashSet<>()).add(c.output);
            });
        }

        long run(Map<String, Integer> initialValues) {
            initialValues.forEach((label, value) -> wires.get(label).value = Optional.of(value));
            Deque<Wire> toProcess = wires.values().stream()
                    .filter(w -> w.value.isEmpty())
                    .collect(Collectors.toCollection(LinkedList::new));
            while (!toProcess.isEmpty()) {
                Wire current = toProcess.pop();
                //We do nothing if the value is already set
                if (current.value.isEmpty()) {
                    //Get the connections
                    Connections connections = this.inputConnections.get(current.label);
                    //Try to see if the input can be calculated
                    current.value = connections.gate.run(connections.input1, connections.input2);
                    //If there is still no value -> back to the queue
                    if (current.value.isEmpty()) {
                        toProcess.addLast(current);
                    }
                }
            }
            String output = externalOutput.entrySet().stream()
                    .sorted(Entry.<String, Wire>comparingByKey().reversed())
                    .map(e -> Integer.toString(e.getValue().value.orElseThrow())).collect(Collectors.joining());
            return Long.parseLong(output, 2);
        }
    }

    private static Circuit parseCircuit(InputStream input) {
        var blocks = inputAsTextBlocks(input);
        Map<String, Wire> wires = new HashMap<>(blocks[0].map(s -> {
            String[] parts = s.split(":");
            return new Wire(parts[0], Integer.parseInt(parts[1].trim()));
        }).collect(Collectors.toMap(Wire::label, Function.identity())));
        Map<String, Connections> connections = new HashMap<>();
        blocks[1].forEach(s -> {
            if (s.trim().isBlank()) {
                return;
            }
            String[] parts = s.split(" ");
            Wire input1 = wires.computeIfAbsent(parts[0], Wire::new);
            Wire input2 = wires.computeIfAbsent(parts[2], Wire::new);
            Wire output = wires.computeIfAbsent(parts[4], Wire::new);
            LogicalGate gate = switch (parts[1]) {
                case "AND" -> new And();
                case "OR" -> new Or();
                case "XOR" -> new Xor();
                default -> throw new RuntimeException("Invalid gate type");
            };
            connections.put(output.label, new Connections(input1, input2, output, gate));
        });
        return new Circuit(wires, connections);
    }

    @Override
    public String part1(InputStream input, String... params) {
        Circuit circuit = parseCircuit(input);
        return Long.toString(circuit.run(Map.of()));
    }

    /**
     * Check if a, b and c are a half adder
     *         a XOR b -> sum
     *         a AND b -> c1
     *
     * @return string label for the wire that contains the carry over
     */
    static String halfAdder(Circuit circuit, String a, String b, String sum) {
        // a XOR b -> sum
        Connections xor = circuit.inputConnections.get(sum);
        assert xor.gate instanceof Xor;
        assert xor.input1.label.equals(a) || xor.input1.label.equals(b);
        assert xor.input2.label.equals(a) || xor.input2.label.equals(b);
        //a and b -> c1
        Set<Wire> fromA = circuit.connectedTo.get(a);
        assert fromA.size() == 2;
        Set<Wire> fromB = circuit.connectedTo.get(b);
        assert fromB.size() == 2;
        Wire carryOver = fromA.stream().filter(w -> !w.label.equals(sum)).findFirst().orElseThrow();
        assert fromB.contains(carryOver);
        Connections and = circuit.inputConnections.get(carryOver.label);
        assert and.gate instanceof And;
        assert and.input1.label.equals(a) || and.input1.label.equals(b);
        assert and.input2.label.equals(a) || and.input2.label.equals(b);
        return carryOver.label;

    }

    @Override
    public String part2(InputStream input, String... params) {
        Circuit circuit = parseCircuit(input);
        //Full Binary Addition
        // x001/y001 -> Half adder -> z001/c1
        // x002/y002/c1 -> Full adder -> z002/c2
        // ...
        // x???/y???/c? -> Full adder -> z???

        // Half adder
        // x001 XOR y001 -> z001
        // x001 AND y001 -> c1

        //Full adder
        // x002 XOR y002 -> tmp1
        // x002 AND y002 -> tmp2
        // c1 XOR tmp1 -> z002
        // c1 AND tmp1 -> tmp3
        // tmp2 OR tmp3 -> c2

        String carryOver = halfAdder(circuit, "x00", "y00", "z00");


        return inputAsString(input).toLowerCase();
    }
}
