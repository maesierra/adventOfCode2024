package net.maesierra.adventOfCode2024.solutions.day7;

import net.maesierra.adventOfCode2024.Runner;
import net.maesierra.adventOfCode2024.utils.Logger;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsStream;

public class Day7 implements Runner.Solution {


    record Equation(long result, List<Long> numbers) {

            boolean canBeSolved(boolean useConcatenation) {
                if (numbers.size() == 1) {
                    return result == numbers.get(0);
                }
                if (numbers.size() == 2) {
                    Long n1 = numbers.get(0);
                    Long n2 = numbers.get(1);
                    return result == n1 + n2 ||
                           result == n1 * n2 ||
                            (useConcatenation && Long.toString(result).equals("%d%d".formatted(n1, n2))) ;
                }
                long last = numbers.get(numbers.size() - 1);
                List<Long> rest = numbers().subList(0, numbers().size() - 1);
                if (result - last > 0) {
                    if (new Equation(result - last, rest).canBeSolved(useConcatenation)) {
                        return true;
                    }
                }
                if (result % last == 0) {
                    if (new Equation(result / last, rest).canBeSolved(useConcatenation)) {
                        return true;
                    }
                }
                if (useConcatenation) {
                    long nDigits = BigInteger.TEN.pow(Long.toString(last).length()).longValue();
                    if ((result - last) % nDigits  == 0) {
                        return new Equation((result - last) / nDigits, rest).canBeSolved(true);
                    }
                }
                return false;
            }


        @Override
        public String toString() {
            return "%s: %s".formatted(result, numbers());
        }
    }

    private static Stream<Equation> parseInput(InputStream input) {
        return inputAsStream(input, Pattern.compile("(\\d+): (.*)")).map(groups -> new Equation(
                Long.parseLong(groups[0]),
                Stream.of(groups[1].split(" ")).map(Long::parseLong).toList()));
    }

    @Override
    public String part1(InputStream input, String... params) {
        long res = parseInput(input)
                .filter(equation -> {
                    boolean canBeSolved = equation.canBeSolved(false);
                    Logger.debug("%s => %s", equation, canBeSolved);
                    return canBeSolved;
                })
                .mapToLong(Equation::result)
                .sum();
        return Long.toString(res);
    }

    @Override
    public String part2(InputStream input, String... params) {
        long res = parseInput(input)
                .filter(equation -> {
                    boolean canBeSolved = equation.canBeSolved(true);
                    Logger.debug("%s => %s", equation, canBeSolved);
                    return canBeSolved;
                })
                .mapToLong(Equation::result)
                .sum();
        return Long.toString(res);
    }
}
