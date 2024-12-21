package net.maesierra.adventOfCode2024.solutions.day21;

import net.maesierra.adventOfCode2024.Runner;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.entry;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsStream;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsString;

public class Day21 implements Runner.Solution {

    public interface Keypad {
        default Set<String> movesFor(String code) {
            if (code.length() == 2) {
                Set<String> res = getBestMovement(code);
                if (res == null) {
                    throw new RuntimeException("We've got a problem for " + code);
                }
                return res;
            }
            Set<String> rest = movesFor(code.substring(1));
            Set<String> res = new HashSet<>();
            Set<String> allFromFirstToSecond = getBestMovement(code.substring(0, 2));
            if (allFromFirstToSecond == null) {
                throw new RuntimeException("We've got a problem for " + code.substring(0, 2));
            }
            for (var moveFromFirstToSecond : allFromFirstToSecond) {
                for (var restOfMovements : rest) {
                    res.add(moveFromFirstToSecond + restOfMovements);
                }
            }
            return res;
        }

        Set<String> getBestMovement(String code);
    }

    static abstract class SimpleKeypad implements Keypad {
        private final Map<String, Set<String>> bestMovementsMap;

        @Override
        public Set<String> getBestMovement(String code) {
            return bestMovementsMap.get(code);
        }

        protected SimpleKeypad(Set<String> buttons, Graph<String, DefaultEdge> keys, Map<String, Map<String, String>> movements) {
            var algorithm = new AllDirectedPaths<>(keys);
            var dijkstra = new DijkstraShortestPath<>(keys);
            bestMovementsMap = new HashMap<>();
            for (String from : buttons) {
                Set<String> others = new HashSet<>(buttons);
                others.remove(from);
                for (String to : others) {
                    //Find the best
                    var best = dijkstra.getPath(from, to);
                    if (best != null) {
                        //Get all the possible paths with the same size
                        var allPaths = algorithm.getAllPaths(from, to, true, best.getLength());
                        bestMovementsMap.put(from + to, allPaths.stream().map(path -> {
                            StringBuilder res = new StringBuilder();
                            String current = path.getStartVertex();
                            for (String v : path.getVertexList().subList(1, path.getVertexList().size())) {
                                res.append(movements.get(current).get(v));
                                current = v;
                            }
                            return res.append("A").toString();
                        }).collect(Collectors.toSet()));
                    }
                }
                bestMovementsMap.put(from + from, Set.of("A"));
            }
        }

    }

    static class NumericKeypad extends SimpleKeypad {

        final static Set<String> buttons = Set.of("A", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        final static Map<String, Map<String, String>> movements = Map.ofEntries(
                entry("A", Map.of(
                        "0", "<",
                        "3", "^")),
                entry("0", Map.of(
                        "A", ">",
                        "2", "^")),
                entry("1", Map.of(
                        "2", "<",
                        "4", "^")),
                entry("2", Map.of(
                        "1", "<",
                        "5", "^",
                        "3", ">",
                        "0", "v")),
                entry("3", Map.of(
                        "2", "<",
                        "6", "^",
                        "A", "v")),
                entry("4", Map.of(
                        "7", "^",
                        "1", "v",
                        "5", ">")),
                entry("5", Map.of(
                        "4", "<",
                        "8", "^",
                        "6", ">",
                        "2", "v")),
                entry("6", Map.of(
                        "9", "^",
                        "3", "v",
                        "5", "<")),
                entry("7", Map.of(
                        "4", "v",
                        "8", ">",
                        "9", ">")),
                entry("8", Map.of(
                        "7", "<",
                        "5", "v",
                        "9", ">")),
                entry("9", Map.of(
                        "6", "v",
                        "8", "<"))
        );

        private static Graph<String, DefaultEdge> buildGraph() {
            Graph<String, DefaultEdge> keys = new DefaultDirectedGraph<>(DefaultEdge.class);
            buttons.forEach(keys::addVertex);
            keys.addEdge("0", "2");
            keys.addEdge("0", "A");
            keys.addEdge("A", "0");
            keys.addEdge("A", "3");
            keys.addEdge("1", "2");
            keys.addEdge("1", "4");
            keys.addEdge("2", "1");
            keys.addEdge("2", "5");
            keys.addEdge("2", "3");
            keys.addEdge("2", "0");
            keys.addEdge("3", "2");
            keys.addEdge("3", "6");
            keys.addEdge("3", "A");
            keys.addEdge("4", "7");
            keys.addEdge("4", "1");
            keys.addEdge("4", "5");
            keys.addEdge("5", "4");
            keys.addEdge("5", "8");
            keys.addEdge("5", "6");
            keys.addEdge("5", "2");
            keys.addEdge("6", "9");
            keys.addEdge("6", "5");
            keys.addEdge("6", "3");
            keys.addEdge("7", "8");
            keys.addEdge("7", "4");
            keys.addEdge("8", "9");
            keys.addEdge("8", "5");
            keys.addEdge("8", "7");
            keys.addEdge("9", "8");
            keys.addEdge("9", "6");
            return keys;
        }


        public NumericKeypad() {
            super(buttons, buildGraph(), movements);
        }

        String run(String sequence) {
            StringBuilder output = new StringBuilder();
            String current = "A";
            for (String c: sequence.chars().mapToObj(Character::toString).toList()) {
                if (c.equals("A")) {
                    output.append(current);
                } else {
                    current = movements.get(current).entrySet().stream()
                            .filter(e -> e.getValue().contains(c))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Invalid movement"))
                            .getKey();
                }
            }
            return output.toString();
        }

    }

    static class DirectionalKeypad extends SimpleKeypad {

        final static Set<String> buttons = Set.of("A", "^", ">", "<", "v");
        final static Map<String, Map<String, String>> movements = Map.ofEntries(
                entry("A", Map.of(
                        "^", "<",
                        ">", "v")),
                entry("^", Map.of(
                        "A", ">",
                        "v", "v")),
                entry("<", Map.of(
                        "v", ">")),
                entry("v", Map.of(
                        "<", "<",
                        "^", "^",
                        ">", ">")),
                entry(">", Map.of(
                        "A", "^",
                        "v", "<"))
        );

        private static Graph<String, DefaultEdge> buildGraph() {
            Graph<String, DefaultEdge> keys = new DefaultDirectedGraph<>(DefaultEdge.class);
            buttons.forEach(keys::addVertex);
            keys.addEdge("A", "^");
            keys.addEdge("A", ">");
            keys.addEdge("^", "A");
            keys.addEdge("^", "v");
            keys.addEdge("<", "v");
            keys.addEdge("v", "<");
            keys.addEdge("v", "^");
            keys.addEdge("v", ">");
            keys.addEdge(">", "A");
            keys.addEdge(">", "v");
            return keys;
        }

        public DirectionalKeypad() {
            super(buttons, buildGraph(), movements);

        }

    }

    static class KeypadChain {
        private final List<Keypad> keypads;

        KeypadChain(List<Keypad> keypads) {
            this.keypads = keypads;
        }

        String shortestSequence(String code) {
            Deque<String> sequences = new LinkedList<>();
            sequences.add(code);
            for (int chainOrder = 0; chainOrder < keypads.size(); chainOrder++) {
                Keypad keypad = keypads.get(chainOrder);
                Deque<String> nextSequences = new LinkedList<>();
                while (!sequences.isEmpty()) {
                    String current = sequences.pop();
                    if (!current.startsWith("A")) {
                        current = "A" + current;
                    }
                    Set<String> options = keypad.movesFor(current);
                    System.out.printf("%s[%d] -> %d%n", current, chainOrder, options.size());
                    nextSequences.addAll(options);
                }
                sequences = nextSequences;
            }
            return sequences.stream().min(Comparator.comparing(String::length)).orElseThrow();
        }
        String shortestSequence2(String code) {
            Deque<String> sequences = new LinkedList<>();
            sequences.add(code);
            for (int chainOrder = 0; chainOrder < keypads.size(); chainOrder++) {
                Keypad keypad = keypads.get(chainOrder);
                Deque<String> nextSequences = new LinkedList<>();
                while (!sequences.isEmpty()) {
                    String current = sequences.pop();
                    if (!current.startsWith("A")) {
                        current = "A" + current;
                    }
                    Set<String> options = keypad.movesFor(current);
                    System.out.printf("%s[%d] -> %d%n", current, chainOrder, options.size());
                    if (chainOrder == 0) {
                        nextSequences.addAll(options);
                    } else {
                       nextSequences.add(options.stream().min(Comparator.comparing(String::length)).stream().findFirst().orElseThrow());
                    }
                }
                sequences = nextSequences;
            }
            return sequences.stream().min(Comparator.comparing(String::length)).orElseThrow();
        }
    }

    @Override
    public String part1(InputStream input, String... params) {
        KeypadChain keypadChain = new KeypadChain(List.of(new NumericKeypad(), new DirectionalKeypad(), new DirectionalKeypad()));
        int res = inputAsStream(input).mapToInt(code -> {
            String translated = keypadChain.shortestSequence(code);
            System.out.printf("Code %s => %s(%d) %n", code, translated, translated.length());
            return translated.length() * Integer.parseInt(code.replace("A", ""));
        }).sum();
        return Integer.toString(res);
    }

    @Override
    public String part2(InputStream input, String... params) {
        return inputAsString(input).toLowerCase();
    }
}
