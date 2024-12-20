package net.maesierra.adventOfCode2024.solutions.day20;

import net.maesierra.adventOfCode2024.Runner;
import net.maesierra.adventOfCode2024.utils.Directions.Direction;
import net.maesierra.adventOfCode2024.utils.Logger;
import net.maesierra.adventOfCode2024.utils.Matrix;
import net.maesierra.adventOfCode2024.utils.Matrix.Item;
import net.maesierra.adventOfCode2024.utils.Position;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsCharMatrix;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsString;

public class Day20 implements Runner.Solution {

    public static final Predicate<Item<Character>> IS_WALL = (i) -> i.value().equals('#');

    private static Predicate<Item<Character>> isWall() {
        return IS_WALL;
    }

    record Shortcut(Position from, Position to, Position wall) {

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Shortcut shortcut = (Shortcut) o;
            return Objects.equals(wall, shortcut.wall);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(wall);
        }
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

    private static List<Position> findPath(Matrix<Character> map, Position start, Position end, Optional<Shortcut> shortcut) {
        var graph = createGraph(shortcut.map(s -> map.map(i -> {
            if (i.position().equals(s.wall)) {
                return '.';
            } else {
                return i.value();
            }
        })).orElse(map));
        var algorithm = new DijkstraShortestPath<>(graph);
        var path = algorithm.getPath(start, end);
        if (path == null) {
            return List.of();
        }
        return path.getVertexList();
    }

    private static void printShortcut(Shortcut shortcut, List<Position> path, int timeToBeat, Matrix<Character> map) {
        int time = path.size() - 1;
        System.out.printf("Shortcut at %s time %d diff: %d%n", shortcut.wall, time, timeToBeat - time);
        Matrix<Character> matrix = map.map(i -> {
            if (i.position().equals(shortcut.wall)) {
                return '=';
            } else if (path.contains(i.position())) {
                return '.';
            } else if (i.value().equals('.')) {
                return ' ';
            } else {
                return i.value();
            }
        });
        System.out.println(matrix.toString());
        System.out.println();
        System.out.println();
    }

    private static String solveUsingDijkstra(Matrix<Character> map, Position start, Position end, int picoseconds) {
        //Get the baseline
        var normalPath = findPath(map, start, end, Optional.empty());
        int timeToBeat = normalPath.size() - 1;

        //Find all the shortcuts
        Set<Shortcut> shortcuts = normalPath.stream().flatMap(pos -> {
            Item<Character> current = map.at(pos);
            //find all the walls along the path
            return current.orthogonalNeighbours().stream()
                    .filter(Objects::nonNull)
                    .filter(isWall())
                    .filter(not(Item::isEdge)) //Take out the edges
                    .map(i -> {
                        //Check if at the other side of the wall there is a path position
                        Direction direction = Direction.fromPosition(current.position(), i.position());
                        Item<Character> neighbour = i.orthogonalNeighbours().get(direction);
                        if (neighbour != null && !isWall().test(neighbour)) {
                            return Optional.of(new Shortcut(current.position(), neighbour.position(), i.position()));
                        } else {
                            return Optional.<Shortcut>empty();
                        }
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get);
        }).collect(Collectors.toSet());


        AtomicInteger countdown = new AtomicInteger(shortcuts.size());
        long res = shortcuts.stream().sorted(Comparator.comparing(Shortcut::wall)).mapToInt(shortcut -> {
                    Logger.info("Calculating path if wall %s removed (%d left)...", shortcut.wall, countdown.getAndDecrement());
                    List<Position> path = findPath(map, start, end, Optional.of(shortcut));

                    int time = path.size() - 1;
                    if (timeToBeat - time == 2) {
                        if (Logger.getLevel().equals(Logger.Level.DEBUG)) {
                            printShortcut(shortcut, path, timeToBeat, map);
                        }
                    }
                    return time;
                })
                .map(n -> timeToBeat - n)
                .filter(diff  -> diff >= picoseconds)
                .count();


        return Long.toString(res);
    }

    @Override
    public String part1(InputStream input, String... params) {
        int picoseconds = params.length >= 1 ? Integer.parseInt(params[0]) : 100;

        Matrix<Character> map = inputAsCharMatrix(input);
        Position start = map.items().filter(i -> i.value().equals('S')).findFirst().orElseThrow().position();
        Position end =   map.items().filter(i -> i.value().equals('E')).findFirst().orElseThrow().position();
        return solveUsingDijkstra(map, start, end, picoseconds);
    }

    @Override
    public String part2(InputStream input, String... params) {
        return inputAsString(input).toLowerCase();
    }
}
