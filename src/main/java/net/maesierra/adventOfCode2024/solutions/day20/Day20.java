package net.maesierra.adventOfCode2024.solutions.day20;

import net.maesierra.adventOfCode2024.Runner;
import net.maesierra.adventOfCode2024.utils.Matrix;
import net.maesierra.adventOfCode2024.utils.Matrix.Item;
import net.maesierra.adventOfCode2024.utils.Position;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsCharMatrix;

public class Day20 implements Runner.Solution {

    public static final Predicate<Item<Character>> IS_WALL = (i) -> i.value().equals('#');

    private static Predicate<Item<Character>> isWall() {
        return IS_WALL;
    }


    private static Graph<Position, DefaultEdge> createGraph(Matrix<Character> map) {
        Graph<Position, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        map.items().forEach(i -> {
            if (!isWall().test(i)) {
                graph.addVertex(i.position());
            }
        });
        map.items().forEach(i -> {
            if (isWall().test(i)) {
                return;
            }
            Stream<Item<Character>> neighbours = i.orthogonalNeighbours().stream()
                    .filter(Objects::nonNull)
                    .filter(not(isWall()));
            neighbours.forEach(neighbour -> {
                graph.addEdge(i.position(), neighbour.position());
            });
        });
        return graph;
    }

    private static Map<Integer, AtomicInteger> getBestTimes(InputStream input, int maxDistance) {
        Matrix<Character> map = inputAsCharMatrix(input);
        Position start = map.items().filter(i -> i.value().equals('S')).findFirst().orElseThrow().position();
        Position end =   map.items().filter(i -> i.value().equals('E')).findFirst().orElseThrow().position();
        var graph = createGraph(map);
        var algorithm = new DijkstraShortestPath<>(graph);
        Map<Integer, AtomicInteger> bestTimes = new HashMap<>();
        List<Position> path = algorithm.getPath(start, end).getVertexList();
        for (var pos:path) {
            int currentCost = path.indexOf(pos);
            System.out.printf("Checking path as pos %d of %d%n", currentCost, path.size());
            //Look for all the points at a distance of 20
            for (int row = -maxDistance; row <= maxDistance; row++) {
                for (int col = -maxDistance; col <= maxDistance; col++) {
                    Position cheatEnd = new Position(pos.row() + row, pos.col() + col);
                    int distance = cheatEnd.manhattanDistance(pos);
                    int cheatExitPos = path.indexOf(cheatEnd);
                    if (cheatExitPos == -1 || distance > maxDistance) {
                        continue;
                    }
                    int diff = cheatExitPos - currentCost - distance;
                    bestTimes.computeIfAbsent(diff, k -> new AtomicInteger(0)).incrementAndGet();
                }

            }
        }
        return bestTimes;
    }

    @Override
    public String part1(InputStream input, String... params) {
        int picoseconds = params.length >= 1 ? Integer.parseInt(params[0]) : 100;

        Map<Integer, AtomicInteger> bestTimes = getBestTimes(input, 2);
        int res = bestTimes.entrySet().stream().filter(e -> e.getKey() >= picoseconds).mapToInt(e -> e.getValue().get()).sum();
        return Integer.toString(res);
    }


    @Override
    public String part2(InputStream input, String... params) {
        int picoseconds = params.length >= 1 ? Integer.parseInt(params[0]) : 100;

        Map<Integer, AtomicInteger> bestTimes = getBestTimes(input, 20);
        int res = bestTimes.entrySet().stream().filter(e -> e.getKey() >= picoseconds).mapToInt(e -> e.getValue().get()).sum();
        return Integer.toString(res);
    }
}
