package net.maesierra.adventOfCode2024.solutions.day17;

import net.maesierra.adventOfCode2024.Runner;
import net.maesierra.adventOfCode2024.solutions.day17.Day17.Computer.Registers;
import net.maesierra.adventOfCode2024.utils.Logger;
import org.apache.commons.lang3.Range;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static java.util.Objects.requireNonNull;
import static net.maesierra.adventOfCode2024.solutions.day17.Day17.OperandType.COMBO;
import static net.maesierra.adventOfCode2024.solutions.day17.Day17.OperandType.LITERAL;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsString;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsTextBlocks;

public class Day17 implements Runner.Solution {

    private final static BigDecimal TWO = new BigDecimal(2);

    static class Computer {
        static class Registers {

            long a;
            long b;
            long c;
            public Registers(Long[] initValues) {
                this.a = initValues[0];
                this.b = initValues[1];
                this.c = initValues[2];
            }

            public Registers(Integer[] initValues) {
                this.a = initValues[0];
                this.b = initValues[1];
                this.c = initValues[2];
            }

            public Registers(Registers registers) {
                this.a = registers.a;
                this.b = registers.b;
                this.c = registers.c;
            }
        }
        Registers registers;
        private final Registers initialState;
        int instructionPointer = 0;
        Program program;
        final List<String> output = new ArrayList<>();

      public Computer(Registers registers) {
          this.registers = new Registers(registers);
          this.initialState = registers;
      }

      public void reset() {
          this.instructionPointer = 0;
          this.output.clear();
          this.registers = new Registers(initialState);
      }


      boolean isRunning() {
          return this.instructionPointer < program.size();
      }
      public void run() {
          run((c -> false));
      }

      public void run(Predicate<Computer> exitCondition) {
          while (isRunning() &&!exitCondition.test(this)) {
              Instruction instruction = program.instructionAt(instructionPointer);
              Logger.debug("Running %s".formatted(instruction));
              instruction.run(this);
          }
      }

        public long getOperandValue(int opcode, OperandType type) {
          return switch (type) {
                case LITERAL -> opcode;
                case COMBO -> switch (opcode) {
                    case 4 -> registers.a;
                    case 5 -> registers.b;
                    case 6 -> registers.c;
                    case 7 -> throw new RuntimeException("Reserved operand.");
                    default -> opcode;
                };
          };
        }
    }
    record Opcode(int instruction, int operand) {

    }

    enum OperandType {
        LITERAL,
        COMBO
    }
    sealed interface Instruction permits Adv,Bxl, Bst, Jnz, Bxc, Out, Bdv, Cdv {
        void run(Computer computer);
    }

    record Adv(Opcode opcode) implements Instruction {
        @Override
        public void run(Computer computer) {
            long operand = computer.getOperandValue(opcode.operand, COMBO);
            long numerator = computer.registers.a;
            int denominator = TWO.pow((int) operand).intValue();
            computer.registers.a = numerator / denominator;
            computer.instructionPointer ++;
            Logger.debug("a = a / %d => %d", denominator, computer.registers.a);
        }

    }

    record Bxl(Opcode opcode) implements Instruction {
        public void run(Computer computer) {
            long operand = computer.getOperandValue(opcode.operand, LITERAL);
            computer.registers.b = computer.registers.b ^ operand;
            computer.instructionPointer ++;
            Logger.debug("b = b xor %d => %d", operand, computer.registers.b);
        }
    }

    record Bst(Opcode opcode) implements Instruction {
        public void run(Computer computer) {
            long operand = computer.getOperandValue(opcode.operand, COMBO);
            computer.registers.b = operand % 8;
            computer.instructionPointer ++;
            Logger.debug("b = %d mod 8 => %d", operand, computer.registers.a);
        }
    }

    record Jnz(Opcode opcode) implements Instruction {
        public void run(Computer computer) {
            if (computer.registers.a != 0) {
                int operandValue = (int) computer.getOperandValue(opcode.operand, LITERAL);
                computer.instructionPointer = operandValue;
                Logger.debug("jump %d", operandValue);
            } else {
                computer.instructionPointer ++;
                Logger.debug("jump ignored");
            }
        }
    }

    record Bxc(Opcode opcode) implements Instruction {
        public void run(Computer computer) {
            computer.registers.b = computer.registers.b ^ computer.registers.c;
            computer.instructionPointer ++;
            Logger.debug("b = b xor c => %d", computer.registers.b);
        }
    }

    record Out(Opcode opcode) implements Instruction {
        public void run(Computer computer) {
            long operand = computer.getOperandValue(opcode.operand, COMBO);
            computer.output.add(Long.toString(operand % 8));
            computer.instructionPointer ++;
            Logger.debug("out %d", operand);
        }
    }

    record Bdv(Opcode opcode) implements Instruction {
        public void run(Computer computer) {
            int operand = (int) computer.getOperandValue(opcode.operand, COMBO);
            long numerator = computer.registers.a;
            long denominator = TWO.pow(operand).intValue();
            computer.registers.b = numerator / denominator;
            computer.instructionPointer ++;
            Logger.debug("b = a / %d => %d", denominator, computer.registers.b);
        }
    }

    record Cdv(Opcode opcode) implements Instruction {
        public void run(Computer computer) {
            int operand = (int) computer.getOperandValue(opcode.operand, COMBO);
            long numerator = computer.registers.a;
            int denominator = TWO.pow(operand).intValue();
            computer.registers.c = numerator / denominator;
            computer.instructionPointer ++;
            Logger.debug("c = a / %d => %d", denominator, computer.registers.c);
        }
    }

    record Program(List<Opcode> opcodes) {

        public static List<Opcode> toOpcodeList(List<Integer> numbers) {
            List<Opcode> opcodeList = new ArrayList<>();
            for (int i = 0; i < numbers.size(); i+=2) {
                opcodeList.add(new Opcode(numbers.get(i), numbers.get(i + 1)));
            }
            return opcodeList;
        }
        public int size() {
            return opcodes.size();
        }
        Instruction instructionAt(int position) {
            Opcode opcode = opcodes.get(position);
            return switch (opcode.instruction) {
                case 0 -> new Adv(opcode);
                case 1 -> new Bxl(opcode);
                case 2 -> new Bst(opcode);
                case 3 -> new Jnz(opcode);
                case 4 -> new Bxc(opcode);
                case 5 -> new Out(opcode);
                case 6 -> new Bdv(opcode);
                case 7 -> new Cdv(opcode);
                default -> throw new RuntimeException("Invalid opcode");
            };
        }

    }

    private static Computer parseInput(InputStream input) {
        Stream<String>[] blocks = inputAsTextBlocks(input);
        Registers registers = new Registers(blocks[0].map(line -> {
            String[] parts = line.split(":");
            return Long.parseLong(parts[1].trim());
        }).toArray(Long[]::new));
        Computer computer = new Computer(registers);
        computer.program = new Program(Program.toOpcodeList(Stream.of(blocks[1].collect(Collectors.joining()).split(",")).map(str -> {
            if (str.contains(":")) {
                str = str.replace("Program: ", "");
            }
            return Integer.parseInt(str.trim());
        }).toList()));
        return computer;
    }

    @Override
    public String part1(InputStream input, String... params) {
        Computer computer = parseInput(input);
        computer.run();
        return String.join(",", computer.output);
    }

    @Override
    public String part2(InputStream input, String... params) {
        Computer computer = parseInput(input);
        // 8^15 35184372088832L
        // 8^16 281474976710656L -1L
        //15 == 0 -> [141659617525749?, 175921860444159]
        //13 == 3 -> [162728209444743?, 167126238924668?], [171524541534773?, 175921860444159]
        //1st with 0 => 141659617525749?
        //
        //Last with 0 => 176171205157951?
        //                   175891124106544L
        long min = params.length > 1 ? Long.parseLong(params[0]) : 141659617525749L;
        long max = params.length > 2 ? Long.parseLong(params[1]) : 175921860444159L;
        List<String> expectedOutput = computer.program.opcodes.stream()
                .flatMap(o -> Stream.of(o.instruction, o.operand))
                .map(i -> Integer.toString(i))
                .toList();
        Map<Integer, Map<String, Integer>> counts = new TreeMap<>(Map.ofEntries(
                entry(0, new HashMap<>()),
                entry(1, new HashMap<>()),
                entry(2, new HashMap<>()),
                entry(3, new HashMap<>()),
                entry(4, new HashMap<>()),
                entry(5, new HashMap<>()),
                entry(6, new HashMap<>()),
                entry(7, new HashMap<>()),
                entry(8, new HashMap<>()),
                entry(9, new HashMap<>()),
                entry(10, new HashMap<>()),
                entry(11, new HashMap<>()),
                entry(12, new HashMap<>()),
                entry(13, new HashMap<>()),
                entry(14, new HashMap<>()),
                entry(15, new HashMap<>())
        ));
        class Range {
            String value;
            long start;
            long end;

            @Override
            public String toString() {
                return "%s [%d, %d]".formatted(value, start, end);
            }
        }
        List<Range> ranges = new ArrayList<>();
        Range currentRange = new Range();
        ranges.add(currentRange);
        currentRange.start = min;
        currentRange.value = "4";

        Random r = new Random();
        long n = 0;
        int randomStep = 1000000000;
        long fixedStep = 0L;
        Supplier<Long> stepRandom = () -> r.nextLong(randomStep);
        Supplier<Long> fixed = () -> 1L;
        for (long i = min; i <= max; i+=stepRandom.get() + fixedStep,n++) {
          computer.reset();
          computer.registers.a = i;
          //We break if any run has more items in the output than the expected size
          computer.run(c -> c.output.size() > expectedOutput.size());
          if (computer.output.equals(expectedOutput)) {
              return Long.toString(i);
          }
            if (computer.output.size() == 16 && !computer.output.get(15).equals("0")) {
                System.out.println(i + "(not good)=>" + String.join(",", computer.output));
            }
            if (computer.output.size() == 16) {
                if (!computer.output.get(14).equals(currentRange.value)) {
                    currentRange.end = i - 1;
                    currentRange = new Range();
                    ranges.add(currentRange);
                    currentRange.value = computer.output.get(14);
                    currentRange.start = i;
                }
            }
            if (n % 10000 == 0) {
                System.out.println("Processing %d".formatted(i));
                System.out.println(i + "=>" + String.join(",", computer.output));
            }
            for (int j = 0; j < computer.output.size(); j++) {
                if (!counts.containsKey(j)) {
                    continue;
                }
                String value = computer.output.get(j);
                counts.get(j).put(value, counts.get(j).getOrDefault(value, 0) + 1);
            }

        }
        currentRange.end = max;
        System.out.println(ranges.stream().filter(r1 -> r1.value.equals("3")).toList());
        System.out.println(counts);
        System.out.println(counts.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
             e -> e.getValue().get(expectedOutput.get(e.getKey()))
        )));
        throw new RuntimeException("No solution found in range");
    }
}
