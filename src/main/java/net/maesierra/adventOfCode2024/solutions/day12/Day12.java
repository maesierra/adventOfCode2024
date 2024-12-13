package net.maesierra.adventOfCode2024.solutions.day12;

import net.maesierra.adventOfCode2024.Main;
import net.maesierra.adventOfCode2024.utils.Directions;
import net.maesierra.adventOfCode2024.utils.Directions.Direction;
import net.maesierra.adventOfCode2024.utils.Matrix;
import net.maesierra.adventOfCode2024.utils.Matrix.Item;
import net.maesierra.adventOfCode2024.utils.Polygon;
import net.maesierra.adventOfCode2024.utils.Polygon.BoundingBox;
import net.maesierra.adventOfCode2024.utils.Position;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;
import static net.maesierra.adventOfCode2024.utils.Directions.Direction.EAST;
import static net.maesierra.adventOfCode2024.utils.Directions.Direction.NORTH;
import static net.maesierra.adventOfCode2024.utils.Directions.Direction.SOUTH;
import static net.maesierra.adventOfCode2024.utils.Directions.Direction.WEST;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsCharMatrix;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsString;

public class Day12 implements Main.Solution {

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
            return point.directNeighbours().map(p -> {
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
        private String crop;
        private List<Plot> plots;

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

        long perimiter() {
            return plots.stream()
                    .reduce(0L,
                            ((sum, plot) ->  sum + (4 - plot.neighbours(true).count())),
                            (a, b) -> a);
        }

        long cost() {
            return area() * perimiter();
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

        List<Polygon> polygons() {
            List<Direction> rotations = List.of(EAST, SOUTH, WEST, NORTH);
            List<Polygon> res = new ArrayList<>();
            //Start on the top left position
            int startingRow = plots.stream().mapToInt(p -> p.position().row()).min().orElse(0);
            Set<Position> toAdd = plots.stream().map(Plot::position).collect(Collectors.toSet());
            Plot start = plots.stream().filter(p -> p.position().row() == startingRow).min(comparing(p -> p.position().col())).orElseThrow();
            //first to the right
            Direction direction = EAST;
            while (!toAdd.isEmpty()) {
                Polygon polygon = new Polygon();
                polygon.add(new Position(0, 0));
                Position position = new Position(0, 1);
                polygon.add(position);
                Plot current = start;
                toAdd.remove(start.position());
                boolean closed = false;
                while (!closed) {
                    Directions<Plot> neighbours = current.neighboursDirections();
                    boolean canMove = false;
                    for (int i = 0; i < rotations.size(); i++) {
                        var rotation = rotations.get(i);
                        if (neighbours.get(rotation) != null) {
                            //Can move
                            int nTurns = Math.abs(i - rotations.indexOf(direction));
                            switch (nTurns) {
                                case 2, 3 -> {
                                    position = position.move(1, direction, true);
                                    polygon.add(position);
                                    position = position.move(1, direction.rotate90Right(), true);
                                    polygon.add(position);
                                }
                            }
                            direction = rotation;
                            canMove = true;
                            break;
                        }
                    }
                    if (!canMove) {
                        polygon.remove(polygon.size() - 1);
                        closed = true;
                        continue;
                    }
                    //Move
                    position = position.move(1, direction, true);
                    polygon.add(position);
                    current = neighbours.get(direction);

//                    if (current.position().equals(start.position())) {
//                        position = position.move(2, direction, true);
//                        if (!position.equals(polygon.get(0))) {
//                            polygon.add(position);
//                        }
//                        res.add(polygon);
//                        closed = true;
//                    }
                }
                toAdd.remove(start.position());
                if (!toAdd.isEmpty()) {
                    start = getAt(toAdd.iterator().next()).orElseThrow();
                }
            }
            return res;
        }

        void draw(Graphics2D graphics, Position at, int scale) {
            for (var polygon : polygons()) {
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
            regions.add(region);;
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

    @Override
    public String part1(InputStream input, String... params) {
        List<Region> regions = parseRegions(input);
        Long res = regions.stream()
                .reduce(0L, (sum, r) -> sum + r.cost(), (a, b) -> a);
        return Long.toString(res);
    }

    public void drawRegions(InputStream input, File output) {
        List<Region> regions = parseRegions(input);
        Map<Integer, Color> colours = Map.of(
                0, Color.BLUE,
                1, Color.CYAN,
                2, Color.GREEN,
                3, Color.MAGENTA,
                4, Color.RED,
                5, Color.YELLOW,
                6, Color.WHITE
        );
        int colour = 0;
        BufferedImage image = new BufferedImage(1000, 1000, TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        for (var r: regions) {
            if (!Set.of("C").contains(r.crop)) {
                continue;
            }
            graphics.setColor(colours.get(colour));
            r.draw(graphics, r.box().topLeft(), 50);
            colour = (colour + 1) % 6;
        }
        try {
            ImageIO.write(image, "png", output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String part2(InputStream input, String... params) {
        return inputAsString(input).toLowerCase();
    }
}
