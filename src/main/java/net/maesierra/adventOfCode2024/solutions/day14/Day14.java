package net.maesierra.adventOfCode2024.solutions.day14;

import net.maesierra.adventOfCode2024.Runner;
import net.maesierra.adventOfCode2024.utils.Polygon.BoundingBox;
import net.maesierra.adventOfCode2024.utils.Position;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsStream;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsString;

public class Day14 implements Runner.Solution {

    static class Space {

        private final int width;
        private final int height;
        Map<Robot, Position> robots;

        Space(int width, int height) {
            this.width = width;
            this.height = height;
        }

        Position wrap(int x, int y) {
            y = y % height;
            if (y < 0) {
                y = height + y;
            }
            x = x % width;
            if (x < 0) {
                x = width + x;
            }
            return new Position(y, x);
        }

        List<Quadrant> quadrants() {
            int middleX = width / 2;
            int middleY = height / 2;
            return List.of(
                    new Quadrant(new BoundingBox(0, 0, middleY - 1, middleX - 1)),
                    new Quadrant(new BoundingBox(0, middleX + 1, middleY - 1, width - 1)),
                    new Quadrant(new BoundingBox(middleY + 1, 0, height - 1, middleX - 1)),
                    new Quadrant(new BoundingBox(middleY + 1, middleX + 1, height - 1, width - 1))
            );
        }

        void nextIteration() {
            robots = robots.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> {
                        Robot robot = e.getKey();
                        return robot.move(e.getValue());
                    }
            ));
        }
    };

    static class Quadrant {
        private final BoundingBox boundingBox;
        private int nRobots = 0;

        public Quadrant(BoundingBox boundingBox) {
            this.boundingBox = boundingBox;
        }

        public boolean contains(Position position) {
            return boundingBox.contains(position);
        }
    }

    record Robot(int id, int speedX, int speedY, Space space) {

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Robot robot = (Robot) o;
            return id == robot.id;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }

        public Position move(Position position) {
            return space.wrap(position.col() + speedX, position.row() + speedY);
        }
    }

    @Override
    public Consumer<Graphics2D> visualisePart1(InputStream input, String... params) {
        AtomicInteger i = new AtomicInteger(0);
        Space space = createSpace(input, params);
        for (int s = 0; s < 7037; s++) {
            space.nextIteration();
            i.getAndIncrement();
        }
        return (graphics -> {
            //space.nextIteration();
            for (var pos:space.robots.values()) {
                var position = pos.multiply(10);
                graphics.fillOval(position.col() - 3, position.row() - 3, 6, 6);
            }
            graphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Font font = new Font("Serif", Font.PLAIN, 20);
            graphics.setFont(font);
            //graphics.drawString("Second %d".formatted(i.getAndIncrement()), 10, 40);
            graphics.drawString("Second %d".formatted(i.get()), 10, 40);
        });
    }

    private static Space createSpace(InputStream input, String... params) {
        int width = 101;
        int height = 103;
        if (params.length == 2) {
            width = Integer.parseInt(params[0]);
            height = Integer.parseInt(params[1]);
        }
        Space space = new Space(width, height);
        AtomicInteger idGenerator = new AtomicInteger(0);
        space.robots = inputAsStream(input, Pattern.compile("p=(\\d+),(\\d+) v=(-?\\d+),(-?\\d+)")).map(groups -> {
            Position position = new Position(Integer.parseInt(groups[1]), Integer.parseInt(groups[0]));
            Robot robot = new Robot(idGenerator.getAndIncrement(), Integer.parseInt(groups[2]), Integer.parseInt(groups[3]), space);
            return Pair.of(robot, position);
        }).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        return space;
    }

    @Override
    public String part1(InputStream input, String... params) {
        Space space = createSpace(input, params);
        for (int second = 0; second < 100; second++) {
            space.nextIteration();
        }
        Map<Position, Integer> robotMap = space.robots.entrySet().stream().reduce(new HashMap<>(), (map, entry) -> {
            Position position = entry.getValue();
            map.put(
                    position,
                    map.getOrDefault(position, 0) + 1
            );
            return map;
        }, (a, b) -> a);

        List<Quadrant> quadrants = space.quadrants();
        robotMap.forEach((position, count) -> {
            for (var quadrant:quadrants) {
                if (quadrant.contains(position)) {
                    quadrant.nRobots += count;
                    break;
                }
            }
        });
        long res = 1L;
        for (var quadrant:quadrants) {
            if (quadrant.nRobots > 0) {
                res *= quadrant.nRobots;
            }
        }
        return Long.toString(res);
    }

    @Override
    public String part2(InputStream input, String... params) {
        return "";
    }

}
