package net.maesierra.adventOfCode2024.solutions.day16;

import net.maesierra.adventOfCode2024.Runner;
import net.maesierra.adventOfCode2024.utils.Directions.Direction;
import net.maesierra.adventOfCode2024.utils.Matrix;
import net.maesierra.adventOfCode2024.utils.Matrix.Item;
import net.maesierra.adventOfCode2024.utils.Position;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static net.maesierra.adventOfCode2024.utils.Directions.Direction.EAST;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsCharMatrix;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsString;

public class Day16 implements Runner.Solution {



    record Movement(Position from, Position to, Direction direction, int cost) {

    }

    record Path(List<Movement> steps) {

        Path(Movement first) {
            this(new ArrayList<>());
            steps.add(first);
        }

        public Path(Path path, Movement m) {
            this(new ArrayList<>(path.steps));
            steps.add(m);
        }

        public Path(Path path1, Path path2) {
            this(new ArrayList<>(path1.steps));
            List<Movement> path2Movements = new ArrayList<>(path2.steps);
            while (!path1.end().to.equals(path2Movements.get(0).from)) {
                path2Movements.remove(path2Movements.get(0));
            }
            steps.addAll(path2Movements);
        }

        Movement end() {
            return steps.get(steps.size() - 1);
        }

        boolean inPath(Position position) {
            return steps.stream()
                    .anyMatch(m -> m.from.equals(position));
        }

        Movement at(Position position) {
            return steps.stream()
                    .filter(m -> m.from.equals(position))
                    .findFirst()
                    .orElseThrow();
        }

        Path cut(Position from) {
            for (int i = 0; i < steps.size(); i++) {
                if (steps.get(i).from.equals(from)) {
                    if (i == 0) {
                        return this;
                    }
                    return new Path(steps.subList(i, steps.size()));
                }
            }
            return this;
        }
        long cost() {
            return steps.stream().mapToLong(Movement::cost).sum();
        }

        @Override
        public String toString() {
            String res = steps.stream().map(m -> "[%d,%d]%s".formatted(
                    m.from.row(),
                    m.from.col(),
                    switch (m.direction()) {
                        case NORTH -> '^';
                        case SOUTH -> 'v';
                        case EAST -> '>';
                        case WEST -> '<';
                        default -> "";
                    }))
                    .collect(Collectors.joining());
            Position endPosition = end().to;
            return res + "[%d,%d]".formatted(endPosition.row(), endPosition.col());
        }
    }
    class Cache {
        private final Map<Key, Entry> hashMap = new HashMap<>();
        private Path bestPath = null;
        private Long bestCost = Long.MAX_VALUE;

        record Key(Position from, Direction direction) {

            public Key(Movement movement) {
                this(movement.from, movement.direction);
            }
        }

        record Entry(Map<Direction, Path> costs, int expectedEntries, boolean partial) {
            boolean isCompleted() {
                return !partial && costs.size() == expectedEntries;
            }
            public void put(Direction direction, Path path) {
                if (!costs.containsKey(direction)) {
                    costs.put(direction, path);
                } else {
                    long current = costs.get(direction).cost();
                    if (path.cost() < current) {
                        costs.put(direction, path);
                    }
                }
            }
            public Optional<Path> best() {
                return costs.values().stream().min(Comparator.comparing(Path::cost));
            }
        }
        public void newEntry(Movement current, List<Movement> possibleMovements) {
            int expectedEntries = 3;
            Set<Direction> directions = possibleMovements.stream().map(Movement::direction).collect(Collectors.toSet());
            if (!directions.contains(current.direction)) {
                expectedEntries --;
            }
            if (!directions.contains(current.direction.rotate90Right())) {
                expectedEntries --;
            }
            if (!directions.contains(current.direction.rotate90Left())) {
                expectedEntries --;
            }
            Key key = new Key(current);
            this.hashMap.put(key, new Entry(new HashMap<>(), expectedEntries, false));
        }

        public void updateIfBest(Path path) {
            System.out.println("Path found at %d".formatted(path.cost()));
            if (bestPath == null) {
                bestPath = path;
                bestCost = path.cost();
            } else {
                if (path.cost() < bestPath.cost()) {
                    bestPath = path;
                    bestCost = path.cost();
                }
            }
        }

        public void updatePaths(Path path) {
            for (int i = path.steps.size() - 1; i >= 0; i--) {
                Movement m = path.steps.get(i);
                Path subPath = path.cut(m.from);
                Key key = new Key(m);
                Entry cacheEntry = this.hashMap.get(key);
                if (cacheEntry == null) {
                    continue;
                }
                Path current = cacheEntry.costs.get(m.direction);
                if (current == null || subPath.cost() < current.cost()) {
                    cacheEntry.put(m.direction, subPath);
                }
                if (!cacheEntry.isCompleted()) {
                    break;
                }

            }
        }
        Optional<Entry> getIfCompleted(Movement movement) {
            Key key = new Key(movement);
            if (!hashMap.containsKey(key)) {
                return Optional.empty();
            }
            Entry entry = hashMap.get(key);
            return entry.isCompleted() ? Optional.of(entry) : Optional.empty();
        }

    }





    static List<Movement> possibleMovements(Position position, Direction direction, Matrix<Character> map) {
        List<Movement> res = new ArrayList<>();
        Map<Direction, Item<Character>> neighbours = map.at(position).orthogonalNeighbours().asMap(true);
        for (var entry: neighbours.entrySet()) {
            if (entry.getValue().value() == '#') {
                continue;
            }
            int rotationDistance = entry.getKey().distance(direction);
            if (rotationDistance == 180) {
                continue;
            }
            int cost = switch (rotationDistance) {
                case 90,270 -> 1001;
                case 0 -> 1;
                default -> throw new RuntimeException("Invalid rotation");
            };

            res.add(new Movement(position, entry.getValue().position(), entry.getKey(), cost));
        }
        res.sort(comparing(Movement::cost).reversed());
        return res;

    }

    record Destinations(Position start, Position end) {

    }

    private static Destinations destinations(Matrix<Character> map) {
        Position start = null;
        Position end = null;
        for (var i: map.items().toList()) {
            switch (i.value()) {
                case 'S' -> start = i.position();
                case 'E' -> end = i.position();
            }
        }
        return new Destinations(start, end);
    }


    @Override
    public String part1(InputStream input, String... params) {
        return usingGraph(input);
    }

    private String usingQueue(InputStream input) {
        Matrix<Character> map = inputAsCharMatrix(input);
        Destinations destinations = destinations(map);
        Position start = destinations.start;
        Position end = destinations.end;
        Cache cache = new Cache();

        Deque<Path> queue = new LinkedList<>();
        for (var m: possibleMovements(start, EAST, map)) {
            queue.addFirst(new Path(m));
        }
        long iteration = 0;

        while (!queue.isEmpty()) {
            if (iteration % 50000 == 0) {
                int nEntries = cache.hashMap.size();
                int completedEntries = (int) cache.hashMap.values().stream().filter(Cache.Entry::isCompleted).count();
                System.out.println("Iteration %d -> Queue size %d Current Best %d cache entries %d/%d".formatted(iteration, queue.size(), cache.bestCost, completedEntries, nEntries));
            }
            Path path = queue.pop();
            Movement lastMovement = path.end();
            if (lastMovement.to.equals(end)) {
                cache.updateIfBest(path);
                //Update the cache
                cache.updatePaths(path);
                continue;
            }
            //Check if we already have all the options from here
            Optional<Cache.Entry> entry = cache.getIfCompleted(lastMovement);
            if (entry.isPresent()) {
                Optional<Path> best = entry.get().best();
                if (best.isPresent()) {
                    Path newPath = new Path(path, best.get());
                    cache.updateIfBest(newPath);
                    cache.updatePaths(newPath);
                }
            } else {
                List<Movement> movements = possibleMovements(lastMovement.to, lastMovement.direction, map);
                cache.newEntry(lastMovement, movements);
                for (var m: movements) {
                    if (!path.inPath(m.to)) {
                        Path newPath = new Path(path, m);
                        if (newPath.cost() < cache.bestCost) {
                            queue.addFirst(newPath);
                        }
                    }

                }
            }

            iteration++;
        }
        if (cache.bestPath == null) {
            throw new RuntimeException("No path found");
        }

        System.out.println(map.map(i -> {
            if (cache.bestPath.inPath(i.position())) {
                Movement m = cache.bestPath.at(i.position());
                return switch (m.direction) {
                    case EAST -> '>';
                    case WEST -> '<';
                    case NORTH -> '^';
                    case SOUTH -> 'v';
                    default -> '.';
                };
            } else {
                return i.value();
            }
        }));
        return Long.toString(cache.bestCost);
    }

    private static String usingGraph(InputStream input) {
        Matrix<Character> map = inputAsCharMatrix(input);
        Destinations destinations = destinations(map);
        Position start = destinations.start;
        Position end = destinations.end;
        record Node(Position position, Direction direction) {}
        Graph<Node, DefaultWeightedEdge> graph =
                new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(new Node(start, EAST));
        map.items().filter(i -> i.value() != '#').forEach(i -> {
            Position position = i.position();
            i.orthogonalNeighbours().asMap(true).forEach((dir, neighbour) -> {
                if (neighbour.value() != '#') {
                    graph.addVertex(new Node(position, dir.rotate180()));
                }
            });
        });
        graph.vertexSet().forEach(node -> {
            map.at(node.position).orthogonalNeighbours().asMap(true).forEach((dir, neighbour) -> {
                if (neighbour.value() != '#') {
                    int distance = node.direction.distance(dir);
                    if (distance == 180) {
                        return;
                    }
                    Node dest = new Node(neighbour.position(), dir);
                    graph.addEdge(node, dest);
                    graph.setEdgeWeight(node, dest, distance == 0 ? 1 : 1001);
                }
            });
        });
        Node startNode = new Node(start, EAST);
        List<Node> endNodes = graph.vertexSet().stream().filter(n -> n.position.equals(end)).toList();
        DijkstraShortestPath<Node, DefaultWeightedEdge> algorithm = new DijkstraShortestPath<>(graph);
        int res = endNodes.stream().mapToInt(node -> (int) algorithm.getPath(startNode, node).getWeight()).min().orElseThrow();
        return Integer.toString(res);
    }

    @Override
    public String part2(InputStream input, String... params) {
        return inputAsString(input).toLowerCase();
    }
}

