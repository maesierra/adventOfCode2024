package net.maesierra.adventOfCode2024.solutions.day17;

import net.maesierra.adventOfCode2024.solutions.day17.Day17.Computer;
import net.maesierra.adventOfCode2024.solutions.day17.Day17.Computer.Registers;
import net.maesierra.adventOfCode2024.solutions.day17.Day17.Program;
import net.maesierra.adventOfCode2024.utils.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static net.maesierra.adventOfCode2024.solutions.day17.Day17.Program.toOpcodeList;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static net.maesierra.adventOfCode2024.utils.Logger.Level.INFO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Day17Test {

    @Test
    void testPrograms() {
        Computer computer;

        computer = new Computer(new Registers(new Integer[]{0, 0, 9}));
        computer.program = new Program(toOpcodeList(List.of(2, 6)));
        computer.run();
        assertThat(computer.registers.b, equalTo(1));
        System.out.println("-------------------------------------\n");

        computer = new Computer(new Registers(new Integer[]{10, 0, 0}));
        computer.program = new Program(toOpcodeList(List.of(5,0,5,1,5,4)));
        computer.run();
        assertThat(computer.registers.a, equalTo(10));
        assertThat(computer.output, equalTo(List.of("0","1","2")));
        System.out.println("-------------------------------------\n");

        computer = new Computer(new Registers(new Integer[]{2024, 0, 0}));
        computer.program = new Program(toOpcodeList(List.of(0,1,5,4,3,0)));
        computer.run();
        assertThat(computer.registers.a, equalTo(0));
        assertThat(computer.output, equalTo(List.of("4", "2", "5", "6", "7", "7", "7", "7", "3", "1", "0")));
        System.out.println("-------------------------------------\n");

        computer = new Computer(new Registers(new Integer[]{0, 29, 0}));
        computer.program = new Program(toOpcodeList(List.of(1, 7)));
        computer.run();
        assertThat(computer.registers.b, equalTo(26));
        System.out.println("-------------------------------------\n");

        computer = new Computer(new Registers(new Integer[]{0, 2024, 43690}));
        computer.program = new Program(toOpcodeList(List.of(4, 0)));
        computer.run();
        assertThat(computer.registers.b, equalTo(44354));
        System.out.println("-------------------------------------\n");
    }

    @Test
    void  testPart1() {
        String expected = "4,6,3,5,6,3,5,2,1,0";
        assertThat(part1(new Day17(), "input_17"), equalTo(expected));
    }

    @Test
    void  testPart2() {
        String expected = "117440";
        Logger.setLevel(INFO);
        //Because of the division by 3 of a if we want to stop after 6 output a needs to be zero by then
        //and a gets divided by 8 each iteration, the max value is 262143 (8^6 - 1)
        //We also know that on the previous cycle must be 3 so it will need to be at least 3x8^5
        assertThat(part2(new Day17(), "input_17_2", "98304", "262143"), equalTo(expected));
    }
}