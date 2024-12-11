package net.maesierra.adventOfCode2024.solutions.day11;

import net.maesierra.adventOfCode2024.Main;
import net.maesierra.adventOfCode2024.utils.Logger;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsString;

public class Day11 implements Main.Solution {

    private final static BigInteger multiplier = new BigInteger("2024");


    private Stream<BigInteger> change(BigInteger value) {
        if (value.equals(BigInteger.ZERO)) {
            return Stream.of(BigInteger.ONE);
        }
        String str = value.toString();
        if (str.length() % 2 == 0) {
            int partSize = str.length() / 2;
            return Stream.of(
                    new BigInteger(str.substring(0, partSize)),
                    new BigInteger(str.substring(partSize))
            );
        } else {
            return Stream.of(value.multiply(multiplier));
        }
    }

    @Override
    public String part1(InputStream input, String... params) {
        List<BigInteger> stones = Stream.of(inputAsString(input).trim().split(" ")).map(BigInteger::new).toList();
        for (int i = 0; i < 25; i++) {
            Logger.info("Blink %d %s", i, stones);
            stones = stones.parallelStream().flatMap(this::change).toList();

        }
        return Integer.toString(stones.size());
    }

    @Override
    public String part2(InputStream input, String... params) {
//        List<Stone> stones = Stream.of(inputAsString(input).trim().split(" ")).map(Stone::new).toList();
//        for (int i = 0; i < 75; i++) {
//            Logger.info("Blink %d %d", i, stones.size());
//            stones = stones.stream().flatMap(Stone::change).toList();
//        }
//        return Integer.toString(stones.size());
        return "";
    }
}
