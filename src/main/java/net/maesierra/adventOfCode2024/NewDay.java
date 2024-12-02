package net.maesierra.adventOfCode2024;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.filefilter.FalseFileFilter.FALSE;
import static org.apache.commons.io.filefilter.TrueFileFilter.TRUE;

public class NewDay {

    public static void main(String[] args) throws Exception {
        Path currentDir = Paths.get("./src/main/java/net/maesierra/adventOfCode2024/solutions").toAbsolutePath().normalize();
        Path baseSolution = Paths.get("./src/main/java/net/maesierra/adventOfCode2024/solutions/day0/Day0.java").toAbsolutePath().normalize();
        Path testDir = Paths.get("./src/test/java/net/maesierra/adventOfCode2024/solutions").toAbsolutePath().normalize();
        Path test = Paths.get("./src/test/java/net/maesierra/adventOfCode2024/solutions/day0/Day0Test.java").toAbsolutePath().normalize();
        Integer newDay = FileUtils.listFilesAndDirs(currentDir.toFile(), FALSE, TRUE)
                .stream().filter(f -> f.getName().startsWith("day"))
                .sorted()
                .reduce((first, second) -> second)
                .map(f -> 1 + Integer.parseInt(f.getName().replace("day", "")))
                .orElseThrow();
        File newDaySrcPath = new File(currentDir.toFile(), "day%d".formatted(newDay));
        File newDayTestPath = new File(testDir.toFile(), "day%d".formatted(newDay));
        newDaySrcPath.mkdirs();
        newDayTestPath.mkdirs();
        FileUtils.write(
                new File(newDaySrcPath, "Day%d.java".formatted(newDay)),
                FileUtils.readFileToString(baseSolution.toFile(), UTF_8)
                        .replace("Day0", "Day%d".formatted(newDay))
                        .replace("day0", "day%d".formatted(newDay)),
                UTF_8
        );
        FileUtils.write(
                new File(newDayTestPath, "Day%dTest.java".formatted(newDay)),
                FileUtils.readFileToString(test.toFile(), UTF_8)
                        .replace("Day0", "Day%d".formatted(newDay))
                        .replace("day0", "day%d".formatted(newDay))
                        .replace("input_0", "input_%d".formatted(newDay)),
                UTF_8
        );
    }
}