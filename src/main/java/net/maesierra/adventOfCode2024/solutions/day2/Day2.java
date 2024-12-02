package net.maesierra.adventOfCode2024.solutions.day2;

import net.maesierra.adventOfCode2024.Main;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsStream;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsString;

public class Day2 implements Main.Solution {

    enum Direction {
        DESC,
        ASC
    }
    record Report(List<Integer> levels) {
        public boolean isSafe() {
            int current = levels().get(0);
            Direction direction = null;
            for (int i = 1; i < levels().size(); i++) {
                int l = levels().get(i);
                int diff = l - current;
                //Difference must be between 1 and 3
                if (Math.abs(diff) < 1 || Math.abs(diff) > 3) {
                    return false;
                }
                if (direction == null) {
                    direction = diff > 0 ? Direction.ASC : Direction.DESC;
                } else if (direction == Direction.ASC && diff < 0) {
                    return false;
                } else if (direction == Direction.DESC && diff > 0) {
                    return false;
                }
                current = l;
            }
            return true;
        }
        public Report remove(int pos) {
            return new Report(Stream.concat(
                    levels().subList(0, pos).stream(),
                    levels().subList(pos + 1, levels().size()).stream()).toList());
        }
    };


    private static Stream<Report> parse(InputStream input) {
        return inputAsStream(input)
                .map(line -> new Report(Arrays.stream(line.split(" ")).map(Integer::valueOf).toList()));
    }

    @Override
    public String part1(InputStream input, String... params) {
        return Long.toString(parse(input)
                .filter(Report::isSafe)
                .count());
    }

    @Override
    public String part2(InputStream input, String... params) {
        return Long.toString(parse(input)
                .filter(r -> {
                    if (r.isSafe()) {
                        return true;
                    }
                    for (int pos= 0; pos< r.levels().size(); pos++) {
                        if (r.remove(pos).isSafe()) {
                            return true;
                        }
                    }
                    return  false;
                })
                .count());
    }
}
