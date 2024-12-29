package net.maesierra.adventOfCode2024.utils;

import net.maesierra.adventOfCode2024.utils.Directions.Direction;

import java.util.Objects;

public record Position(int row, int col) implements Comparable<Position> {

    public final static Position ZERO_ZERO = new Position(0, 0);

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

    @Override
    public int compareTo(Position o) {
        int compareRows = Integer.compare(row, o.row);
        return compareRows != 0 ? compareRows : Integer.compare(col, o.col);
    }

    public Position multiply(int multiplier) {
        return new Position(row * multiplier, col * multiplier);
    }

    public Position add(int n) {
        return new Position(row + n, col + n);
    }

    public Position add(int dr, int dc) {
        return new Position(row + dr, col + dc);
    }

    public Position moveOrthogonally(int distance, Direction direction) {
        return move(distance, direction, true);
    }
    public Position move(int distance, Direction direction, boolean orthogonalOnly) {
        return switch (direction) {
            case NORTH_EAST -> orthogonalOnly ? this : new Position(row - distance, col + distance);
            case EAST -> new Position(row, col + distance);
            case SOUTH_EAST -> orthogonalOnly ? this : new Position(row + distance, col + distance);
            case SOUTH -> new Position(row + distance, col);
            case SOUTH_WEST -> orthogonalOnly ? this : new Position(row + distance, col - distance);
            case WEST -> new Position(row, col - distance);
            case NORTH_WEST -> orthogonalOnly ? this : new Position(row - distance, col - distance);
            case NORTH -> new Position(row - distance, col);
        };
    }

    public int manhattanDistance(Position other) {
        return Math.abs(this.row - other.row) + Math.abs(this.col - other.col);
    }
}
