package net.maesierra.adventOfCode2024;

public class Main extends Runner{

    public static void main(String[] args) throws Exception {
        run(args, (data, input) -> {
            String result = switch (data.part()) {
                case "1" -> data.solution().part1(input);
                case "2" -> data.solution().part2(input);
                default -> throw new IllegalStateException("Unexpected value: " + data.part());
            };
            System.out.println(result);
        });
    }
}