package net.maesierra.adventOfCode2024.utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class IOHelper {

    public static String inputAsString(InputStream input) {
        try {
            return IOUtils.toString(input, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<String> inputAsStream(InputStream input) {
        return IOUtils.readLines(input, StandardCharsets.UTF_8).stream();
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

}
