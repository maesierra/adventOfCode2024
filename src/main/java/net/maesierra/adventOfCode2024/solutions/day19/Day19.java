package net.maesierra.adventOfCode2024.solutions.day19;

import net.maesierra.adventOfCode2024.Runner;
import net.maesierra.adventOfCode2024.utils.Logger;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsTextBlocks;

public class Day19 implements Runner.Solution {


    static class PatternMap {

        private final Set<String> patterns = new HashSet<>();
        private final HashMap<String, Long> cache = new HashMap<>();

        public void put(String pattern) {
            patterns.add(pattern);
        }

        boolean contains(String pattern) {
            return this.patterns.contains(pattern);
        }

        long updateCache(String str, long result) {
            if (!cache.containsKey(str)) {
                cache.put(str, result);
            }
            return result;
        }

        Long matchAll(String str){
            return matchAll(str, false, false);
        }

        boolean match(String str){
            return matchAll(str, true, false) > 0;
        }

        Long matchAll(String str, boolean stopAtFistMatch, boolean ignoreCache){
            Logger.info("matching %s", str);
            long res = 0;
            if (!ignoreCache && cache.containsKey(str)) {
                res = cache.get(str);
                return res;
            }
            if (contains(str)) {
                res = 1;
                if (stopAtFistMatch) {
                    return res;
                }
            }
            int size = 1;
            while (size < str.length()) {
                String pattern = str.substring(0, size);
                if (contains(pattern)) {
                    Long rest = matchAll(str.substring(size), stopAtFistMatch, ignoreCache);
                    res += rest;
                    if (stopAtFistMatch && rest > 0) {
                        return updateCache(str, res);
                    }
                }
                size++;
            }
            //No match found
            return updateCache(str, res);
        }

    }


    private static PatternMap parsePatternMap(Stream<String>[] blocks, boolean match) {
        PatternMap patternMap = new PatternMap();
        List<String> patterns = blocks[0].flatMap(s -> Stream.of(s.split(",")))
                .map(String::trim)
                .toList();
        patterns.forEach(patternMap::put);
        return patternMap;
    }

    @Override
    public String part1(InputStream input, String... params) {
        var blocks = inputAsTextBlocks(input);
        PatternMap patternMap = parsePatternMap(blocks, false);
        AtomicInteger numValid = new AtomicInteger(0);
        AtomicInteger counter = new AtomicInteger(0);
        blocks[1].forEach(str -> {
            Logger.debug("Processing %d %s", counter.getAndIncrement(), str);
            boolean valid = patternMap.match(str);
            if (valid) {
                numValid.incrementAndGet();
            }
        });

        return Integer.toString(numValid.get());
    }

    @Override
    public String part2(InputStream input, String... params) {
        var blocks = inputAsTextBlocks(input);
        PatternMap patternMap = parsePatternMap(blocks, true);
        AtomicLong sum = new AtomicLong(0);
        AtomicInteger counter = new AtomicInteger(0);
        //We need to match the patterns themselves so the cache gets filled
        patternMap.patterns.stream().sorted(Comparator.comparing(String::length)).forEach(pattern -> {
            patternMap.matchAll(pattern, false, true);
        });

        blocks[1].forEach(str -> {
            Long res = patternMap.matchAll(str);
            sum.addAndGet(res);
            System.out.printf("Processing %d %s => %d %n", counter.getAndIncrement(), str, res);
        });

        return Long.toString(sum.get());
    }
}
