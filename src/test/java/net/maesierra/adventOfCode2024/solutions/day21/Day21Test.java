package net.maesierra.adventOfCode2024.solutions.day21;

import net.maesierra.adventOfCode2024.solutions.day21.Day21.DirectionalKeypad;
import net.maesierra.adventOfCode2024.solutions.day21.Day21.KeypadChain;
import net.maesierra.adventOfCode2024.solutions.day21.Day21.NumericKeypad;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part1;
import static net.maesierra.adventOfCode2024.testUtils.TestHelper.part2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

class Day21Test {

    @Test
    void testKeypads() {

        NumericKeypad keypad = new NumericKeypad();
        DirectionalKeypad directionalKeypad = new DirectionalKeypad();
        KeypadChain keypadChain1 = new KeypadChain(List.of(directionalKeypad, directionalKeypad, directionalKeypad, directionalKeypad));
        String aaa = keypadChain1.shortestSequence2("A<");


        Set<String> step1 = directionalKeypad.movesFor("A<");
        System.out.println(step1.stream().map(String::length).distinct().toList());
        Set<String> step2 = new HashSet<>();
        for (var s:step1) {
            step2.addAll(directionalKeypad.movesFor("A" + s));
        }
        System.out.println(step2.stream().map(String::length).distinct().toList());
        Set<String> step3 = new HashSet<>();
        for (var s:step2) {
            step3.addAll(directionalKeypad.movesFor("A" + s));
        }
        System.out.println(step3.stream().map(String::length).distinct().toList());
        Set<String> step4 = new HashSet<>();
        for (var s:step3) {
            step4.addAll(directionalKeypad.movesFor("A" + s));
        }
        System.out.println(step4.stream().map(String::length).distinct().toList());



                assertThat(keypad.movesFor("A029A"), equalTo(Set.of(
                "<A^A>^^AvvvA",
                "<A^A^>^AvvvA",
                "<A^A^^>AvvvA"
        )));


        Set<String> allOptions = Set.of("<A^A>^^AvvvA", "<A^A^>^AvvvA", "<A^A^^>AvvvA").stream()
                .flatMap(code -> directionalKeypad.movesFor("A" + code).stream())
                .collect(Collectors.toSet());
        assertThat(allOptions, hasItem("v<<A>>^A<A>AvA<^AA>A<vAAA>^A"));
        KeypadChain keypadChain = new KeypadChain(List.of(keypad, directionalKeypad));
        assertThat(keypadChain.shortestSequence("A029A").length(), equalTo("v<<A>>^A<A>AvA<^AA>A<vAAA>^A".length()));

        KeypadChain keypadChain2 = new KeypadChain(List.of(keypad, directionalKeypad, new DirectionalKeypad()));
        assertThat(keypadChain2.shortestSequence("A029A").length(), equalTo("<vA<AA>>^AvAA<^A>A<v<A>>^AvA^A<vA>^A<v<A>^A>AAvA^A<v<A>A>^AAAvA<^A>A".length()));


    }

    @Test
    void  testPart1() {
        String expected = "126384";
        assertThat(part1(new Day21(), "input_21"), equalTo(expected));
    }

    @Test
    void  testPart2() {
        String expected = "09:30, 4h 20min, 13:50, 46.00€";
        assertThat(part2(new Day21(), "input_21"), equalTo(expected));
    }
}