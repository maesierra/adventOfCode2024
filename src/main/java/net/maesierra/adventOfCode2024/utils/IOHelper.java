package net.maesierra.adventOfCode2024.utils;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class IOHelper {

    public static String inputAsString(InputStream input) {
        try {
            return IOUtils.toString(input, UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<String> inputAsStream(InputStream input) {
        return IOUtils.readLines(input, UTF_8).stream();
    }

    public static Stream<String[]> inputAsStream(InputStream input, Pattern regExp) {
        return  inputAsStream(input)
                .map(s -> {
                    Matcher m = regExp.matcher(s);
                    if (m.matches()) {
                        String[] groups = new String[m.groupCount()];
                        for (int i = 1; i <= m.groupCount(); i++) {
                            groups[i - 1] = m.group(i);
                        }
                        return groups;
                    }
                    return new String[]{};
                })
                .filter(groups -> groups.length > 0);

    }

    @SuppressWarnings("unchecked")
    public static Stream<String>[] inputAsTextBlocks(InputStream input) {
        String[] blocks = inputAsString(input).split("\\n\\n");
        return Stream.of(blocks)
                .map(block -> IOUtils.readLines(new ByteArrayInputStream(block.getBytes(UTF_8)), UTF_8).stream())
                .toArray(Stream[]::new);
    }

    public static Matrix<Character> inputAsCharMatrix(InputStream input) {
        return new Matrix<>(inputAsStream(input).map(s -> s.chars().mapToObj(c -> (char) c).toList()));
    }

}
