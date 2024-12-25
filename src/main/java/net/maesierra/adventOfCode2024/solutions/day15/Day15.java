package net.maesierra.adventOfCode2024.solutions.day15;

import net.maesierra.adventOfCode2024.Runner;
import net.maesierra.adventOfCode2024.utils.Directions.Direction;
import net.maesierra.adventOfCode2024.utils.Logger;
import net.maesierra.adventOfCode2024.utils.Matrix;
import net.maesierra.adventOfCode2024.utils.Matrix.Item;
import net.maesierra.adventOfCode2024.utils.Position;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.maesierra.adventOfCode2024.solutions.day15.Day15.HalfPart.LEFT;
import static net.maesierra.adventOfCode2024.solutions.day15.Day15.HalfPart.RIGHT;
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

    enum HalfPart {
        LEFT,
        RIGHT;
    }
    static class BigBoxHalf implements TileContent {
        HalfPart part;
        int id;

        public BigBoxHalf(int id, HalfPart part) {
            this.part = part;
            this.id = id;
        }
        
        boolean sameBox(BigBoxHalf other) {
            return this.id == other.id;
        }

        @Override
        public String toString() {
            return part == LEFT ? "[" : "]";
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
            return content instanceof BigBoxHalf;
        }

        boolean isEmpty() {
            return content == null;
        }

        @Override
        public String toString() {
            return content == null ? "." : content.toString();
        }
        @SuppressWarnings("unchecked")
        <T extends TileContent> T content(Class<T> clazz) {
            return (T) content;
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
            }
            if (nextTile.value().isEmpty()) {
                moveTileContent(robot, nextTile);
            } else if (nextTile.value().containsBox()) {
                if (pushSmallBox(nextTile, direction)) {
                    moveTileContent(robot, nextTile);
                }
            } else if (nextTile.value().containsBigBox()) {
                boolean moved = switch (direction) {
                    case EAST, WEST -> pushBigBoxHorizontally(nextTile, direction);
                    case NORTH, SOUTH -> pushBigBoxVertically(nextTile, direction);
                    default -> throw new RuntimeException("Unexpected value: " + direction);
                };
                if (moved) {
                    moveTileContent(robot, nextTile);
                }
            }
        }

        private boolean pushBigBoxVertically(Item<Tile> boxTile1, Direction direction) {
            record Pair(Item<Tile> left, Item<Tile> right) {

            }
            Deque<Pair> queue = new ArrayDeque<>();
            //Check if tile is the right part or the left part of the bigbox
            if (boxTile1.value().content(BigBoxHalf.class).part == LEFT) {
                queue.add(new Pair(boxTile1, boxTile1.orthogonalNeighbours().east()));
            } else {
                queue.add(new Pair(boxTile1.orthogonalNeighbours().west(), boxTile1));
            }
            record Movement(Pair from, Pair to) {

                @Override
                public String toString() {
                    return "[(%s),(%s)] => [(%s),(%s)]".formatted(from.left.position(), from.right.position(), to.left.position(), to.right.position());
                }
            }
            Set<Pair> processed = new HashSet<>();
            List<Movement> movements = new ArrayList<>();
            while (!queue.isEmpty()) {
                Pair current = queue.pop();
                if (processed.contains(current)) {
                    continue;
                }
                processed.add(current);
                var nextLeft = current.left.orthogonalNeighbours().get(direction);
                var nextRight =current.right.orthogonalNeighbours().get(direction);
                //The box can only move if both parts can be moved
                boolean leftCanMove = nextLeft != null && !nextLeft.value().containsObstacle();
                boolean rightCanMove = nextRight != null && !nextRight.value().containsObstacle();
                if (!leftCanMove || !rightCanMove) {
                    return false;
                }
                //Check if both spaces are empty
                Pair nextPair = new Pair(nextLeft, nextRight);
                if (nextRight.value().isEmpty() && nextLeft.value().isEmpty()) {
                    movements.add(new Movement(current, nextPair));
                } else if (nextRight.value().containsBigBox() && nextLeft.value().containsBigBox()) {
                    //Both parts touch bigboxes
                    if (nextRight.value().content(BigBoxHalf.class).sameBox(nextLeft.value().content(BigBoxHalf.class))) {
                        //Only one box
                        movements.add(new Movement(current, nextPair));
                        queue.addLast(nextPair);
                    } else {
                        //Two boxes
                        movements.add(new Movement(current, nextPair));
                        queue.addLast(new Pair(nextLeft.orthogonalNeighbours().west(), nextLeft));
                        queue.addLast(new Pair(nextRight, nextRight.orthogonalNeighbours().east()));
                    }
                } else if (nextRight.value().containsBigBox() && nextLeft.value().isEmpty()) {
                    //box at right, empty at left
                    movements.add(new Movement(current, nextPair));
                    queue.addLast(new Pair(nextRight, nextRight.orthogonalNeighbours().east()));
                } else if (nextLeft.value().containsBigBox() && nextRight.value().isEmpty()) {
                    //box at left, empty at right
                    movements.add(new Movement(current, nextPair));
                    queue.addLast(new Pair(nextLeft.orthogonalNeighbours().west(), nextLeft));
                } else {
                    throw new RuntimeException("Invalid state");
                }
            }
            //If the queue was emptied -> there is space to execute the movement
            Collections.reverse(movements);
            for (var movement:movements) {
                moveTileContent(movement.from.left, movement.to.left);
                moveTileContent(movement.from.right, movement.to.right);
            }
            return true;
        }

        private boolean pushSmallBox(Item<Tile> nextTile, Direction direction) {
            //Time to push boxes, we get the line of boxes in the direction
            Item<Tile> current = nextTile;
            while (current != null && current.value().containsBox()) {
                current = current.orthogonalNeighbours().get(direction);
            }
            //Check if the line can be moved
            if (current != null && current.value().isEmpty()) {
                //because all the boxes are the same we could just swap the first with the empty space
                moveTileContent(nextTile, current);
                return true;
            } else {
                return false;
            }
        }

        private boolean pushBigBoxHorizontally(Item<Tile> nextTile, Direction direction) {
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
                return true;
            } else {
                return false;
            }
        }
    }

    private static Warehouse parseInput(String block) {
        AtomicInteger idGenerator = new AtomicInteger(0);
        Map<Position, Integer> bigBoxes = new HashMap<>();
        Matrix<Tile> matrix = inputAsCharMatrix(new ByteArrayInputStream(block.getBytes())).map(i -> {
            return new Tile(switch (i.value().toString()) {
                case "#" -> new Obstacle();
                case "@" -> new Robot();
                case "O" -> new Box();
                case "[" -> {
                    BigBoxHalf bigBox = new BigBoxHalf(idGenerator.getAndIncrement(), LEFT);
                    bigBoxes.put(i.position(), bigBox.id);
                    yield bigBox;
                }
                case "]" -> new BigBoxHalf(bigBoxes.get(i.orthogonalNeighbours().west().position()), RIGHT);
                default -> null;
            });
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
        movements.chars().forEachOrdered(c -> {
            warehouse.moveRobot((char)c);
            Logger.debug("%s", warehouse.tiles.toString());
        });

        int res = warehouse.tiles.items()
                .filter(i -> i.value().containsBigBox() && i.value().content(BigBoxHalf.class).part == LEFT)
                .mapToInt(i -> i.position().row() * 100 + i.position().col())
                .sum();
        Logger.info("%s", warehouse.tiles.toString());
        return Integer.toString(res);

    }

    @Override
    public Consumer<Graphics2D> visualisePart2(InputStream input, String... params) {
        String[] blocks = inputAsString(input).split("\\n\\n");
        Deque<Character> movements = blocks[1].chars().mapToObj(c -> (char)c).collect(Collectors.toCollection(ArrayDeque::new));
        String map = blocks[0]
                .replace("#", "##")
                .replace("O", "[]")
                .replace(".", "..")
                .replace("@",  "@.");
        Warehouse warehouse = parseInput(map);
        int nMovements = movements.size();
        for (int i = 0; i < 1618; i++) {
            warehouse.moveRobot(movements.pop());
        }
        return graphics -> {
            if (!movements.isEmpty()) {
                warehouse.moveRobot(movements.pop());
                warehouse.tiles.items().forEach(i -> {
                    int tileSize = 16;
                    int halfTileSize = tileSize / 2;
                    var position = i.position().multiply(tileSize);
                    if (i.value().containsObstacle()) {
                        graphics.setColor(Color.RED);
                        graphics.fillRect(position.col(), position.row(), tileSize, tileSize);
                    } else if (i.value().containsRobot()) {
                        graphics.setColor(Color.GREEN);
                        graphics.fillRect(position.col(), position.row(), tileSize, tileSize);
                    } else {
                        if (i.value().containsBigBox() && i.value().content(BigBoxHalf.class).part == LEFT) {
                            graphics.setColor(Color.YELLOW);
                            graphics.fillRect(position.col() + halfTileSize, position.row(), halfTileSize, tileSize);
                        } else if (i.value().containsBigBox() && i.value().content(BigBoxHalf.class).part == RIGHT) {
                            graphics.setColor(Color.YELLOW);
                            graphics.fillRect(position.col(), position.row(), halfTileSize, tileSize);
                        }
                    }
                });
                graphics.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Font font = new Font("Serif", Font.PLAIN, 20);
                graphics.setFont(font);
                graphics.drawString("Move: %d".formatted(nMovements - movements.size()), 10, 40);
            }
        };
    }
}
