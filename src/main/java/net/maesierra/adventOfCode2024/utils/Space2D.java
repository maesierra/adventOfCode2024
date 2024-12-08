package net.maesierra.adventOfCode2024.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class Space2D<T> {


    private final  Map<Point, T> space;
    private final BigDecimal maxX;
    private final BigDecimal maxY;

    public <T2> Space2D(Matrix<T2> matrix, BiFunction<Point, T2, T> contentMapper) {
        space = matrix.items().reduce(
                new HashMap<>(),
                (map, i) -> {
                    Point point = new Point(i.position().col(), i.position().row());
                    T value = contentMapper.apply(point, i.value());
                    if (value != null) {
                        map.put(point, value);
                    }
                    return map;
                },
                (a, b) -> a
        );
        this.maxX = new BigDecimal(matrix.nCols());
        this.maxY = new BigDecimal(matrix.nRows());
    }

    public Stream<T> items() {
        return space.values().stream();
    }

    public boolean contains(Point position) {
        // x > 0
        if (position.x.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        // x < maxX
        if (position.x.compareTo(maxX) >= 0) {
            return false;
        }
        // y > 0
        if (position.y.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        // y < maxY
        if (position.y.compareTo(maxY) >= 0) {
            return false;
        }
        return true;
    }

    public record Point(BigDecimal x, BigDecimal y) implements Comparable<Point>{

        public Point(int x, int y) {
            this(new BigDecimal(x), new BigDecimal(y));
        }

        public BigDecimal distanceX(Point other) {
            return this.x.subtract(other.x);
        }

        public BigDecimal distanceY(Point other) {
            return this.y.subtract(other.y);
        }

        public Distance distance(Point other) {
            return new Distance(distanceX(other), distanceY(other));
        }

        public Point round(int scale) {
            return new Point(x.setScale(scale, MathContext.DECIMAL32.getRoundingMode()),
                    y.setScale(scale, MathContext.DECIMAL32.getRoundingMode())
            );
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return Objects.equals(x, point.x) && Objects.equals(y, point.y);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public int compareTo(Point o) {
            int compareX = this.x.compareTo(o.x);
            int compareY = this.y.compareTo(o.y);
            if (compareX == 0) {
                return compareY;
            } else {
                return compareX;
            }
        }
    }


    public record Line(BigDecimal slope, BigDecimal intercept) {


        public Point get(BigDecimal x) {
            // y = x * slope + intercept
            BigDecimal y = x.multiply(slope).add(intercept);
            return new Point(x, y);
        }

        public static Line interpolate(Point p1, Point p2) {
            // (y2-y1) / (x2-x1)
            BigDecimal slope = p2.distanceY(p1).divide(p2.distanceX(p1), MathContext.DECIMAL32);
            //  y1 - a × x1
            BigDecimal intercept = p1.y.subtract(slope.multiply(p1.x));
            return new Space2D.Line(slope, intercept);
        }
    }

    public record Distance(BigDecimal x, BigDecimal y) {

        public BigDecimal total() {
            // d = sqrt(dx*dx + dy*dy)
            return x.pow(2).add(y.pow(2)).sqrt(MathContext.DECIMAL32);
        }
    }

}
