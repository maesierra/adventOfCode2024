package net.maesierra.adventOfCode2024.solutions.day25;

import net.maesierra.adventOfCode2024.Runner;
import net.maesierra.adventOfCode2024.utils.Matrix;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsString;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsTextBlocks;

public class Day25 implements Runner.Solution {

    record Key(int[] heights) {
        @Override
        public String toString() {
            return Arrays.toString(heights);
        }
    }
    record Lock(int[] heights) {
        @Override
        public String toString() {
            return Arrays.toString(heights);
        }
    }

    static boolean fits(Key key, Lock lock) {
        for (int i = 0; i < key.heights.length; i++) {
            if (key.heights[i] + lock.heights[i] > 5) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String part1(InputStream input, String... params) {
        List<Lock> locks = new ArrayList<>();
        List<Key> keys = new ArrayList<>();
        Stream.of(inputAsTextBlocks(input)).forEach(
                s -> {
                    Matrix<Character> matrix = new Matrix<>(s.map(s1 -> s1.chars().mapToObj(c -> (char) c).toList()));
                    boolean isLock =
                            matrix.row(0).items().stream().allMatch(i -> i.value().equals('#')) &&
                            matrix.row(matrix.nRows() - 1).items().stream().allMatch(i -> i.value().equals('.'));
                    int[] heights = new int[matrix.nCols()];
                    char heightChar = isLock ? '#' : '.';
                    Arrays.fill(heights, 0);
                    matrix.rows().forEach(r -> {
                        r.items().forEach(i -> {
                            if (i.value() == heightChar) {
                                heights[i.column()] = i.row();
                            }
                        });
                    });
                    if (isLock) {
                        locks.add(new Lock(heights));
                    } else {
                        for (int i = 0; i < heights.length; i++) {
                            heights[i] = matrix.nRows() - 2 - heights[i];
                        }
                        keys.add(new Key(heights));
                    }
                }
        );
        long res = 0;
        for (var lock:locks) {
            for (var key:keys) {
                if (fits(key, lock)) {
                    res ++;
                }
            }
        }
        return Long.toString(res);
    }

    @Override
    public String part2(InputStream input, String... params) {
        return inputAsString(input).toLowerCase();
    }
}
