package net.maesierra.adventOfCode2024.solutions.day3;

import net.maesierra.adventOfCode2024.Main;

import java.io.InputStream;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsString;

public class Day3 implements Main.Solution {
    public static class Machine {
        private Long accumulator;
        private boolean enabled;

        public Machine() {
            this.accumulator = 0L;
            this.enabled = true;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public Machine add(long i) {
            this.accumulator += i;
            return this;
        }

        public Machine enable(boolean value) {
            this.enabled = value;
            return this;
        }
    }

    sealed interface Instruction permits Mul, Do, DoNot {
        Machine run(Machine machine);
    }

    record Mul(Integer mul1, Integer mul2) implements Instruction {

        @Override
        public Machine run(Machine machine) {
            if (!machine.isEnabled()) {
                return machine;
            }
            return machine.add((long) mul1() * mul2());
        }
    }
    record Do() implements Instruction {

        @Override
        public Machine run(Machine machine) {
            return machine.enable(true);
        }
    }
    record DoNot() implements Instruction {

        @Override
        public Machine run(Machine machine) {
            return machine.enable(false);
        }
    }

    private Instruction parse(MatchResult matchResult) {
        if (matchResult.group(1) != null) {
            return new Mul(
                    Integer.valueOf(matchResult.group(2)),
                    Integer.valueOf(matchResult.group(3))
            );
        } else if (matchResult.group(4) != null) {
            return new Do();
        } else {
            return new DoNot();
        }
    }

    public static final Pattern INSTRUCTIONS_PATTERN = Pattern.compile("(mul)[(](\\d+),(\\d+)[)]|(do)\\(\\)|(don't)\\(\\)");

    @Override
    public String part1(InputStream input, String... params) {
        Machine machine = INSTRUCTIONS_PATTERN.matcher(inputAsString(input))
                .results()
                .map(this::parse)
                .filter(i -> i instanceof Mul)
                .reduce(new Machine(), (m, i) -> i.run(m), (a, b) -> a);
        return Long.toString(machine.accumulator);
    }

    @Override
    public String part2(InputStream input, String... params) {
        Machine machine = INSTRUCTIONS_PATTERN.matcher(inputAsString(input))
                .results()
                .map(this::parse)
                .reduce(new Machine(), (m, i) -> i.run(m), (a, b) -> a);
        return Long.toString(machine.accumulator);
    }
}
