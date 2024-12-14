package net.maesierra.adventOfCode2024.solutions.day9;

import net.maesierra.adventOfCode2024.Runner;
import net.maesierra.adventOfCode2024.utils.Logger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsString;

public class Day9 implements Runner.Solution {
    final static FileId EMPTY = new FileId(-1);
    record FileId(int value) {
        @Override
        public String toString() {
            return value == -1 ? "." : Integer.toString(value);
        }
    }

    static class Block {
       private final FileId fileId;
       private int size;
       private boolean moved = false;
       private Optional<Block> prev = Optional.empty();
       private Optional<Block> next = Optional.empty();

       Block(FileId fileId, int size) {
            this.fileId = fileId;
            this.size = size;
       }

       void link(Block prev) {
           this.prev = Optional.of(prev);
           prev.next = Optional.of(this);
       }
       boolean isEmptySpace() {
           return fileId == EMPTY;
       }


        @Override
        public String toString() {
           List<String> numbers = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                numbers.add(fileId.toString());
            }
           return String.join("|", numbers);
        }

        public void increase() {
            this.size += 1;
        }

        public void decrease() {
            decrease(1);
        }

        public void decrease(int n) {
            this.size -= n;
        }

        public Optional<Block> prev() {
            return prev;
        }

        public Optional<Block> next() {
            return next;
        }
    }

    static class FileSystem {
        private Block first;
        private Block last;

        FileSystem(List<Block> blocks) {
            for (int i = 0; i < blocks.size(); i++) {
                Block block = blocks.get(i);
                if (i == 0) {
                    first = block;
                    continue;
                }
                block.link(blocks.get(i - 1));
            }
            last = blocks.get(blocks.size() - 1);
        }

        public <T> T reduce(T initial, BiFunction<T, Block, T> accumulator) {
            Block block = first;
            T value = initial;
            while (block.next.isPresent()) {
                value = accumulator.apply(value, block);
                block = block.next.get();
            }
            return accumulator.apply(value, block);
        }

        @Override
        public String toString() {
            return String.join("||", reduce(new ArrayList<String>(), (l, b) -> {
                l.add(b.toString());
                return l;
            }));
        }

        public Long checksum() {
            int pos = 0;
            long checksum = 0L;
            Block block = first;
            while (block.next.isPresent()) {
                for (int i = 0; i < block.size; i++) {
                    if (block.fileId != EMPTY) {
                        checksum += (long) block.fileId.value() * pos;
                    }
                    pos ++;
                }
                block = block.next.get();

            }
            return checksum;

        }

        public Block first() {
            return this.first;
        }

        public Block last() {
            return this.last;
        }

        public Optional<Block> nextEmpty(Block block) {
            while (!block.isEmptySpace()) {
                if (block.next.isEmpty()) {
                    return Optional.empty();
                }
                block = block.next.get();
            }
            return Optional.of(block);
        }

        public Optional<Block> prevNotEmpty(Block block) {
            while (block.isEmptySpace()) {
                if (block.prev.isEmpty()) {
                    return Optional.empty();
                }
                block = block.prev.get();
            }
            return Optional.of(block);
        }

        public void add(Block block) {
            block.link(last);
            last = block;
        }

        public void remove(Block block) {
            Optional<Block> prev = block.prev;
            Optional<Block> next = block.next;
            if (next.isPresent() && prev.isPresent()) {
                next.get().link(prev.get());
            } else if (next.isPresent()) {
                //First element
                next.get().prev = Optional.empty();
                first = next.get();
            } else if (prev.isPresent()){
                //Last element
                prev.get().next = Optional.empty();
                last = prev.get();
            }
        }

        public void insertBefore(Block block, Block blockToAdd) {
            if (block.prev.isPresent()) {
                Block before = block.prev.get();
                blockToAdd.link(before);
            } else {
                first = blockToAdd;
            }
            block.link(blockToAdd);
        }

        public Optional<Block> nextEmptyFitting(Block block) {
            Optional<Block> current = Optional.of(first());
            while (current.isPresent()) {
                Block c = current.get();
                if (c == block) {
                    return Optional.empty();
                }
                if (c.isEmptySpace() && c.size >= block.size) {
                    return current;
                }
                current = current.flatMap(Block::next);
            }
            return Optional.empty();
        }
    }

    private static FileSystem parseInput(InputStream input) {
        AtomicInteger fileIdGenerator = new AtomicInteger(0);
        AtomicBoolean isGap = new AtomicBoolean(false);
        return new FileSystem(inputAsString(input).trim().chars().mapToObj(c -> {
            int blockSize = Integer.parseInt(((char) c) + "");
            boolean gap = isGap.getAndSet(!isGap.get());
            FileId fileId = gap ? EMPTY : new FileId(fileIdGenerator.getAndIncrement());
            return new Block(fileId, blockSize);
        }).filter(b -> b.size > 0).toList());
    }

    @Override
    public String part1(InputStream input, String... params) {
        FileSystem fileSystem = parseInput(input);
        Logger.debug("Filesystem: %s", fileSystem);
        int toMove = fileSystem.reduce(0, (count, b) -> b.isEmptySpace() ? count + b.size : count);
        //Make sure the last block contains empty spaces
        Block emptySpaces = fileSystem.last();
        if (!emptySpaces.isEmptySpace()) {
            emptySpaces = new Block(EMPTY, 0);
            fileSystem.add(emptySpaces);
        }
        Block leftBlock = fileSystem.nextEmpty(fileSystem.first()).orElseThrow();
        Block rightBlock = fileSystem.prevNotEmpty(fileSystem.last()).orElseThrow();
        while (toMove > 0) {
            FileId fileId = rightBlock.fileId;
            //Add the file to the first matching block to the left of the space (it may be a new block)
            Block l = leftBlock;
            leftBlock.prev.filter(b -> !b.isEmptySpace() && b.fileId == fileId).orElseGet(() -> {
                Block b = new Block(fileId, 0);
                fileSystem.insertBefore(l, b);
                return b;
            }).increase();
            //Remove the file from the original block
            rightBlock.decrease();
            //Take one empty space
            leftBlock.decrease();
            //Add empty space to the end
            emptySpaces.increase();
            toMove--;
            Logger.debug("Filesystem: %s", () -> new Object[]{fileSystem});
            if (leftBlock.size == 0) {
                fileSystem.remove(leftBlock);
                leftBlock = fileSystem.nextEmpty(fileSystem.first()).orElseThrow();
            }
            if (rightBlock.size == 0) {
                fileSystem.remove(rightBlock);
                rightBlock = fileSystem.prevNotEmpty(fileSystem.last()).orElseThrow();
            }
        }
        return Long.toString(fileSystem.checksum());
    }

    @Override
    public String part2(InputStream input, String... params) {
        FileSystem fileSystem = parseInput(input);
        Optional<Block> block = Optional.of(fileSystem.last());
        while (block.isPresent()) {
            Block b = block.get();
            block = block.flatMap(Block::prev);
            if (!b.isEmptySpace() && !b.moved) {
                fileSystem.nextEmptyFitting(b).ifPresent(dest -> {
                    //Add the block before
                    Block newBlock = new Block(b.fileId, b.size);
                    newBlock.moved = true;
                    fileSystem.insertBefore(dest, newBlock);
                    //Decrease the size and remove if there is no more space
                    dest.decrease(b.size);
                    if (dest.size == 0) {
                        fileSystem.remove(dest);
                    }
                    //Replace the moved block with empty space
                    fileSystem.insertBefore(b, new Block(EMPTY, b.size));
                    fileSystem.remove(b);
                });
            }
        }
        Long checksum = fileSystem.checksum();
        Logger.debug("Filesystem: %s", fileSystem);
        return Long.toString(checksum);
    }
}
