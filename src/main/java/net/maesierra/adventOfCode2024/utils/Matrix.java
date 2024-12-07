package net.maesierra.adventOfCode2024.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Matrix<T> {

    public static class Row<T> {
        private final int n;
        private final List<T> row;
        private final Matrix<T> matrix;
        private final List<Item<T>> items;

        public Row(int n, List<T> row, Matrix<T> matrix) {
            this.n = n;
            this.row = row;
            this.matrix = matrix;
            this.items = row.stream()
                    .reduce(
                            new ArrayList<>(),
                            (items, i) -> {
                                items.add(new Item<>(this.n, items.size(), i, this.matrix));
                                return items;
                            },
                            (a, b) -> a);

        }

        public List<Item<T>> items() {
            return items;
        }

        public int n() {
            return n;
        }

        public List<T> row() {
            return row;
        }

        public Matrix<T> matrix() {
            return matrix;
        }
        public Item<T> at(int col) {
            return items.get(col);
        }
    }

    public record Item<T>(int row, int column, T value, Matrix<T> matrix) {
        public Position position() {
            return new Position(row, column);
        }
        public Directions<List<Item<T>>> neighbours(int radius) {
            List<Item<T>> northWest = new ArrayList<>(radius);
            List<Item<T>> north = new ArrayList<>(radius);
            List<Item<T>> northEast = new ArrayList<>(radius);
            List<Item<T>> east = new ArrayList<>(radius);
            List<Item<T>> southEast = new ArrayList<>(radius);
            List<Item<T>> south = new ArrayList<>(radius);
            List<Item<T>> southWest = new ArrayList<>(radius);
            List<Item<T>> west = new ArrayList<>(radius);
            for (int i = 0; i < radius; i++) {
                int row = this.row();
                int column = this.column();
                int rowTop = row - i;
                int rowBottom = row + i;
                int columnLeft = column - i;
                int columnRight = column + i;
                if (this.matrix.isIn(rowTop, columnLeft)) {
                    northWest.add(matrix.at(rowTop, columnLeft));
                }
                if (this.matrix.isIn(rowTop, column)) {
                    north.add(matrix.at(rowTop, column));
                }
                if (this.matrix.isIn(rowTop, columnRight)) {
                    northEast.add(matrix.at(rowTop, columnRight));
                }
                if (this.matrix.isIn(row, columnRight)) {
                    east.add(matrix.at(row, columnRight));
                }
                if (this.matrix.isIn(rowBottom, columnRight)) {
                    southEast.add(matrix.at(rowBottom, columnRight));
                }
                if (this.matrix.isIn(rowBottom, column)) {
                    south.add(matrix.at(rowBottom, column));
                }
                if (this.matrix.isIn(rowBottom, columnLeft)) {
                    southWest.add(matrix.at(rowBottom, columnLeft));
                }
                if (this.matrix.isIn(row, columnLeft)) {
                    west.add(matrix.at(row, columnLeft));
                }
            }
            return new Directions<>(
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
    }

    public Item<T> at(int row, int col) {
        return this.rows.get(row).at(col);
    }
    public Item<T> at(Position pos) {
        return at(pos.row(), pos.col());
    }

    private final List<Row<T>> rows;
    private final int nRows;
    private final int nCols;

    public Matrix(Stream<List<T>> rows) {
        this.rows = rows.reduce(new ArrayList<>(), (r, s) -> {
            r.add(new Row<>(r.size(), s, this));
            return r;
        }, (a, b) -> a);
        this.nRows = this.rows.size();
        if (this.nRows > 0) {
            this.nCols = this.rows.get(0).row().size();
        } else {
            this.nCols = 0;
        }
    }
    public Matrix(Matrix<T> other) {
        this.rows = other.rows;
        this.nRows = other.nRows;
        this.nCols = other.nCols;
    }
    public Stream<Row<T>> rows() {
        return rows.stream();
    }

    public int nRows() {
        return nRows;
    }

    public int nCols() {
        return nCols;
    }

    public Stream<Item<T>> items() {
        return rows.stream().flatMap(r -> r.items().stream());
    }

    public boolean isIn(int row, int col) {
        return (row >= 0 && row < this.nRows) && (col >= 0 && col < this.nCols);
    }
    public boolean isIn(Position position) {
        return isIn(position.row(), position.col());
    }

    public <T2> Matrix<T2> map(Function<Item<T>, T2> mapper) {
        return new Matrix<>(this.rows()
                .map(r -> r.items().stream().map(mapper).toList())
        );
    }

    public String toString() {
        return toString(i -> i.value().toString());
    }
    public String toString(Function<Item<T>, String> formatter) {
        return this.rows().map(r -> {
                    return r.items().stream().map(formatter).collect(Collectors.joining());
                })
                .collect(Collectors.joining("\n"));
    }
}