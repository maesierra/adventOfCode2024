package net.maesierra.adventOfCode2024.solutions.day23;

import net.maesierra.adventOfCode2024.Runner;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;

import java.io.InputStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsStream;

public class Day23 implements Runner.Solution {

    static class ComputerSet {
        String[] computers = new String[3];
        Set<String> set = new HashSet<>();

        ComputerSet(ComputerSet other) {
            this(other.computers[0], other.computers[1], other.computers[2]);
        }

        ComputerSet(String c1, String c2, String c3) {
            c1(c1);
            c2(c2);
            c3(c3);
        }

        void addComputer(String c, int pos) {
            if (c!= null) {
                computers[pos] = c;
                set.add(c);
            }
        }
        void c1(String c) {
            addComputer(c, 0);
        }

        void c2(String c) {
            addComputer(c, 1);
        }

        void c3(String c) {
            addComputer(c, 2);
        }

        public String c1() {
            return computers[0];
        }

        public String c2() {
            return computers[1];
        }

        public String c3() {
            return computers[2];
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ComputerSet that = (ComputerSet) o;
            return Objects.equals(set, that.set);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(set);
        }

        boolean contains(String c) {
            return set.contains(c);
        }

        boolean anyStartsWith(String letter) {
            return set.stream().anyMatch(c -> c.startsWith(letter));
        }

        @Override
        public String toString() {
            return set.toString();
        }
    }

    static Graph<String, DefaultEdge> parseNetworkMap(InputStream input, BiConsumer<String, String> forEachPair) {
        Graph<String, DefaultEdge> networkMap = new DefaultUndirectedGraph<>(DefaultEdge.class);
        inputAsStream(input).forEach(s -> {
            var computers = s.split("-");
            String c1 = computers[0];
            String c2 = computers[1];
            if (!networkMap.containsVertex(c1)) {
                networkMap.addVertex(c1);
            }
            if (!networkMap.containsVertex(c2)) {
                networkMap.addVertex(c2);
            }
            networkMap.addEdge(c1, c2);
            forEachPair.accept(c1, c2);
        });
        return networkMap;
    }

    @Override
    public String part1(InputStream input, String... params) {
        Set<ComputerSet> incomplete = new HashSet<>();

        Graph<String, DefaultEdge> networkMap = parseNetworkMap(
                input,
                (c1, c2) -> incomplete.add(new ComputerSet(c1, c2, null))
        );

        Set<ComputerSet> sets = new HashSet<>();
        incomplete.forEach(s -> {
            for (var source: List.of(s.c1(), s.c2())) {
                for (var edge : networkMap.edgesOf(source)) {
                    String target = networkMap.getEdgeTarget(edge);
                    if (target.equals(source)) {
                        target = networkMap.getEdgeSource(edge);
                    }
                    String other = source.equals(s.c1()) ? s.c2() : s.c1();
                    if (!networkMap.containsEdge(target, other)) {
                        continue;
                    }
                    if (!s.contains(target)) {
                        ComputerSet completedSet = new ComputerSet(s);
                        completedSet.c3(target);
                        if (completedSet.anyStartsWith("t")) {
                            sets.add(completedSet);
                        }
                    }
                }
            }
        });
        return Integer.toString(sets.size());
    }

    static <T> List<Set<T>> combinations(Set<T> set, int size) {
        if (set.size() == size) {
            return List.of(set);
        }
        List<Set<T>> res = new ArrayList<>();
        List<T> items = new ArrayList<>(set); //Need to iterate in order
        int diff = set.size() - size + 1;
        for (int i = 0; i < diff; i++) {
            for (int j = i + 1; j <= diff; j++) {
                Set<T> group = new HashSet<>();
                group.add(items.get(i));
                group.addAll(items.subList(j, j + size - 1));
                res.add(group);
            }
        }
        return res;
    }

    static boolean checkSet(Set<String> set, String startingNode, Graph<String, DefaultEdge> networkMap) {
        for (var c1:set) {
            if (c1.equals(startingNode)) {
                continue;
            }
            for (var c2:set) {
                if (c1.equals(c2) || c2.equals(startingNode)) {
                    continue;
                }
                if (!networkMap.containsEdge(c1, c2)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String part2(InputStream input, String... params) {
        Graph<String, DefaultEdge> networkMap = parseNetworkMap(input, (c1, c2) -> {});
        int nComputers = networkMap.vertexSet().size();
        for (int setSize = nComputers; setSize > 0; setSize--) {
            //In order for a computer with a given set size to be connected to all the computers in the set
            //their number of edges should be at least setSize and we need to have at least setSize computers meeting that criteria
            int currentSetSize = setSize;
            List<String> withSufficientEdges = networkMap.vertexSet().stream().filter(v -> networkMap.edgesOf(v).size() + 1 >= currentSetSize).toList();
            if (withSufficientEdges.size() >= setSize) {
                Set<Set<String>> alreadyChecked = new HashSet<>();
                for (var computer:networkMap.vertexSet()) {
                    Set<String> nodes = networkMap.edgesOf(computer).stream().map(e -> {
                        String source = networkMap.getEdgeSource(e);
                        return source.equals(computer) ? networkMap.getEdgeTarget(e) : source;
                    }).collect(Collectors.toCollection(HashSet::new));
                    nodes.add(computer);
                    for (var set: combinations(nodes, setSize)) {
                        if (alreadyChecked.contains(set)) {
                            continue;
                        }
                        if (checkSet(set, computer, networkMap)) {
                            return set.stream().sorted().collect(Collectors.joining(","));
                        }
                        alreadyChecked.add(set);
                    }
                }
            }
        }
        throw  new RuntimeException("No solution found");
    }
}
