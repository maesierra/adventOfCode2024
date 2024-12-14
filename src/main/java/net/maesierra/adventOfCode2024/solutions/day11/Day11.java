package net.maesierra.adventOfCode2024.solutions.day11;

import net.maesierra.adventOfCode2024.Runner;
import net.maesierra.adventOfCode2024.utils.Logger;
import org.apache.commons.lang3.function.TriFunction;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsString;

public class Day11 implements Runner.Solution {

    private final static BigInteger multiplier = new BigInteger("2024");

    private final Map<BigInteger, List<BigInteger>> rules = new HashMap<>();

    static class Histogram<K> {
        private final Map<K, Long> histogram = new HashMap<>();

        public Histogram() {

        }

        public Histogram(Stream<K> init) {
            init.forEach(this::add);
        }

        public Histogram<K> add(K value) {
            return add(value, 1L);
        }

        public Histogram<K> add(K value, long c) {
            if (!histogram.containsKey(value)) {
                histogram.put(value, c);
            } else {
                histogram.put(value, histogram.get(value) + c);
            }
            return this;
        }

        public void forEach(BiConsumer<K, Long> consumer) {
            histogram.forEach(consumer);
        }

        public Histogram<K> reduce(TriFunction<Histogram<K>, K, Long, Histogram<K>> reducer) {
            return histogram.entrySet().stream().reduce(
                    new Histogram<>(),
                    (acc, entry) -> reducer.apply(acc, entry.getKey(), entry.getValue()),
                    (a, b) -> a
            );
        }

        public long count() {
            return histogram.values().stream().mapToLong(i -> i).sum();
        }

    }


    private List<BigInteger> change(BigInteger stone) {
        if (stone.equals(BigInteger.ZERO)) {
            return List.of(BigInteger.ONE);
        }
        String str = stone.toString();
        if (str.length() % 2 == 0) {
            int partSize = str.length() / 2;
            return List.of(
                    new BigInteger(str.substring(0, partSize)),
                    new BigInteger(str.substring(partSize))
            );
        } else {
            return List.of(stone.multiply(multiplier));
        }
    }

    @Override
    public String part1(InputStream input, String... params) {
        int nIterations = Stream.of(params).map(Integer::parseInt).findFirst().orElse(25);
        Histogram<BigInteger> stones = new Histogram<>(Stream.of(inputAsString(input).trim().split(" ")).map(BigInteger::new));
        for (int i = 0; i < nIterations; i++) {
            Logger.info("Blink %d", i);
            stones = stones.reduce((h, stone, count) -> {
                if (!rules.containsKey(stone)) {
                    rules.put(stone, change(stone));
                }
                rules.get(stone).forEach(s -> h.add(s, count));
                return h;
            });
        }
        return Long.toString(stones.count());
    }

    @Override
    public String part2(InputStream input, String... params) {
        return part1(input, "75");
    }
}