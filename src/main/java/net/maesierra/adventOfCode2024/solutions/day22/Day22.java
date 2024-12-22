package net.maesierra.adventOfCode2024.solutions.day22;

import net.maesierra.adventOfCode2024.Runner;
import net.maesierra.adventOfCode2024.solutions.day22.Day22.SecretGenerator.Sequence;

import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsStream;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsString;

public class Day22 implements Runner.Solution {

    public static class SecretGenerator {
        record PriceChange(int nBananas, Optional<Integer> diff) {

        }
        public record Sequence(int n1, int n2, int n3, int n4) {

        }
        private final List<PriceChange> prices = new ArrayList<>();
        private final HashMap<Sequence, Integer> bananasPerSequence = new HashMap<>();
        private long value;

        public HashMap<Sequence, Integer> getBananasPerSequence() {
            return bananasPerSequence;
        }

        private void addPriceChange(long value) {
            int nBananas = (int) (value % 10);
            if (prices.isEmpty()) {
                prices.add(new PriceChange(nBananas, Optional.empty()));
            } else {
                int nChanges = prices.size();
                int diff = nBananas - prices.get(nChanges - 1).nBananas;
                prices.add(new PriceChange(nBananas, Optional.of(diff)));
                if (nChanges >= 4) {
                    Sequence seq = new Sequence(
                            prices.get(nChanges - 3).diff.orElseThrow(),
                            prices.get(nChanges - 2).diff.orElseThrow(),
                            prices.get(nChanges - 1).diff.orElseThrow(),
                            prices.get(nChanges).diff.orElseThrow());
                    if (!bananasPerSequence.containsKey(seq)) {
                        bananasPerSequence.put(seq, nBananas);
                    }
                }
            }
        }
        public SecretGenerator(long value) {
            this.value = value;
            addPriceChange(value);
        }


        public long next() {
            return next(1);
        }

        public long next(int n) {
            for (int i = 0; i < n; i++) {
                value = (value ^ (value *   64)) % 16777216;
                value = (value ^ (value /   32)) % 16777216;
                value = (value ^ (value * 2048)) % 16777216;
                addPriceChange(value);
            }
            return this.value;
        }
    }

    private static Stream<SecretGenerator> parseInput(InputStream input) {
        return inputAsStream(input)
                .map(String::trim)
                .filter(not(String::isEmpty))
                .map(Long::parseLong)
                .map(SecretGenerator::new);
    }

    @Override
    public String part1(InputStream input, String... params) {
        long res = parseInput(input)
                .mapToLong(secretGenerator -> secretGenerator.next(2000))
                .sum();

        return Long.toString(res);
    }

    @Override
    public String part2(InputStream input, String... params) {
        List<SecretGenerator> secrets = parseInput(input)
                .peek(secretGenerator -> secretGenerator.next(2000))
                .toList();
        Map<Sequence, Long> bananasPerSequence = new HashMap<>();
        for (var secret:secrets) {
            for (var seq:secret.bananasPerSequence.keySet()) {
                bananasPerSequence.put(
                        seq,
                        bananasPerSequence.getOrDefault(seq, 0L) + secret.bananasPerSequence.getOrDefault(seq, 0));
            }
        }
        Entry<Sequence, Long> entry = bananasPerSequence.entrySet().stream().max(Entry.comparingByValue()).orElseThrow();
        long res = entry.getValue();
        return Long.toString(res);
    }
}
