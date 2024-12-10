package net.maesierra.adventOfCode2024.solutions.day10;

import net.maesierra.adventOfCode2024.Main;
import net.maesierra.adventOfCode2024.utils.Matrix;
import net.maesierra.adventOfCode2024.utils.Matrix.Item;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsCharMatrix;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsString;

public class Day10 implements Main.Solution {

    @Override
    public String part1(InputStream input, String... params) {
        Matrix<Integer> map = inputAsCharMatrix(input).map(c -> Integer.parseInt(c.value().toString()));
        List<Item<Integer>> startingPoints = map.items().filter(p -> p.value() == 0).toList();
        int sum = 0;
        for (var start:startingPoints) {
            List<Item<Integer>> points = new ArrayList<>(List.of(start));
            AtomicInteger currentHeight = new AtomicInteger(0);
            while (currentHeight.get() < 9 && !points.isEmpty()) {
                points = points
                        .stream()
                        .flatMap(i ->
                             i.orthogonalNeighbours().stream()
                                     .filter(Objects::nonNull)
                                     .filter(p -> p.value() == currentHeight.get() + 1)
                        )
                        .distinct()
                        .toList();
                currentHeight.incrementAndGet();
            }
            sum += points.size();;

        }
        return Integer.toString(sum);
    }

    @Override
    public String part2(InputStream input, String... params) {
        Matrix<Integer> map = inputAsCharMatrix(input).map(c -> Integer.parseInt(c.value().toString()));
        List<Item<Integer>> startingPoints = map.items().filter(p -> p.value() == 0).toList();
        int sum = 0;
        for (var start:startingPoints) {
            List<Item<Integer>> points = new ArrayList<>(List.of(start));
            AtomicInteger currentHeight = new AtomicInteger(0);
            while (currentHeight.get() < 9 && !points.isEmpty()) {
                points = points
                        .stream()
                        .flatMap(i ->
                                i.orthogonalNeighbours().stream()
                                        .filter(Objects::nonNull)
                                        .filter(p -> p.value() == currentHeight.get() + 1)
                        )
                        .toList();
                currentHeight.incrementAndGet();
            }
            sum += points.size();;

        }
        return Integer.toString(sum);
    }
}
