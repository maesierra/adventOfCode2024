package net.maesierra.adventOfCode2024.solutions.day1;

import net.maesierra.adventOfCode2024.Runner;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.regex.Pattern;

import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsStream;

public class Day1 implements Runner.Solution {

    public static final Pattern LINE_REGEXP = Pattern.compile("(\\d+) +(\\d+)");

    record Pair<T>(T left, T right) {}

    @Override
    public String part1(InputStream input, String... params) {
        Pair<PriorityQueue<Integer>> lists = inputAsStream(input, LINE_REGEXP).reduce(
                new Pair<>(new PriorityQueue<>(), new PriorityQueue<>()),
                (l, groups) -> {
                    l.left().add(Integer.valueOf(groups[0]));
                    l.right().add(Integer.valueOf(groups[1]));
                    return l;
                },
                (a, b) -> a

        );

        System.out.println("Left total: %d, Right total: %d".formatted(lists.left().size(), lists.right().size()));
        int sum = 0;
        while (!lists.left().isEmpty() && !lists.right().isEmpty()) {
            int minLeft = lists.left().poll();
            int minRight = lists.right().poll();
            System.out.println("Comparing %d %d".formatted(minLeft, minRight));
            sum += Math.abs(minLeft - minRight);
        }

        return Integer.toString(sum);
    }

    @Override
    public String part2(InputStream input, String... params) {
        record Lists(List<Integer> left, Map<Integer, Integer> histogram) {}
        Lists lists = inputAsStream(input, LINE_REGEXP).reduce(
                new Lists(new ArrayList<>(), new HashMap<>()),
                (p, groups) -> {
                    Integer leftValue = Integer.valueOf(groups[0]);
                    Integer rightValue = Integer.valueOf(groups[1]);
                    p.left().add(leftValue);
                    p.histogram().put(
                            rightValue,
                            p.histogram().getOrDefault(rightValue, 0) + 1
                    );
                    return p;
                },
                (a, b) -> a
        );
        Integer sum = lists.left().stream()
                .reduce(0, (s, n) -> s + (n * lists.histogram().getOrDefault(n, 0)));
        return Integer.toString(sum);
    }
}
