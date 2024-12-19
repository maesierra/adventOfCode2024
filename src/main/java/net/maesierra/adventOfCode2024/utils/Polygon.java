package net.maesierra.adventOfCode2024.utils;

import java.util.ArrayList;

public class Polygon extends ArrayList<Position> {

    @Override
    public boolean add(Position position) {
        if (isEmpty()) {
            return super.add(position);
        }
        Position current = get(size() - 1);
        if (current.equals(position)) {
            return false;
        }
        return super.add(position);
    }

    public Position last() {
        return get(size() - 1);
    }

    public Position first() {
        return get(0);
    }

    public record BoundingBox(Position topLeft, Position topRight, Position bottomLeft, Position bottomRight) {

        public int height() {
            return bottomLeft.row() - topLeft.row();
        }

        public int width() {
            return bottomRight.col() - bottomLeft.col();
        }
        public BoundingBox(int minRow, int minCol, int maxRow, int maxCol) {
            this(
                    new Position(minRow, minCol),
                    new Position(minRow, maxCol),
                    new Position(maxRow, minCol),
                    new Position(maxRow, maxCol)
            );
        }

        public boolean contains(Position position) {
            return position.row() >= topLeft.row() && position.col() >= topLeft.col() &&
                   position.row() <= bottomRight.row() && position.col() <= bottomRight.col();
        }
    }
    public BoundingBox boundingBox() {
        int minRow = stream().mapToInt(Position::row).min().orElseThrow();
        int minCol = stream().mapToInt(Position::col).min().orElseThrow();
        int maxRow = stream().mapToInt(Position::row).max().orElseThrow();
        int maxCol = stream().mapToInt(Position::col).max().orElseThrow();
        return new BoundingBox(minRow, minCol, maxRow, maxCol);
    }


}
