package net.maesierra.adventOfCode2024.solutions.day15;

import net.maesierra.adventOfCode2024.Runner;
import net.maesierra.adventOfCode2024.utils.Directions;
import net.maesierra.adventOfCode2024.utils.Directions.Direction;
import net.maesierra.adventOfCode2024.utils.Logger;
import net.maesierra.adventOfCode2024.utils.Matrix;
import net.maesierra.adventOfCode2024.utils.Matrix.Item;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static net.maesierra.adventOfCode2024.utils.Directions.Direction.*;
import static net.maesierra.adventOfCode2024.utils.IOHelper.*;

public class Day15 implements Runner.Solution {

    interface TileContent { }
    record Obstacle() implements TileContent {
        @Override
        public String toString() {
            return "#";
        }
    }
    record Robot() implements TileContent {
        @Override
        public String toString() {
            return "@";
        }
    }
    record Box() implements TileContent {
        @Override
        public String toString() {
            return "O";
        }
    }

    static class BigBoxLeft implements TileContent {
        private BigBoxRight right = null;
        @Override
        public String toString() {
            return "[";
        }
    }

    static class BigBoxRight implements TileContent {
        private BigBoxLeft leftt = null;
        @Override
        public String toString() {
            return "]";
        }
    }

    static class Tile {
        private TileContent content;

        Tile(TileContent content) {
            this.content = content;
        }

        boolean containsRobot() {
            return this.content instanceof Robot;
        }

        boolean containsObstacle() {
            return this.content instanceof Obstacle;
        }

        boolean containsBox() {
            return this.content instanceof Box;
        }

        public boolean containsBigBox() {
            return containsBigBoxLeft() || content instanceof BigBoxRight;
        }
        public boolean containsBigBoxLeft() {
            return content instanceof BigBoxLeft;
        }

        boolean isEmpty() {
            return content == null;
        }

        @Override
        public String toString() {
            return content == null ? "." : content.toString();
        }
    }

    static class Warehouse {
        private final Matrix<Tile> tiles;
        Item<Tile> robot;

        public Warehouse(Matrix<Tile> tiles) {
            this.tiles = tiles;
            this.robot = tiles.items()
                                .filter(i -> i.value().containsRobot())
                                .findFirst()
                                .orElseThrow();
        }

        private void moveTileContent(Item<Tile> src, Item<Tile> dest) {
            dest.value().content = src.value().content;
            src.value().content = null;
            if (src == robot) {
                robot = dest;
            }
        }

        public void moveRobot(char movement) {
            Direction direction = switch (movement) {
                case '^' -> NORTH;
                case '>' -> EAST;
                case 'v' -> SOUTH;
                case '<' -> WEST;
                default -> SOUTH_WEST;
            };
            //Lame hack to protect against invalid movements
            if (direction == SOUTH_WEST) {
                return;
            }
            Item<Tile> nextTile = robot.orthogonalNeighbours().get(direction);
            if (nextTile.value().containsObstacle()) {
                return;
            } else if (nextTile.value().isEmpty()) {
                moveTileContent(robot, nextTile);
                return;
            } else if (nextTile.value().containsBox()) {
                pushSmallBox(nextTile, direction);
                moveTileContent(robot, nextTile);
            } else if (nextTile.value().containsBigBox()) {
                switch (direction) {
                    case EAST, WEST -> pushBigBoxHorizontally(nextTile, direction);
                    case NORTH, SOUTH -> pushBigBoxVertically(nextTile, direction);
                }
                moveTileContent(robot, nextTile);
            }
        }

        private void pushBigBoxVertically(Item<Tile> boxTile1, Direction direction) {
            record Pair(Item<Tile> left, Item<Tile> right) {
                Pair(Item<Tile> tile) {
                    this(
                       tile.value().containsBigBoxLeft() ? tile : tile.orthogonalNeighbours().get(WEST),
                       tile.value().containsBigBoxLeft() ? tile.orthogonalNeighbours().get(EAST) : tile
                    );
                }
            }
            Deque<Pair> queue = new ArrayDeque<>();
            queue.add(new Pair(boxTile1));
            record Movement(Pair from, Pair to) {

            }
            //TODO:
            // Use the link property do determine if the 2 tiles over/bellow a bigbox contain the same box or 2 different boxes
            // if same box: add to queue (kill pair, we only need left item)
            // if different: add both to the queue
            // if wall found / null found -> stop movement
            // if queue emtpy -> allow movement
            // keep movements list to execute them
            List<Movement> movements = new ArrayList<>();
            while (!queue.isEmpty()) {
                Pair current = queue.pop();
                Item<Tile> nextLeft = current.left.orthogonalNeighbours().get(direction);
                Item<Tile> nextRight = current.right.orthogonalNeighbours().get(direction);
                //The box can only move if both parts can be moved
                if (nextRight != null && nextRight.value().containsBigBox() && nextLeft != null && nextLeft.value().containsBigBox()) {
                    movements.add(new Movement(current, new Pair(nextLeft, nextRight)));
                }
            }



        }

        private void pushSmallBox(Item<Tile> nextTile, Direction direction) {
            //Time to push boxes, we get the line of boxes in the direction
            Item<Tile> current = nextTile;
            while (current != null && current.value().containsBox()) {
                current = current.orthogonalNeighbours().get(direction);
            }
            //Check if the line can be moved
            if (current != null && current.value().isEmpty()) {
                //because all the boxes are the same we could just swap the first with the empty space
                moveTileContent(nextTile, current);
            }
        }

        private void pushBigBoxHorizontally(Item<Tile> nextTile, Direction direction) {
            List<Item<Tile>> boxes = new ArrayList<>();
            Item<Tile> current = nextTile;
            while (current != null && current.value().containsBigBox()) {
                boxes.add(current);
                current = current.orthogonalNeighbours().get(direction);
            }
            //Check if the line can be moved
            if (current != null && current.value().isEmpty()) {
                //No swapping here because of the boxes take 2 spaces and the whole line must be moved
                for (int i = boxes.size() - 1; i >= 0 ; i--) {
                    var box = boxes.get(i);
                    moveTileContent(box, current);
                    current = box;
                }
            }
        }
    }

    private static Warehouse parseInput(String block) {
        Matrix<Tile> matrix = inputAsCharMatrix(new ByteArrayInputStream(block.getBytes())).map(i -> {
            return new Tile(switch (i.value().toString()) {
                case "#" -> new Obstacle();
                case "@" -> new Robot();
                case "O" -> new Box();
                case "[" -> new BigBoxLeft();
                case "]" -> new BigBoxRight();
                default -> null;
            });
        });
        matrix.items().filter(i -> i.value().containsBigBoxLeft())
                .forEach(i -> {
                    Tile tile = i.value();
                    BigBoxLeft leftPart = (BigBoxLeft) tile.content;
                    BigBoxRight rightPart = (BigBoxRight) i.orthogonalNeighbours().get(EAST).value().content;
                    leftPart.right = rightPart;
                    rightPart.leftt = leftPart;
                });
        return new Warehouse(matrix);
    }

    @Override
    public String part1(InputStream input, String... params) {
        String[] blocks = inputAsString(input).split("\\n\\n");
        String movements = blocks[1];
        Warehouse warehouse = parseInput(blocks[0]);
        movements.chars().forEachOrdered(c -> {
            warehouse.moveRobot((char)c);
            Logger.debug("%s", warehouse.tiles.toString());
        });

        int res = warehouse.tiles.items()
                    .filter(i -> i.value().containsBox())
                    .mapToInt(i -> i.position().row() * 100 + i.position().col())
                    .sum();
        return Integer.toString(res);
    }

    @Override
    public String part2(InputStream input, String... params) {
        String[] blocks = inputAsString(input).split("\\n\\n");
        String movements = blocks[1];
        String map = blocks[0]
                .replace("#", "##")
                .replace("O", "[]")
                .replace(".", "..")
                .replace("@",  "@.");

        Warehouse warehouse = parseInput(map);
        System.out.println(warehouse.tiles);
        movements.chars().forEachOrdered(c -> {
            warehouse.moveRobot((char)c);
            Logger.debug("%s", warehouse.tiles.toString());
        });

        int res = warehouse.tiles.items()
                .filter(i -> i.value().containsBox())
                .mapToInt(i -> i.position().row() * 100 + i.position().col())
                .sum();
        return Integer.toString(res);

    }
}
