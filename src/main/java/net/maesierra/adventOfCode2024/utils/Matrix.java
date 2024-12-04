package net.maesierra.adventOfCode2024.utils;

import java.util.ArrayList;
import java.util.List;
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
        public Directions<String> neighbours(int radius) {
            StringBuilder topLeft = new StringBuilder();
            StringBuilder top = new StringBuilder();
            StringBuilder topRight = new StringBuilder();
            StringBuilder right = new StringBuilder();
            StringBuilder bottomRight = new StringBuilder();
            StringBuilder bottom = new StringBuilder();
            StringBuilder bottomLeft = new StringBuilder();
            StringBuilder left = new StringBuilder();
            for (int i = 0; i < radius; i++) {
                int row = this.row();
                int column = this.column();
                int rowTop = row - i;
                int rowBottom = row + i;
                int columnLeft = column - i;
                int columnRight = column + i;
                if (this.matrix.in(rowTop, columnLeft)) {
                    topLeft.append(matrix.at(rowTop, columnLeft).value());
                }
                if (this.matrix.in(rowTop, column)) {
                    top.append(matrix.at(rowTop, column).value());
                }
                if (this.matrix.in(rowTop, columnRight)) {
                    topRight.append(matrix.at(rowTop, columnRight).value());
                }
                if (this.matrix.in(row, columnRight)) {
                    right.append(matrix.at(row, columnRight).value());
                }
                if (this.matrix.in(rowBottom, columnRight)) {
                    bottomRight.append(matrix.at(rowBottom, columnRight).value());
                }
                if (this.matrix.in(rowBottom, column)) {
                    bottom.append(matrix.at(rowBottom, column).value());
                }
                if (this.matrix.in(rowBottom, columnLeft)) {
                    bottomLeft.append(matrix.at(rowBottom, columnLeft).value());
                }
                if (this.matrix.in(row, columnLeft)) {
                    left.append(matrix.at(row, columnLeft).value());
                }
            }
            return new Directions<>(
                    topLeft.toString(),
                    top.toString(),
                    topRight.toString(),
                    right.toString(),
                    bottomRight.toString(),
                    bottom.toString(),
                    bottomLeft.toString(),
                    left.toString()
            );
        }
    }

    private Item<T> at(int row, int col) {
        return this.rows.get(row).at(col);
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

    public boolean in(int row, int col) {
        return (row >= 0 && row < this.nRows) && (col >= 0 && col < this.nCols);
    }
}
