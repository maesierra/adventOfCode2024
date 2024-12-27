package net.maesierra.adventOfCode2024.solutions.day16;

import net.maesierra.adventOfCode2024.Runner;
import net.maesierra.adventOfCode2024.utils.Directions.Direction;
import net.maesierra.adventOfCode2024.utils.Logger;
import net.maesierra.adventOfCode2024.utils.Matrix;
import net.maesierra.adventOfCode2024.utils.Position;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.maesierra.adventOfCode2024.utils.Directions.Direction.EAST;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsCharMatrix;

public class Day16 implements Runner.Solution {


    record Node(Position position, Direction direction) {
        int cost(Node other) {
            int distance = Node.this.direction.distance(other.direction);
            return switch (distance) {
                case 0 -> 1;
                //make 180 turns much more expensive to discourage them
                case 180 -> 100000;
                default -> 1001;
            };
        }

    }

    record MazeMap(Matrix<Character> map, Position start, Position end) {

        public Matrix.Item<Character> at(Position pos) {
            return map.at(pos);
        }

        public <T2> Matrix<T2> map(Function<Matrix.Item<Character>, T2> mapper) {
            return map.map(mapper);
        }

        public Stream<Matrix.Item<Character>> items() {
            return map.items();
        }
    }

    private static MazeMap parseMap(InputStream input) {
        Matrix<Character> map = inputAsCharMatrix(input);
        Position start = null;
        Position end = null;
        for (var i: map.items().toList()) {
            switch (i.value()) {
                case 'S' -> start = i.position();
                case 'E' -> end = i.position();
            }
        }
        return new MazeMap(map, start, end);
    }

    private static Graph<Node, DefaultWeightedEdge> createGraph(MazeMap map) {
        Graph<Node, DefaultWeightedEdge> graph =
                new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(new Node(map.start, EAST));
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
                    graph.setEdgeWeight(node, dest, node.cost(dest));
                }
            });
        });
        return graph;
    }

    @Override
    public String part1(InputStream input, String... params) {
        MazeMap map = parseMap(input);
        var graph = createGraph(map);
        Node startNode = new Node(map.start, EAST);
        List<Node> endNodes = graph.vertexSet().stream().filter(n -> n.position.equals(map.end)).toList();
        DijkstraShortestPath<Node, DefaultWeightedEdge> algorithm = new DijkstraShortestPath<>(graph);
        int min = Integer.MAX_VALUE;
        for (var node:endNodes) {
            var path = algorithm.getPath(startNode, node);
            int shortestPath = (int) path.getWeight();
            if (shortestPath < min) {
                min = shortestPath;
            }
        }
        return Integer.toString(min);
    }


    @Override
    public String part2(InputStream input, String... params) {
        MazeMap map = parseMap(input);
        var graph = createGraph(map);
        Node startNode = new Node(map.start, EAST);
        List<Node> endNodes = graph.vertexSet().stream().filter(n -> n.position.equals(map.end)).toList();
        DijkstraShortestPath<Node, DefaultWeightedEdge> algorithm = new DijkstraShortestPath<>(graph);
        Node endNode = endNodes.get(0);
        int minCost = Integer.MAX_VALUE;
        GraphPath<Node, DefaultWeightedEdge> shortestPath = null;
        for (var node:endNodes) {
            var path = algorithm.getPath(startNode, node);
            if (path.getWeight() < minCost) {
                endNode = node;
                minCost = (int) path.getWeight();
                shortestPath = path;
            }
        }
        List<GraphPath<Node, DefaultWeightedEdge>> paths = new ArrayList<>();
        paths.add(shortestPath);
        Deque<GraphPath<Node, DefaultWeightedEdge>> toProcess = new LinkedList<>();
        toProcess.add(shortestPath);

        record Candidate(Node node, int currentCost, Node divergingAt, Node original) {}

        Map<Node, Integer> accruedCosts = new HashMap<>();
        Set<Position> visited = new HashSet<>();

        while (!toProcess.isEmpty()) {
            GraphPath<Node, DefaultWeightedEdge> path = toProcess.pop();
            int cost = 0;
            List<Node> nodes = path.getVertexList();
            Node lastNodeInPath = nodes.get(nodes.size() - 1);
            accruedCosts.put(lastNodeInPath, (int) path.getWeight());
            for (int i = 0; i < nodes.size() - 1; i++) {
                Node n1 = nodes.get(i);
                Node n2 = nodes.get(i + 1);
                int stepCost = (int) graph.getEdgeWeight(graph.getEdge(n1, n2));
                cost += stepCost;
                accruedCosts.put(n1, cost);
                visited.add(n1.position);
                visited.add(n2.position);
            }


            List<Candidate> candidates = nodes.stream().filter(n -> !n.equals(lastNodeInPath)).flatMap(n -> map.at(n.position)
                            .orthogonalNeighbours()
                            .stream()
                            .filter(Objects::nonNull)
                            .filter(i -> i.value() != '#')
                            .filter(i -> !visited.contains(i.position()))
                            .map(i -> {
                                Node nextNode = nodes.get(nodes.indexOf(n) + 1);
                                return new Candidate(
                                        new Node(i.position(), Direction.fromPosition(n.position, i.position())),
                                        accruedCosts.get(n),
                                        n,
                                        nextNode);
                            }))
                            .toList();
            System.out.printf("Checking %d candidates...%n", candidates.size());
            for (var candidate:candidates) {
                var p = algorithm.getPath(candidate.node, endNode);
                if (p == null) {
                    continue;
                }
                //We need to figure out the difference between going from the diverging point to the next in path
                //and from diverging and the new path
                int diff = candidate.divergingAt.cost(candidate.original) - candidate.divergingAt.cost(candidate.node);
                if (p.getWeight() + candidate.currentCost - diff == path.getWeight()) {
                    paths.add(p);
                    toProcess.add(p);
                }
            }
        }

        Set<Position> bestPositions = paths.stream().flatMap(p -> p.getVertexList().stream())
                .map(Node::position)
                .collect(Collectors.toSet());
        if (Logger.getLevel() == Logger.Level.DEBUG) {
            Set<Position> shortestPathPositions = shortestPath.getVertexList().stream().map(Node::position).collect(Collectors.toSet());
            System.out.println(map.map(i -> {
                if (shortestPathPositions.contains(i.position())) {
                    return 'o';
                }
                return bestPositions.contains(i.position()) ? 'O' : i.value();
            }));
        }
        return Integer.toString(bestPositions.size());
    }
}

