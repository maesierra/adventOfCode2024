package net.maesierra.adventOfCode2024.solutions.day24;

import net.maesierra.adventOfCode2024.Runner;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsTextBlocks;
import static org.apache.commons.lang3.StringUtils.defaultString;

public class Day24 implements Runner.Solution {


    public static class Wire {
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

        public void reset() {
            this.value = Optional.empty();
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
        @Override
        public String toString() {
            return "AND";
        }
    }

    static class Or extends LogicalGate {

        @Override
        protected int run(int i1, int i2) {
            return i1 | i2;
        }

        @Override
        public String toString() {
            return "OR";
        }
    }

    static class Xor extends LogicalGate {

        @Override
        protected int run(int i1, int i2) {
            return i1 ^ i2;
        }

        @Override
        public String toString() {
            return "XOR";
        }
    }

    public record Connections(Wire input1, Wire input2, Wire output, LogicalGate gate) {

        Connections withClonedWires() {
            return new Connections(new Wire(input1.label), new Wire(input2.label), new Wire(output.label), gate);
        }

        @Override
        public String toString() {
            return "%s %s %s -> %s".formatted(input1.label, gate, input2.label, output.label);
        }
    }
    public static class Circuit {
        final Map<String, Wire> wires;
        final Map<String, Wire> externalOutput;
        final Map<String, Connections> inputConnections;
        final Map<String, Set<Wire>> connectedTo = new HashMap<>();

        public Circuit(Map<String, Wire> wires, Map<String, Connections> connections) {
            this.wires = wires;
            this.externalOutput = wires.values().stream().filter(wire -> wire.label.startsWith("z")).collect(Collectors.toMap(Wire::label, Function.identity()));
            this.inputConnections = connections;
            calculateConnectedTo();
        }

        private void calculateConnectedTo() {
            connectedTo.clear();
            this.inputConnections.values().forEach(c -> {
                connectedTo.computeIfAbsent(c.input1.label, k -> new HashSet<>()).add(c.output);
                connectedTo.computeIfAbsent(c.input2.label, k -> new HashSet<>()).add(c.output);
            });
        }

        public void swap(String wire1, String wire2) {
            Connections c1 = inputConnections.get(wire1);
            Connections c2 = inputConnections.get(wire2);
            inputConnections.remove(wire1);
            inputConnections.remove(wire2);
            inputConnections.put(c2.output.label, new Connections(c1.input1,c1.input2,c2.output,c1.gate));
            inputConnections.put(c1.output.label, new Connections(c2.input1,c2.input2,c1.output,c2.gate));
            calculateConnectedTo();
        }

        void reset() {
            wires.values().forEach(Wire::reset);
        }
        long run(Map<String, Integer> initialValues) {
            return run(initialValues, Long.MAX_VALUE);
        }

        long run(Map<String, Integer> initialValues, long limit) {
            initialValues.forEach((label, value) -> wires.get(label).value = Optional.of(value));
            Deque<Wire> toProcess = wires.values().stream()
                    .filter(w -> w.value.isEmpty())
                    .collect(Collectors.toCollection(LinkedList::new));
            long counter = 0;
            while (!toProcess.isEmpty()) {
                if (counter > limit) {
                    throw new RuntimeException("Limit exceeded");
                }
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
                        counter++;
                    }
                }
            }
            String output = externalOutput.entrySet().stream()
                    .sorted(Entry.<String, Wire>comparingByKey().reversed())
                    .map(e -> Integer.toString(e.getValue().value.orElseThrow())).collect(Collectors.joining());
            return Long.parseLong(output, 2);
        }

        @Override
        public String toString() {
            return inputConnections.values().stream().map(Connections::toString).sorted().collect(Collectors.joining("\n"));
        }
    }

    private static Circuit parseCircuit(InputStream input) {
        var blocks = inputAsTextBlocks(input);
        Map<String, Wire> wires = new HashMap<>(blocks[0].map(s -> {
            String[] parts = s.split(":");
            return new Wire(parts[0], Integer.parseInt(parts[1].trim()));
        }).collect(Collectors.toMap(Wire::label, Function.identity())));
        Stream<String> block = blocks[1];
        Map<String, Connections> connections = parseConnections(block, wires);
        return new Circuit(wires, connections);
    }

    static Map<String, Connections> parseConnections(Stream<String> block, Map<String, Wire> wires) {
        Map<String, Connections> connections = new HashMap<>();
        block.forEach(s -> {
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
        return connections;
    }

    @Override
    public String part1(InputStream input, String... params) {
        Circuit circuit = parseCircuit(input);
        return Long.toString(circuit.run(Map.of()));
    }


    public static String  label(String part, int bit) {
        return "%s%02d".formatted(part, bit);
    }

    record FullAdder(Circuit circuit, String a, String b, String cIn, String sum, String cOut) {
        boolean test() {
            Map<Map<String, Integer>, Map<String, Integer>> table = Map.of(
                    Map.of(a, 0, b, 0, cIn, 0), Map.of(sum, 0, cOut, 0),
                    Map.of(a, 0, b, 1, cIn, 0), Map.of(sum, 1, cOut, 0),
                    Map.of(a, 1, b, 0, cIn, 0), Map.of(sum, 1, cOut, 0),
                    Map.of(a, 1, b, 1, cIn, 0), Map.of(sum, 0, cOut, 1),
                    Map.of(a, 0, b, 0, cIn, 1), Map.of(sum, 1, cOut, 0),
                    Map.of(a, 0, b, 1, cIn, 1), Map.of(sum, 0, cOut, 1),
                    Map.of(a, 1, b, 0, cIn, 1), Map.of(sum, 0, cOut, 1),
                    Map.of(a, 1, b, 1, cIn, 1), Map.of(sum, 1, cOut, 1)
            );
            for (var entry:table.entrySet()) {
                Map<String, Integer> in = entry.getKey();
                Map<String, Integer> out = entry.getValue();
                circuit.reset();
                //This simple circuit should not take that long
                try {
                    circuit.run(in, 100);
                } catch (Exception e) {
                    return false;
                }
                Map<String, Integer> actual = circuit.wires.values().stream().filter(w -> out.containsKey(w.label)).collect(Collectors.toMap(Wire::label, w -> w.value.orElseThrow()));
                if (!out.equals(actual)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean testFullAdder(Circuit circuit, String a, String b, String cIn, String sum, String cOut) {
        Map<Map<String, Integer>, Map<String, Integer>> table = Map.of(
                Map.of(a, 0, b, 0, cIn, 0), Map.of(sum, 0, cOut, 0),
                Map.of(a, 0, b, 1, cIn, 0), Map.of(sum, 1, cOut, 0),
                Map.of(a, 1, b, 0, cIn, 0), Map.of(sum, 1, cOut, 0),
                Map.of(a, 1, b, 1, cIn, 0), Map.of(sum, 0, cOut, 1),
                Map.of(a, 0, b, 0, cIn, 1), Map.of(sum, 1, cOut, 0),
                Map.of(a, 0, b, 1, cIn, 1), Map.of(sum, 0, cOut, 1),
                Map.of(a, 1, b, 0, cIn, 1), Map.of(sum, 0, cOut, 1),
                Map.of(a, 1, b, 1, cIn, 1), Map.of(sum, 1, cOut, 1)
        );
        for (var entry:table.entrySet()) {
            Map<String, Integer> in = entry.getKey();
            Map<String, Integer> out = entry.getValue();
            circuit.reset();
            circuit.run(in);
            Map<String, Integer> actual = circuit.wires.values().stream().filter(w -> out.containsKey(w.label)).collect(Collectors.toMap(Wire::label, w -> w.value.orElseThrow()));
            if (!out.equals(actual)) {
                return false;
            }
        }
        return true;
    }

    List<String> calculateSwap(FullAdder fullAdder) {
        Set<Set<String>> swaps = new HashSet<>();
        List<Wire> outputWires = fullAdder.circuit.inputConnections.values().stream().map(Connections::output).toList();
        for (var w1:outputWires) {
            for (var w2:outputWires) {
                if (w1 != w2) {
                    swaps.add(Set.of(w1.label, w2.label));
                }
            }
        }
        for (var swap:swaps.stream().map(s -> s.stream().toList()).toList()) {
            fullAdder.circuit.swap(swap.get(0), swap.get(1));
            if (fullAdder.test()) {
                return swap;
            }
            //Undo the swap
            fullAdder.circuit.swap(swap.get(0), swap.get(1));
        }
        throw new RuntimeException("No swap found!!");
    }


    @Override
    public String part2(InputStream input, String... params) {
        Circuit circuit = parseCircuit(input);
        Map<Integer, List<String>> swaps = new HashMap<>();
        //x00,y00,z00 is a half-adder
        //we cannot get 44 because the overflow is z45
        for (int i = 1; i <= 43; i++) {
            FullAdder fullAdder = getFullAdder(i, circuit);
            if (!fullAdder.test()) {
                List<String> swap = calculateSwap(fullAdder);
                swaps.put(i, swap);
                circuit.swap(swap.get(0), swap.get(1));
                System.out.printf("Invalid full-adder at position %d -> swapping %s%n", i, swap);
                //Back to the beginning
                i = 0;
            }
        }
        return swaps.values().stream().flatMap(List::stream).sorted().collect(Collectors.joining(","));
    }


    private FullAdder getFullAdder(int i, Circuit circuit) {
        Map<String, Connections> connections = new HashMap<>();
        String a = label("x", i);
        String b = label("y", i);
        String sum = label("z", i);
        for (var w1: circuit.connectedTo.get(a)) {
            var conn = circuit.inputConnections.get(w1.label).withClonedWires();
            connections.put(w1.label, conn);
            if (circuit.connectedTo.get(w1.label) != null) {
                for (var w2: circuit.connectedTo.get(w1.label)) {
                    conn = circuit.inputConnections.get(w2.label).withClonedWires();
                    connections.put(w2.label, conn);
                }
            }
        }
        var conn = circuit.inputConnections.get(sum).withClonedWires();
        connections.put(sum, conn);
        boolean invalidCIn = false;
        if (connections.size() == 4) {
            //This means one of the connections from a to b is connected directly to sum
            conn = connections.values().stream()
                    .filter(c-> !c.input1.label.equals(a) && !c.input2.label.equals(a))
                    .filter(c -> !c.output.label.equals(sum))
                    .findFirst()
                    .orElseThrow();
            Wire wire = circuit.connectedTo.get(conn.output.label).iterator().next();
            connections.put(wire.label, circuit.inputConnections.get(wire.label));
            invalidCIn = true;
        }
        Map<String, Wire> wires = new HashMap<>();
        connections.forEach((label, c) -> {
            wires.putIfAbsent(c.input1.label, c.input1);
            wires.putIfAbsent(c.input2.label, c.input2);
            wires.putIfAbsent(c.output.label, c.output);
        });
        connections = connections.entrySet().stream().collect(Collectors.toMap(
                Entry::getKey,
                e -> new Connections(
                        wires.get(e.getValue().input1.label),
                        wires.get(e.getValue().input2.label),
                        wires.get(e.getValue().output.label),
                        e.getValue().gate())
        ));
        //Cout must be connected to next z
        conn = circuit.inputConnections.get(label("z", i + 1));
        String cOut = wires.containsKey(conn.input1.label) ? conn.input1.label : conn.input2.label;
        if (!wires.containsKey(cOut)) {
            //In this case is safer to assume cOut is the OR gate result
            Connections orGate = connections.values().stream().filter(c -> c.gate instanceof Or).findFirst().orElseThrow();
            cOut = orGate.output.label;
        }
        String cIn = null;
        if (!invalidCIn) {
            //Cin must be connected to z but all its input must not be in the small circuit
            conn = circuit.inputConnections.get(label("z", i));
            cIn = !wires.containsKey(circuit.inputConnections.get(conn.input1.label).input1.label) ? conn.input1.label : conn.input2.label;
        }
        if (invalidCIn || (connections.containsKey(defaultString(cIn)))){
            //In this case we need to lookup for the previous fullAdder and use its OR gate
            FullAdder prev = getFullAdder(i - 1, circuit);
            Connections orGate = prev.circuit.inputConnections.values().stream().filter(c -> c.gate instanceof Or).findFirst().orElseThrow();
            cIn = orGate.output.label;
        }
        return new FullAdder(new Circuit(wires, connections), a, b, cIn, sum, cOut);
    }
}
