package net.maesierra.adventOfCode2024.solutions.day6;

import net.maesierra.adventOfCode2024.Runner;
import net.maesierra.adventOfCode2024.utils.Directions;
import net.maesierra.adventOfCode2024.utils.Directions.Direction;
import net.maesierra.adventOfCode2024.utils.Logger;
import net.maesierra.adventOfCode2024.utils.Matrix;
import net.maesierra.adventOfCode2024.utils.Matrix.Item;
import net.maesierra.adventOfCode2024.utils.Position;
import org.apache.commons.lang3.function.TriConsumer;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static net.maesierra.adventOfCode2024.utils.Directions.Direction.NORTH;
import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsCharMatrix;

public class Day6 implements Runner.Solution {

    static class GridPosition {
        private boolean hasObstacle;
        private final boolean isInitial;
        private Directions<Item<GridPosition>> connections;

        public GridPosition(boolean hasObstacle, boolean isInitial) {
            this.hasObstacle = hasObstacle;
            this.isInitial = isInitial;
        }

        @Override
        public String toString() {
            if (hasObstacle) {
                return "#";
            } else if (isInitial) {
                return "^";
            } else {
                return ".";
            }
        }
    }

    static class Grid extends Matrix<GridPosition> {

        public Grid(Matrix<GridPosition> other) {
            super(other);
        }

        Position initialPosition() {
            return this.items().filter(i -> i.value().isInitial).findFirst().orElseThrow().position();
        }

        Direction initialDirection() {
            return NORTH;
        }

        Optional<Step> next(Position position, Direction direction) {
            GridPosition current = this.at(position).value();
            Item<GridPosition> next = current.connections.get(direction);
            while (next != null && next.value().hasObstacle) {
                direction = direction.rotate90Right();
                next = current.connections.get(direction);
            }
            if (next == null) {
                return Optional.empty();
            }
            return Optional.of(new Step(next.position(), direction));
        }

        Path move(Position position, Direction direction) {
            return move(position, direction, ((k, v, s) -> {}));
        }

        Path move(Position position, Direction direction, TriConsumer<Step, Path, Grid> consumer) {
            Path path = new Path(position, direction);
            Optional<Step> next = next(position, direction);
            while (next.isPresent()) {
                position = next.get().position();
                direction = next.get().direction();
                if (path.contains(next.get())) {
                    path.add(position, direction);
                    path.hasLoop = true;
                    return path;
                }
                path.add(position, direction);
                consumer.accept(next.get(), path, this);
                next = next(position, direction);
            }
            return path;
        }


        String toString(Position position, Direction direction, Path path) {
            return this.toString(i -> {
                if (i.value().isInitial) {
                    return "i";
                }
                if (i.position().equals(position)) {
                    return switch (direction) {
                        case NORTH -> "^";
                        case EAST -> ">";
                        case WEST -> "<";
                        case SOUTH -> "v";
                        default -> "";
                    };
                } else if (path.contains(i.position())) {
                    return "X";
                } else {
                    return i.value().toString();
                }
            });
        }

        String printPath(Path path, Optional<Position> obstacle) {
            return this.toString(i -> {
                if (obstacle.filter(p -> i.position().equals(p)).isPresent()) {
                    return "O";
                }
                if (i.value().isInitial) {
                    return "i";
                } else if (path.contains(i.position())) {
                    return "X";
                } else {
                    return i.value().toString();
                }
            });
        }

    }


    private static Grid parseGrid(InputStream input) {
        Matrix<GridPosition> grid = inputAsCharMatrix(input).map(item -> {
            return switch (item.value()) {
                case '^' -> new GridPosition(false, true);
                case '#' -> new GridPosition(true, false);
                default -> new GridPosition(false, false);
            };
        }).map( item -> {
            var neighbours = item.neighbours(2).map(l -> {
                l.remove(0);
                return l;
            });
            item.value().connections = new Directions<>(
                    null,
                    neighbours.north().stream().findFirst().orElse(null),
                    null,
                    neighbours.east().stream().findFirst().orElse(null),
                    null,
                    neighbours.south().stream().findFirst().orElse(null),
                    null,
                    neighbours.west().stream().findFirst().orElse(null)
            );
            return item.value();
        });
        Logger.debug("Initial grid: \n%s\n\n", grid);
        return new Grid(grid);
    }



    record Step(Position position, Direction direction) {

    }

    private static class Path {
        private final Set<Step> visited;
        private final Set<Position> positionsVisited;
        private final List<Step> steps;
        private boolean hasLoop = false;

        Path(Position position, Direction direction) {
            this();
            add(position, direction);
        }
        Path() {
            this.visited = new HashSet<>();
            this.steps = new ArrayList<>();
            this.positionsVisited = new HashSet<>();
        }

        void add(Position position, Direction direction) {
            Step step = new Step(position, direction);
            visited.add(step);
            positionsVisited.add(position);
            steps.add(step);
        }

        boolean contains(Step step) {
            return visited.contains(step);
        }

        boolean contains(Position position) {
            return positionsVisited.contains(position);
        }

        @Override
        public String toString() {
            return steps.stream().map(s -> "(%s => %s)".formatted(s.position, s.direction)).collect(Collectors.joining(" | "));
        }
    }

    @Override
    public String part1(InputStream input, String... params) {
        Grid grid = parseGrid(input);
        Path path = grid.move(
                grid.initialPosition(),
                grid.initialDirection(),
                (s, p, g) -> Logger.debug("%s\n\n", g.toString(s.position, s.direction, p))
        );
        long res = path.positionsVisited.size();
        return Long.toString(res);
    }

    @Override
    public String part2(InputStream input, String... params) {

        Grid grid = parseGrid(input);
        final AtomicInteger nLoops = new AtomicInteger(0);
        Path path = grid.move(grid.initialPosition(), grid.initialDirection());
        Set<Position> used = new HashSet<>();
        for (int i = 0; i < path.steps.size(); i++) {
            if (i == 0) {
                continue;
            }
            Step obstacle = path.steps.get(i);
            if (used.contains(obstacle.position())) {
                continue;
            }
            Step prev = path.steps.get(i - 1);
            grid.at(obstacle.position).value().hasObstacle = true;
            Path pathIfObstacle = grid.move(prev.position, prev.direction);
            if (pathIfObstacle.hasLoop) {
                Logger.debug("Loop found!!\n\n%s\n\n", grid.printPath(pathIfObstacle, Optional.of(obstacle.position())));
                nLoops.incrementAndGet();
            }
            grid.at(obstacle.position).value().hasObstacle = false;
            used.add(obstacle.position);

        }
        return Integer.toString(nLoops.get());
    }
}
