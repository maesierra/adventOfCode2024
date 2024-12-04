package net.maesierra.adventOfCode2024.utils;

import java.util.stream.Stream;

public record Directions<T>(
        T topLeft,
        T top,
        T topRight,
        T right,
        T bottomRight,
        T bottom,
        T bottomLeft,
        T left
) {
    public Stream<T> stream() {
        return Stream.of(
                topLeft,
                top,
                topRight,
                right,
                bottomRight,
                bottom,
                bottomLeft,
                left
        );
    }
}
