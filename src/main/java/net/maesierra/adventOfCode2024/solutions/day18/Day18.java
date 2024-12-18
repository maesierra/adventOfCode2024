package net.maesierra.adventOfCode2024.solutions.day18;

import net.maesierra.adventOfCode2024.Runner;
import net.maesierra.adventOfCode2024.utils.Matrix;
import net.maesierra.adventOfCode2024.utils.Position;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsStream;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsString;

public class Day18 implements Runner.Solution {

    static class MemorySpace {
        boolean corrupted = false;

        public void corrupt() {
            this.corrupted = true;
        }

        @Override
        public String toString() {
            return corrupted ? "#" : ".";
        }
    }

    private record InputData(int gridSize, int nBytes, List<Position> bytes) { }

    private static InputData parseInput(InputStream input, String[] params) {
        int gridSize = 71;
        int nBytes = 1024;
        if (params.length >= 1) {
            gridSize = Integer.parseInt(params[0]);
        }
        if (params.length >= 2) {
            nBytes = Integer.parseInt(params[1]);
        }
        List<Position> bytes = inputAsStream(input).map(s -> {
            String[] parts = s.split(",");
            return new Position(Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
        }).toList();
        return new InputData(gridSize, nBytes, bytes);
    }

    private static Graph<Position, DefaultWeightedEdge> createGraph(Matrix<MemorySpace> memory) {
        Graph<Position, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        memory.items().forEach(i -> {
            if (!i.value().corrupted) {
                graph.addVertex(i.position());
            }
        });
        memory.items().forEach(i -> {
            if (i.value().corrupted) {
                return;
            }
            i.orthogonalNeighbours().stream().filter(Objects::nonNull).forEach(neighbour -> {
                if (!neighbour.value().corrupted) {
                    graph.addEdge(i.position(), neighbour.position());
                }
            });
        });
        return graph;
    }

    private static Matrix<MemorySpace> initaliseMemory(InputData inputData) {
        Matrix<MemorySpace> memory = Matrix.init(inputData.gridSize(), inputData.gridSize(), MemorySpace::new);

        for (int i = 0; i < inputData.nBytes(); i++) {
            memory.at(inputData.bytes().get(i)).value().corrupt();
        }
        return memory;
    }

    @Override
    public String part1(InputStream input, String... params) {
        InputData inputData = parseInput(input, params);
        Matrix<MemorySpace> memory = initaliseMemory(inputData);
        Graph<Position, DefaultWeightedEdge> graph = createGraph(memory);

        Position start = new Position(0, 0);
        Position end = new Position(inputData.gridSize() - 1, inputData.gridSize() - 1);
        var algorithm = new DijkstraShortestPath<>(graph);
        var path = algorithm.getPath(start, end);
        return Integer.toString(path.getLength());
    }

    @Override
    public String part2(InputStream input, String... params) {
        InputData inputData = parseInput(input, params);
        Matrix<MemorySpace> memory = initaliseMemory(inputData);
        Graph<Position, DefaultWeightedEdge> graph = createGraph(memory);

        Position start = new Position(0, 0);
        Position end = new Position(inputData.gridSize() - 1, inputData.gridSize() - 1);
        var algorithm = new DijkstraShortestPath<>(graph);
        //Start at the end of part1
        for (int i = inputData.nBytes + 1; i < inputData.bytes.size(); i++) {
            Position position = inputData.bytes.get(i);
            graph.removeVertex(position);
            var path = algorithm.getPath(start, end);
            if (path == null) {
                return "%d,%d".formatted(position.col(), position.row());
            }
        }
        throw new RuntimeException("No solution found");
    }
}
