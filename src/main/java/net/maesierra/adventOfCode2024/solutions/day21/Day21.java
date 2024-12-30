package net.maesierra.adventOfCode2024.solutions.day21;

import net.maesierra.adventOfCode2024.Runner;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsStream;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsString;

public class Day21 implements Runner.Solution {

    public static final Movement MOVEMENT_TO_A = new Movement('A');

    public static class Movement {
        private final char direction;
        private int n;

        public Movement(char direction) {
            this.direction = direction;
            this.n = 1;
        }

        public void increase() {
            this.n++;
        }


        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Movement movement = (Movement) o;
            return n == movement.n && Objects.equals(direction, movement.direction);
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, n);
        }

        @Override
        public String toString() {
            return StringUtils.repeat(direction, n);
        }
    }
    public interface Keypad {
        default Set<List<Movement>> movesFor(String code) {
            if (code.length() == 2) {
                Set<List<Movement>> res = getBestMovement(code);
                if (res == null) {
                    throw new RuntimeException("We've got a problem for " + code);
                }
                return res;
            }
            Set<List<Movement>> rest = movesFor(code.substring(1));
            Set<List<Movement>> res = new HashSet<>();
            Set<List<Movement>> allFromFirstToSecond = getBestMovement(code.substring(0, 2));
            if (allFromFirstToSecond == null) {
                throw new RuntimeException("We've got a problem for " + code.substring(0, 2));
            }
            for (var moveFromFirstToSecond : allFromFirstToSecond) {
                for (var restOfMovements : rest) {
                    res.add(Stream.concat(moveFromFirstToSecond.stream(), restOfMovements.stream()).toList());
                }
            }
            return res;
        }

        Set<List<Movement>> getBestMovement(String code);
    }

    static abstract class SimpleKeypad implements Keypad {
        private final Map<String, Set<List<Movement>>> bestMovementsMap;

        @Override
        public Set<List<Movement>> getBestMovement(String code) {
            return bestMovementsMap.get(code);
        }

        protected SimpleKeypad(Set<String> buttons, Graph<String, DefaultEdge> keys, Map<String, Map<String, String>> movements, Map<String, Set<String>> discardedOptions) {
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
                            List<Movement> res = new ArrayList<>();
                            String current = path.getStartVertex();
                            for (String v : path.getVertexList().subList(1, path.getVertexList().size())) {
                                char direction = movements.get(current).get(v).charAt(0);
                                if (res.isEmpty()) {
                                    res.add(MOVEMENT_TO_A);
                                    res.add(new Movement(direction));
                                } else {
                                    Movement last = res.get(res.size() - 1);
                                    if (last.direction == direction) {
                                        last.increase();
                                    } else {
                                        res.add(new Movement(direction));
                                    }
                                }
                                current = v;
                            }
                            if (discardedOptions.getOrDefault(from + to, Set.of()).contains(res.stream().map(Movement::toString).collect(Collectors.joining()))) {
                                return null;
                            }
                            res.add(MOVEMENT_TO_A);
                            return res;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()));
                    }
                }
                bestMovementsMap.put(from + from, Set.of(List.of(MOVEMENT_TO_A, MOVEMENT_TO_A)));
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
            super(buttons, buildGraph(), movements, Map.of());
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
            super(buttons, buildGraph(), movements, Map.of(
                    "A<", Set.of("A<v<"),
                    "<A", Set.of("A>^>")
            ));

        }

    }

    static class KeypadChain {
        private final NumericKeypad numeric = new NumericKeypad();
        private final DirectionalKeypad directional = new DirectionalKeypad();
        private final int levels;
        record State(char from, char to, int level) {}
        private final Map<State, Long> cache = new HashMap<>();

        KeypadChain() {
            this(2);
        }
        KeypadChain(int levels) {
            this.levels = levels;
        }

        long shortestSequence(State state) {
            if (state.level > levels) {
                return 1L;
            }
            Long cached = cache.get(state);
            if (cached != null) {
                return cached;
            }
            //level 0 == numeric keypad
            Keypad keypad = state.level == 0 ? numeric : directional;
            Set<List<Movement>> movements = keypad.getBestMovement("%s%s".formatted(state.from, state.to));
            long min = Long.MAX_VALUE;
            for (var movement:movements) {
                long value = connectedPairs(movement).stream()
                        .mapToLong(pair -> {
                            return shortestSequence(new State(pair.getLeft().direction, pair.getRight().direction, state.level + 1)) + (pair.getLeft().n - 1);
                        })
                        .sum();
                min = Math.min(min, value);
            }
            cache.put(state, min);
            return min;
        }

        long shortestSequence(String code) {
           return connectedPairs(code).stream()
                   .mapToLong(pair -> shortestSequence(new State(pair.getLeft(), pair.getRight(), 0)))
                   .sum();
        }

    }

    static List<Pair<Character, Character>> connectedPairs(String str) {
        return connectedPairs(str.chars().mapToObj(i -> (char)i).toList());
    }
    static <T> List<Pair<T, T>> connectedPairs(List<T> list) {
        List<Pair<T, T>> res = new ArrayList<>();
        for (int i = 0; i < list.size() - 1; i++) {
            res.add(Pair.of(list.get(i), list.get(i + 1)));
        }
        return res;
    }

    @Override
    public String part1(InputStream input, String... params) {
        KeypadChain keypadChain = new KeypadChain();
        long res = inputAsStream(input).mapToLong(code -> {
            long length = keypadChain.shortestSequence("A" + code);
            System.out.printf("Code %s => %d %n", code, length);
            return length * Long.parseLong(code.replace("A", ""));
        }).sum();
        return Long.toString(res);
    }

    @Override
    public String part2(InputStream input, String... params) {
        KeypadChain keypadChain = new KeypadChain(25);
        long res = inputAsStream(input).mapToLong(code -> {
            long length = keypadChain.shortestSequence("A" + code);
            System.out.printf("Code %s => %d %n", code, length);
            return length * Long.parseLong(code.replace("A", ""));
        }).sum();
        return Long.toString(res);
    }
}
