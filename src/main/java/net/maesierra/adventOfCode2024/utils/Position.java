package net.maesierra.adventOfCode2024.utils;

import java.util.Objects;

public record Position(int row, int col) {

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return row == position.row && col == position.col;
    }
}
