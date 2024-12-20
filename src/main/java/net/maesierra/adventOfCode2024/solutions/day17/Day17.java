package net.maesierra.adventOfCode2024.solutions.day17;

import net.maesierra.adventOfCode2024.Runner;
import net.maesierra.adventOfCode2024.solutions.day17.Day17.Computer.Registers;
import net.maesierra.adventOfCode2024.utils.Logger;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.maesierra.adventOfCode2024.solutions.day17.Day17.OperandType.COMBO;
import static net.maesierra.adventOfCode2024.solutions.day17.Day17.OperandType.LITERAL;
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
    public String part2(InputStream input, String... params) {
        Computer computer = parseInput(input);
        //By understanding the program we know that
        //Each iteration a is divided by 8. If we want 16 outputs -> 16 cycles
        //Output is the value of b after some operations with a and c. But it only takes into account the last 3 bits of a value
        //eg in iteration 16 we know that a must be between zero and 8 (3 bits number)
        //   that number must be zero (the output on iteration 16)
        //if we go to the previous iteration that number will become the most significant bits

        List<String> expectedOutput = computer.program.opcodes.stream()
                .flatMap(o -> Stream.of(o.instruction, o.operand))
                .map(i -> Integer.toString(i))
                .toList();
        Deque<Long> values = new ArrayDeque<>();
        values.add(0L);
        for (int i = expectedOutput.size() - 1; i >= 0; i--) {
            List<String> output = expectedOutput.subList(i, expectedOutput.size());
            List<Long> res = new ArrayList<>();
            while (!values.isEmpty()) {
                long currentValue = (values.pop() << 3);
                for (int n = 0; n < 8; n++) {
                    computer.reset();
                    computer.registers.a = currentValue + n;
                    computer.run();
                    if (computer.output.equals(output)) {
                        res.add(currentValue + n);
                    }
                }
            }
            values.addAll(res);
        }
        for (var l:values.stream().sorted().toList()){
            computer.reset();
            computer.registers.a = l;
            computer.run();
            if (computer.output.equals(expectedOutput)) {
                return Long.toString(l);
            }
        };
        throw new RuntimeException("No solution found!!!");
    }

}
