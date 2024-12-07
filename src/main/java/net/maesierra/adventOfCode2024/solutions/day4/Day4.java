package net.maesierra.adventOfCode2024.solutions.day4;

import net.maesierra.adventOfCode2024.Main;
import net.maesierra.adventOfCode2024.utils.Matrix;
import net.maesierra.adventOfCode2024.utils.Directions;

import java.io.InputStream;
import java.util.stream.Collectors;

import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsCharMatrix;

public class Day4 implements Main.Solution {

    @Override
    public String part1(InputStream input, String... params) {
        Matrix<Character> matrix = inputAsCharMatrix(input);
        long res = matrix.items().reduce(
                0L,
                (total, item) -> {
                    if (item.value().equals('X')) {
                        return total + item.neighbours(4)
                                .map(l -> l.stream().map(i -> i.value().toString()).collect(Collectors.joining()))
                                .stream()
                                .filter(l -> l.equals("XMAS")).count();
                    }
                    return total;
                },
                (a, b) -> a
        );
        return Long.toString(res);
    }

    @Override
    public String part2(InputStream input, String... params) {
        Matrix<Character> matrix = inputAsCharMatrix(input);
        long res = matrix.items().reduce(
                0L,
                (total, item) -> {
                    if (item.value().equals('A')) {
                        Directions<String> neighbours = item
                                .neighbours(2)
                                .map(l -> l.stream().map(i -> i.value().toString()).collect(Collectors.joining()));
                        String diagonals = (neighbours.northWest() + neighbours.northEast() + neighbours.southEast() + neighbours.southWest())
                                .replace("A", "");
                        if (diagonals.equals("MSSM") || diagonals.equals("SSMM")  || diagonals.equals("SMMS") ||  diagonals.equals("MMSS")) {
                            return total + 1;
                        }
                    }
                    return total;
                },
                (a, b) -> a
        );
        return Long.toString(res);
    }
}
