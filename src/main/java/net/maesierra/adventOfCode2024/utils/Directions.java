package net.maesierra.adventOfCode2024.utils;

import java.util.function.Function;
import java.util.stream.Stream;

public record Directions<T>(
        T northWest,
        T north,
        T northEast,
        T east,
        T southEast,
        T south,
        T southWest,
        T west
) {

    public enum Direction {
        NORTH_WEST,
        NORTH,
        NORTH_EAST,
        EAST,
        SOUTH_EAST,
        SOUTH,
        SOUTH_WEST,
        WEST;

        public Direction rotate90Right() {
            return switch (this) {
                case NORTH_WEST -> NORTH_EAST;
                case NORTH -> EAST;
                case NORTH_EAST -> SOUTH_EAST;
                case EAST -> SOUTH;
                case SOUTH_EAST -> SOUTH_WEST;
                case SOUTH -> WEST;
                case SOUTH_WEST -> NORTH_WEST;
                case WEST -> NORTH;
            };
        }
        public Direction rotate90Left() {
            return switch (this) {
                case NORTH_WEST -> SOUTH_WEST;
                case WEST -> SOUTH;
                case SOUTH_WEST -> SOUTH_EAST;
                case SOUTH -> EAST;
                case SOUTH_EAST -> NORTH_EAST;
                case EAST -> NORTH;
                case NORTH_EAST -> NORTH_WEST;
                case NORTH -> WEST;
            };
        }
    }

    public Stream<T> stream() {
        return Stream.of(
                northWest,
                north,
                northEast,
                east,
                southEast,
                south,
                southWest,
                west
        );

    }
    public T get(Direction direction) {
        return switch (direction) {
            case NORTH_WEST -> northWest;
            case NORTH -> north;
            case NORTH_EAST -> northEast;
            case EAST -> east;
            case SOUTH_EAST -> southEast;
            case SOUTH -> south;
            case SOUTH_WEST -> southWest;
            case WEST -> west;
        };
    }

    public <T2> Directions<T2> map(Function<T, T2> mapper) {
        return new Directions<>(
                mapper.apply(northWest),
                mapper.apply(north),
                mapper.apply(northEast),
                mapper.apply(east),
                mapper.apply(southEast),
                mapper.apply(south),
                mapper.apply(southWest),
                mapper.apply(west)
        );
    }
}
