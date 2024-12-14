package net.maesierra.adventOfCode2024.solutions.day5;

import net.maesierra.adventOfCode2024.Runner;
import net.maesierra.adventOfCode2024.utils.Logger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsTextBlocks;

public class Day5 implements Runner.Solution {

    record Page(int number, Set<Integer> beforeThan) {
        public Page(int number) {
            this(number, new HashSet<>());
        }

        @Override
        public String toString() {
            return "%d < %s".formatted(
                    this.number,
                    this.beforeThan.stream().map(p -> Integer.toString(p)).collect(Collectors.joining(", "))
            );
        }
    }

    record Update(List<Page> pages) {

        public Page middle() {
            return pages.get(pages.size() / 2);
        }


        public boolean isValid() {
            int nPages = pages.size();
            for (int i = 0; i < nPages; i++) {
                Page page = pages.get(i);
                for (int j = i + 1; j < nPages; j++) {
                    Page other = pages.get(j);
                    if (other.beforeThan.contains(page.number())) {
                        return false;
                    }
                }

            }
            return true;
        }

        public Update fix() {
            Comparator<Page> pageComparator = (p1, p2) -> {
                int res = 0;
                if (p1.number() != p2.number()) {
                    if (p2.beforeThan().contains(p1.number())) {
                        res = 1;
                    } else {
                        res = -1;
                    }
                }
                Logger.debug("Comparing %d <> %s => %d", p1.number(), p2, res);
                return res;
            };
            List<Page> newList = new ArrayList<>(pages());
            newList.sort(pageComparator);
            return new Update(newList);
        }

        @Override
        public String toString() {
            return pages().stream().map(p -> Integer.toString(p.number)).collect(Collectors.joining(", "));
        }
    }

    private static List<Update> parseInput(InputStream input) {
        var blocks = inputAsTextBlocks(input);
        HashMap<Integer, Page> pages = blocks[0].reduce(new HashMap<>(),
                (map, line) -> {
                    String[] parts = line.split("\\|");
                    Page page1 = map.computeIfAbsent(parseInt(parts[0]), Page::new);
                    Page page2 = map.computeIfAbsent(parseInt(parts[1]), Page::new);
                    page1.beforeThan().add(page2.number());
                    return map;
                },
                (a, b) -> a);
        return blocks[1]
                .map(line -> {
                    List<Page> updatedPages = Stream.of(line.split(","))
                            .map(number -> pages.computeIfAbsent(parseInt(number), Page::new))
                            .toList();
                    return new Update(updatedPages);
                }).toList();
    }

    @Override
    public String part1(InputStream input, String... params) {
        int total = parseInput(input).stream().reduce(
                0,
                (sum, update) -> {
                    if (update.isValid()) {
                        return sum + update.middle().number();
                    }
                    return sum;
                },
                (a, b) -> a
        );
        return Integer.toString(total);
    }

    @Override
    public String part2(InputStream input, String... params) {
        List<Update> updates = parseInput(input);

        Logger.info("Total %d Invalid updates %d",
                updates.size(),
                updates.stream().filter(Update::isValid).count()
        );


        int total = updates
                .stream()
                .filter(Predicate.not(Update::isValid))
                .mapToInt(update -> {
                    Update fixed = update.fix();
                    Logger.debug("%s to %s", update, fixed);
                    return fixed.middle().number();
                })
                .sum();

        return Integer.toString(total);
    }
}
