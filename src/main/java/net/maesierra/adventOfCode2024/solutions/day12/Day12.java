package net.maesierra.adventOfCode2024.solutions.day12;

import net.maesierra.adventOfCode2024.Runner;
import net.maesierra.adventOfCode2024.utils.*;
import net.maesierra.adventOfCode2024.utils.Directions.Direction;
import net.maesierra.adventOfCode2024.utils.Matrix.Item;
import net.maesierra.adventOfCode2024.utils.Polygon;
import net.maesierra.adventOfCode2024.utils.Polygon.BoundingBox;

import java.awt.*;
import java.io.InputStream;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toCollection;
import static net.maesierra.adventOfCode2024.solutions.day12.Day12.CornerShape.NORTH_EAST_SOUTH;
import static net.maesierra.adventOfCode2024.solutions.day12.Day12.CornerShape.NORTH_EAST_WEST;
import static net.maesierra.adventOfCode2024.solutions.day12.Day12.CornerShape.WEST_EAST;
import static net.maesierra.adventOfCode2024.solutions.day12.Day12.CornerShape.WEST_EAST_SOUTH;
import static net.maesierra.adventOfCode2024.solutions.day12.Day12.CornerShape.WEST_NORTH;
import static net.maesierra.adventOfCode2024.solutions.day12.Day12.CornerShape.WEST_SOUTH;
import static net.maesierra.adventOfCode2024.solutions.day12.Day12.CornerShape.NORTH_SOUTH_WEST;
import static net.maesierra.adventOfCode2024.solutions.day12.Day12.CornerShape.EAST_SOUTH;
import static net.maesierra.adventOfCode2024.solutions.day12.Day12.CornerShape.NORTH_SOUTH;
import static net.maesierra.adventOfCode2024.utils.Directions.Direction.*;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsCharMatrix;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsString;

public class Day12 implements Runner.Solution {

    enum CornerShape {
        NORTH_EAST_SOUTH,
        NORTH_SOUTH_WEST,
        NORTH_EAST_WEST,
        WEST_EAST_SOUTH,
        WEST_NORTH,
        NORTH_EAST,
        NORTH_SOUTH,
        WEST_SOUTH,
        WEST_EAST,
        EAST_SOUTH,
        NORTH,
        WEST,
        EAST,
        SOUTH,
        ALL, NONE
    }
    private static final Map<Set<Direction>, CornerShape> neighboursMap = Map.ofEntries(
            // ...
            // .x.  =>  ┌─┐
            // .x.
            entry(Set.of(SOUTH), NORTH_EAST_WEST),
            // ...       ─┐
            // xx.  =>   ─┘
            // ...
            entry(Set.of(WEST), NORTH_EAST_SOUTH),
            // ...       ┌─
            // xx.  =>   └─
            // ...
            entry(Set.of(EAST), NORTH_SOUTH_WEST),
            // .x.
            // .x.  =>  └─┘
            // ...
            entry(Set.of(NORTH), WEST_EAST_SOUTH),
            // ...
            // .xx  =>  ┌─
            // .x.
            entry(Set.of(EAST, SOUTH), WEST_NORTH),
            // ...
            // xx.  =>  ─┐
            // .x.
            entry(Set.of(SOUTH, WEST), CornerShape.NORTH_EAST),
            // .x.
            // xx.  =>   ─┘
            // ...
            entry(Set.of(WEST, NORTH), EAST_SOUTH),
            // .x.
            // .xx  =>  └─
            // ...
            entry(Set.of(NORTH, EAST), WEST_SOUTH),
            // .x.
            // .x.  =>  │ │
            // .x.
            entry(Set.of(NORTH, SOUTH), WEST_EAST),
            // ...       ─
            // xxx  =>   ─
            // ...
            entry(Set.of(EAST, WEST), NORTH_SOUTH),
            // .x.
            // .xx  =>  │
            // .x.
            entry(Set.of(NORTH, SOUTH, EAST), CornerShape.WEST),
            // .x.
            // xx.  =>   │
            // .x.
            entry(Set.of(NORTH, SOUTH, WEST), CornerShape.EAST),
            // .x.
            // xxx  =>   ─
            // ...
            entry(Set.of(EAST, WEST, NORTH), CornerShape.SOUTH),
            // ...
            // xxx  =>   ─
            // .x.
            entry(Set.of(EAST, WEST, SOUTH), CornerShape.NORTH),
            // .X.
            // xxx  =>
            // .x.
            entry(Set.of(EAST, WEST, SOUTH, NORTH), CornerShape.NONE),
            // ...
            // .x.  => ┌─┐
            // ...     └─┘
            entry(Set.of(), CornerShape.ALL)
    );



    static class Plot implements Comparable<Plot> {
        private List<Plot> neighbours = null;
        private final Item<Character> point;

        public Plot(Item<Character> point) {
            this.point = point;
        }

        public Item<Character> point() {
            return point;
        }

        public String crop() {
            return point.value().toString();
        }

        public Directions<Plot> neighboursDirections() {
            return neighboursDirections(false);
        }

        public Directions<Plot> neighboursDirections(boolean orthogonal) {
            return (orthogonal ? point.orthogonalNeighbours() : point.directNeighbours()).map(p -> {
                if (p != null && p.value().toString().equals(crop())) {
                    return new Plot(p);
                } else {
                    return null;
                }
            });
        }

        public Stream<Plot> neighbours() {
            if (neighbours == null) {
                neighbours = neighboursDirections().stream().filter(Objects::nonNull).toList();
            }
            return neighbours.stream();
        }
        public Stream<Plot> neighbours(boolean orthogonal) {
            if (!orthogonal) {
                return neighbours();
            } else {
                return neighbours().filter(p -> p.position().row() == position().row() || p.position().col() == position().col());
            }

        }
        public CornerShape fenceShape() {
            return neighboursMap.get(neighboursDirections(true).asMap(true).keySet());
        }

        private Position position() {
            return point.position();
        }


        @Override
        public String toString() {
            return "%s[%d,%d]".formatted(point.value(), point.position().row(), point.position().col());
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Plot plot = (Plot) o;
            return Objects.equals(point, plot.point);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(point);
        }


        @Override
        public int compareTo(Plot p) {
            return this.point().position().compareTo(p.point.position());
        }
    }

    static class Region {
        private final String crop;
        private final List<Plot> plots;

        Region(String crop) {
            this.crop = crop;
            this.plots = new ArrayList<>();
        }

        void add(Plot plot) {
            this.plots.add(plot);
        }

        int area() {
            return plots.size();
        }

        long cost(boolean discounted) {
            long perimeter = polygons(discounted).stream().mapToInt(ArrayList::size).sum();
            return area() * perimeter;
        }

        boolean contains(Position position) {
            return getAt(position).isPresent();
        }

        boolean contains(int row, int col) {
            return getAt(new Position(row, col)).isPresent();
        }

        Optional<Plot> getAt(Position position) {
            return plots.stream().filter(p -> p.position().equals(position)).findFirst();
        }

        List<Polygon> polygons(boolean simplified) {
            Logger.debug("Generating polygons for crop %s", this.crop);
            List<Polygon> res = new ArrayList<>();
            //Start on the top left position
            int startingRow = plots.stream().mapToInt(p -> p.position().row()).min().orElse(0);
            //Track the number of points that are expected in each position
            class PointsTracker {
                final Map<Position, Integer> map = new HashMap<>();
                void trackPosition(Position position, CornerShape shape) {
                    if (map.containsKey(position)) {
                        return;
                    }
                    map.put(position, switch (shape) {
                        case NONE -> 0;
                        case NORTH,SOUTH,EAST,WEST -> 2;
                        case NORTH_EAST, WEST_NORTH, WEST_SOUTH, EAST_SOUTH -> 3;
                        default -> 4;
                    });
                }
                void pointsUsed(Position position, Integer n) {
                    if (!map.containsKey(position)) {
                        return;
                    }
                    map.put(position, map.get(position) - n);
                }
                boolean isNotCompleted() {
                    return map.values().stream().anyMatch(v -> v > 0);
                }
                Position next() {
                    Map<Position, Integer> available = map.entrySet().stream()
                            .filter(e -> e.getValue() > 0)
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    return map.entrySet().stream()
                            .filter(e -> e.getValue() > 0)
                            .map(Map.Entry::getKey)
                            .min(Comparator.naturalOrder())
                            .orElseThrow();
                }

            }
            var tracker = new PointsTracker();
            plots.forEach(p -> tracker.trackPosition(p.position(), p.fenceShape()));
            Plot start = plots.stream().filter(p -> p.position().row() == startingRow).min(comparing(p -> p.position().col())).orElseThrow();
            boolean outward = true;
            while (tracker.isNotCompleted()) {
                Polygon polygon = new Polygon();
                Direction direction = outward ? EAST : WEST;
                Plot current = start;
                boolean closed = false;
                while (!closed) {
                    Objects.requireNonNull(current);
                    Position topLeft = current.position().add(-start.position().row(), -start.position().col());
                    Position topRight = new Position(topLeft.row(), topLeft.col() + 1);
                    Position bottomRight = new Position(topLeft.row() + 1, topLeft.col() + 1);
                    Position bottomLeft = new Position(topLeft.row() + 1, topLeft.col());
                    CornerShape cornerShape = current.fenceShape();
                    switch (cornerShape) {
                        case NORTH_EAST_WEST -> {
                            polygon.add(bottomLeft);
                            polygon.add(topLeft);
                            polygon.add(topRight);
                            polygon.add(bottomRight);
                            tracker.pointsUsed(current.position(), 4);
                            direction = SOUTH;
                        }
                        case WEST_EAST_SOUTH -> {
                            polygon.add(topRight);
                            polygon.add(bottomRight);
                            polygon.add(bottomLeft);
                            polygon.add(topLeft);
                            tracker.pointsUsed(current.position(), 4);
                            direction = NORTH;
                        }
                        case NORTH_EAST_SOUTH -> {
                            polygon.add(topLeft);
                            polygon.add(topRight);
                            polygon.add(bottomRight);
                            polygon.add(bottomLeft);
                            tracker.pointsUsed(current.position(), 4);
                            direction = WEST;
                        }
                        case NORTH_SOUTH_WEST -> {
                            polygon.add(bottomRight);
                            polygon.add(bottomLeft);
                            polygon.add(topLeft);
                            polygon.add(topRight);
                            tracker.pointsUsed(current.position(), 4);
                            direction = EAST;
                        }
                        case NORTH_SOUTH -> {
                            if (direction.equals(EAST)) {
                                polygon.add(topLeft);
                                polygon.add(topRight);
                                tracker.pointsUsed(current.position(), 2);
                            } else  {
                                polygon.add(bottomRight);
                                polygon.add(bottomLeft);
                                tracker.pointsUsed(current.position(), 2);
                            }
                        }
                        case WEST_EAST -> {
                            if (direction.equals(SOUTH)) {
                                polygon.add(topRight);
                                polygon.add(bottomRight);
                                tracker.pointsUsed(current.position(), 2);
                            } else  {
                                polygon.add(bottomLeft);
                                polygon.add(topLeft);
                                tracker.pointsUsed(current.position(), 2);
                            }
                        }
                        case WEST_NORTH -> {
                            switch (direction) {
                                //EAST is a special case as this could be a starting title
                                case NORTH, EAST -> {
                                    direction = EAST;
                                    polygon.add(bottomLeft);
                                    polygon.add(topLeft);
                                    polygon.add(topRight);
                                    tracker.pointsUsed(current.position(), 3);
                                }
                                case WEST -> {
                                    direction = SOUTH;
                                }
                            }
                        }
                        case WEST_SOUTH -> {
                            if (direction.equals(SOUTH)) {
                                direction = EAST;
                            } else if (direction.equals(WEST)) {
                                direction = NORTH;
                                polygon.add(bottomRight);
                                polygon.add(bottomLeft);
                                polygon.add(topLeft);
                                tracker.pointsUsed(current.position(), 3);
                            }
                        }
                        case NORTH_EAST -> {
                            if (direction.equals(NORTH)) {
                                direction = WEST;
                            } else if (direction.equals(EAST)) {
                                polygon.add(topLeft);
                                polygon.add(topRight);
                                polygon.add(bottomRight);
                                tracker.pointsUsed(current.position(), 3);
                                direction = SOUTH;
                            }
                        }
                        case EAST_SOUTH -> {
                            if (direction.equals(SOUTH)) {
                                polygon.add(topRight);
                                polygon.add(bottomRight);
                                polygon.add(bottomLeft);
                                tracker.pointsUsed(current.position(), 3);
                                direction = WEST;
                            } else if (direction.equals(EAST)) {
                                direction = NORTH;
                            }
                        }
                        case NORTH -> {
                            if (direction.equals(NORTH)) {
                                direction = WEST;
                            } else if (direction.equals(WEST)) {
                                direction = SOUTH;
                            } else if (direction.equals(EAST)) {
                                polygon.add(topLeft);
                                polygon.add(topRight);
                                tracker.pointsUsed(current.position(), 2);
                            }
                        }
                        case SOUTH -> {
                            if (direction.equals(EAST)) {
                                direction = NORTH;
                            } else if (direction.equals(SOUTH)) {
                                direction = EAST;
                            } else if (direction.equals(WEST)) {
                                polygon.add(bottomRight);
                                polygon.add(bottomLeft);
                                tracker.pointsUsed(current.position(), 2);
                            }
                        }
                        case EAST -> {
                            if (direction.equals(EAST)) {
                                direction = NORTH;
                            } else if (direction.equals(NORTH)) {
                                direction = WEST;
                            } else if (direction.equals(SOUTH)) {
                                polygon.add(topRight);
                                polygon.add(bottomRight);
                                tracker.pointsUsed(current.position(), 2);
                            }
                        }
                        case WEST -> {
                            if (direction.equals(SOUTH)) {
                                direction = EAST;
                            } else if (direction.equals(WEST)) {
                                direction = SOUTH;
                            } else if (direction.equals(NORTH)) {
                                polygon.add(bottomLeft);
                                polygon.add(topLeft);
                                tracker.pointsUsed(current.position(), 2);
                            }
                        }
                        case ALL -> {
                            polygon.add(topLeft);
                            polygon.add(topRight);
                            polygon.add(bottomRight);
                            polygon.add(bottomLeft);
                            tracker.pointsUsed(current.position(), 4);
                        }
                        case NONE -> {
                            switch (direction) {
                                case SOUTH -> direction = EAST;
                                case EAST -> direction = NORTH;
                                case NORTH -> direction = WEST;
                                case WEST -> direction = SOUTH;
                            }
                        }

                    }
                    current = current.neighboursDirections().get(direction);
                    if (current == null || current.equals(start)) {
                        //some edge cases
                        if (start.fenceShape().equals(WEST_NORTH) && direction.equals(WEST)) {
                            closed = false;
                        } else {
                            closed = true;
                        }
                    }
                }
                //Adjust the polygon to start in 0,0
                List<Position> endTail = new ArrayList<>();
                if (outward) {
                    while (!polygon.get(0).equals(Position.ZERO_ZERO)) {
                        endTail.add(polygon.get(0));
                        polygon.remove(0);
                    }
                    for (var p : endTail) {
                        polygon.add(p);
                    }
                }
                if (polygon.isEmpty()) {
                    throw new RuntimeException("Invalid polygon");
                }
                if (polygon.last().equals(polygon.first())) {
                    polygon.remove(polygon.last());
                }
                //Validate the polygon
                if (!validatePolygon(polygon)) {
                    throw new RuntimeException("Invalid polygon");
                }
                if (tracker.isNotCompleted()) {
                    //Inner cuts
                    start = getAt(tracker.next()).orElseThrow();
                    outward  = false;
                }

                res.add(polygon);

            }
            Logger.debug("%d Polygons generated for region %s", res.size(), this.crop);
            if (simplified) {
                res = res.stream().map(polygon -> {
                    Polygon newPolygon = new Polygon();
                    Polygon side = new Polygon();
                    side.add(polygon.first());
                    newPolygon.add(side.first());
                    Direction currentDirection = EAST;
                    for (int i = 1; i <= polygon.size(); i++) {
                        Position position = i == polygon.size() ? polygon.first() : polygon.get(i);
                        Direction direction = fromPosition(side.last(), position);
                        if (direction.equals(currentDirection)) {
                            side.add(position);
                        } else {
                            Position last = side.last();
                            newPolygon.add(last);
                            side = new Polygon();
                            side.add(last);
                            side.add(position);
                            currentDirection = direction;
                        }
                    }
                    return newPolygon;
                }).toList();
            }
            return res;
        }


        void draw(Graphics2D graphics, Position at, int scale) {
            Color colour = graphics.getColor();
            for (var plot: plots) {
                Position position = plot.position().multiply(scale);
                graphics.fillRect(position.col(), position.row(), scale, scale);
            }
            graphics.setColor(Color.WHITE);
            List<Polygon> polygons = polygons(false);
            for (var polygon : polygons) {
                for (int i = 0; i < polygon.size(); i++) {
                    Position position = polygon.get(i)
                            .add(at.row(), at.col())
                            .multiply(scale);
                    Position next = ((i == polygon.size() - 1) ?
                        polygon.get(0) :
                        polygon.get(i + 1))
                            .add(at.row(), at.col())
                            .multiply(scale);
                    graphics.fillOval(position.col() - 3, position.row() - 3, 6, 6);
                    graphics.drawLine(position.col(), position.row(), next.col(), next.row());
                }
            }
            graphics.setColor(colour);
        }

        public BoundingBox box() {
            int minRow = plots.stream().mapToInt(p -> p.position().row()).min().orElse(0);
            int minCol = plots.stream().mapToInt(p -> p.position().col()).min().orElse(0);
            int maxRow = plots.stream().mapToInt(p -> p.position().row()).max().orElse(0);
            int maxCol = plots.stream().mapToInt(p -> p.position().col()).max().orElse(0);
            return new BoundingBox(minRow, minCol, maxRow, maxCol);
        }
    }

    private static List<Region> parseRegions(InputStream input) {
        Matrix<Plot> map = inputAsCharMatrix(input).map(Plot::new);
        Set<Plot> toProcess = map.items().map(Item::value).collect(toCollection(TreeSet::new));
        List<Region> regions = new ArrayList<>();
        while (!toProcess.isEmpty()) {
            Plot plot = toProcess.iterator().next();
            toProcess.remove(plot);
            Region region = new Region(plot.crop());
            region.add(plot);
            regions.add(region);
            Deque<Plot> neighbours = plot.neighbours(true)
                    .filter(toProcess::contains)
                    .collect(toCollection(ArrayDeque::new));
            while (!neighbours.isEmpty()) {
                Plot currentPlot = neighbours.pop();
                if (!toProcess.contains(currentPlot)) {
                    continue;
                }
                region.add(currentPlot);
                toProcess.remove(currentPlot);
                currentPlot.neighbours(true)
                        .filter(toProcess::contains)
                        .forEach(neighbours::addLast);

            }
        }
        return regions;
    }

    private static boolean validatePolygon(Polygon polygon) {
        for (int i = 0; i < polygon.size(); i++) {
            Position p1 = polygon.get(i);
            Position p2 = i == polygon.size() - 1 ? polygon.get(0) : polygon.get(i + 1);
            boolean res =  switch (Direction.fromPosition(p1, p2)) {
                case EAST -> p1.row() == p2.row() && p1.col() == p2.col() - 1;
                case WEST -> p1.row() == p2.row() && p1.col() == p2.col() + 1;
                case NORTH -> p1.col() == p2.col() && p1.row() == p2.row() + 1;
                case SOUTH -> p1.col() == p2.col() && p1.row() == p2.row() - 1;
                default -> throw new RuntimeException("Invalid polygon");
            };
            if (!res) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String part1(InputStream input, String... params) {
        List<Region> regions = parseRegions(input);
        Long res = regions.stream()
                .reduce(0L, (sum, r) -> sum + r.cost(false), (a, b) -> a);
        return Long.toString(res);
    }

    @Override
    public String part2(InputStream input, String... params) {
        List<Region> regions = parseRegions(input);
        Long res = regions.stream()
                .reduce(0L, (sum, r) -> sum + r.cost(true), (a, b) -> a);
        return Long.toString(res);
    }

    @Override
    public Consumer<Graphics2D> visualisePart1(InputStream input, String... params) {
        List<Region> regions = parseRegions(input);
        AtomicInteger colour = new AtomicInteger();
        Map<Region, Color> regionColour = regions.stream().collect(Collectors.toMap(
                r -> r,
                r -> switch (colour.getAndIncrement() % 6) {
                    case 0 ->Color.BLUE;
                    case 1 ->Color.CYAN;
                    case 2 ->Color.GREEN;
                    case 3 ->Color.MAGENTA;
                    case 4 ->Color.RED;
                    case 5 ->Color.YELLOW;
                    default -> Color.LIGHT_GRAY;
                }));

        return (graphics) -> {
            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, 1000, 1000);
            for (var r: regions) {
//                if (!Set.of("D").contains(r.crop)) {
//                    continue;
//                }
                graphics.setColor(regionColour.get(r));
                r.draw(graphics, r.box().topLeft(), 50);
            }
        };
    }

}
