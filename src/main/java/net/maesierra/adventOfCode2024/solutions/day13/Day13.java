package net.maesierra.adventOfCode2024.solutions.day13;

import net.maesierra.adventOfCode2024.Runner;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsString;

public class Day13 implements Runner.Solution {

    private final static Pattern REGEXP= Pattern.compile("A: X\\+(\\d+), Y\\+(\\d+).*B: X\\+(\\d+), Y\\+(\\d+).*X=(\\d+), Y=(\\d+)", Pattern.DOTALL);
    private final static BigDecimal CONVERSION = new BigDecimal("10000000000000");
    private static final BigDecimal A_COST = new BigDecimal("3");
    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

    record Movement(BigDecimal x, BigDecimal y) {

    }

    record Solution(BigDecimal a, BigDecimal b) {
        BigDecimal cost() {
            return a.multiply(A_COST).add(b);
        }
    }

    record Machine(BigDecimal x, BigDecimal y, Movement a, Movement b) {

        Optional<Solution> solve() {
            // b = (y*a.x - a.y*x) / (a.x*b.y - a.y*b.x)
            BigDecimal timesOnB = y.multiply(a.x).subtract(a.y.multiply(x))
                    .divide(a.x.multiply(b.y).subtract(a.y.multiply(b.x)), MATH_CONTEXT);
            if (timesOnB.toString().contains(".")) {
                return Optional.empty();
            }
            // a = (x - b*b.x) / a.x
            BigDecimal timesOnA = x.subtract(timesOnB.multiply(b.x)).divide(a.x, MATH_CONTEXT);
            return Optional.of(new Solution(
                    timesOnA,
                    timesOnB
            ));
        }

        public Machine adjust(BigDecimal n) {
            return new Machine(x.add(n), y.add(n), a, b);
        }
    }

    private static Stream<Machine> parseMachines(InputStream input) {
        return Stream.of(inputAsString(input).split("\\n\\n")).map(block -> {
            Matcher matcher = REGEXP.matcher(block);
            if (!matcher.find()) {
                throw new RuntimeException("Invalid input");
            }
            return new Machine(
                    new BigDecimal(matcher.group(5)),
                    new BigDecimal(matcher.group(6)),
                    new Movement(new BigDecimal(matcher.group(1)), new BigDecimal(matcher.group(2))),
                    new Movement(new BigDecimal(matcher.group(3)), new BigDecimal(matcher.group(4)))
            );
        });
    }

    @Override
    public String part1(InputStream input, String... params) {
        Stream<Machine> machines = parseMachines(input);
        BigDecimal limit = new BigDecimal("100");
        BigDecimal res = machines.map(m -> m
                .solve()
                .filter(s -> s.a.compareTo(limit) < 0 && s.b.compareTo(limit) < 0)
                .map(Solution::cost)
                .orElse(BigDecimal.ZERO)
        ).reduce(BigDecimal.ZERO, BigDecimal::add);
        return res.toString();
    }

    @Override
    public String part2(InputStream input, String... params) {
        Stream<Machine> machines = parseMachines(input)
                .map(m -> m.adjust(CONVERSION));
        BigDecimal res = machines.map(m -> m
                .solve()
                .map(Solution::cost)
                .orElse(BigDecimal.ZERO)
        ).reduce(BigDecimal.ZERO, BigDecimal::add);
        return res.toString();
    }
}
