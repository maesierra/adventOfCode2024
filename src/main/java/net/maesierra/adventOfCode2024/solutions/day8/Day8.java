package net.maesierra.adventOfCode2024.solutions.day8;

import net.maesierra.adventOfCode2024.Main;
import net.maesierra.adventOfCode2024.utils.Space2D;
import net.maesierra.adventOfCode2024.utils.Space2D.Line;
import net.maesierra.adventOfCode2024.utils.Space2D.Point;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static net.maesierra.adventOfCode2024.utils.IOHelper.inputAsSpace2D;
import static net.maesierra.adventOfCode2024.utils.Space2D.Line.interpolate;

public class Day8 implements Main.Solution {

    record Antenna(String frequency, Point position) {
        Stream<Point> antinodes(Antenna a2, int multiplier) {
            Antenna a1 = this;
            BigDecimal m = new BigDecimal(multiplier);
            //Determine the line between the 2 antennas
            Line line = interpolate(a1.position, a2.position);
            //Antinode on a2
            //x = a2.x  + a2.distanceX(a1) * multiplier
            BigDecimal x = a2.position.x().add(a2.position().distanceX(a1.position).multiply(m));
            Point position1 = line.get(x).round(2);

            //Antinode on a1
            //x = a1.x  + a1.distanceX(a2) * multiplier
            x = a1.position.x().add(a1.position().distanceX(a2.position).multiply(m));
            Point position2 = line.get(x).round(2);

            return Stream.of(position1, position2);

        }
    }
    record AntennaMap(Space2D<Antenna> space, Map<String, List<Antenna>> antennas) {

    }

    private static AntennaMap parseInput(InputStream input) {
        Space2D<Antenna> space = inputAsSpace2D(input, (point, c) -> {
            if (c == '.') {
                return null;
            } else {
                return new Antenna(c.toString(), point);
            }
        });
        Map<String, List<Antenna>> antennas = space.items().reduce(
                new HashMap<>(),
                (map, a) -> {
                    map.computeIfAbsent(a.frequency(), k -> new ArrayList<>()).add(a);
                    return map;
                },
                (a, b) -> a
        );
        return new AntennaMap(space, antennas);
    }

    @Override
    public String part1(InputStream input, String... params) {
        AntennaMap map = parseInput(input);
        Set<Point> antinodes = new HashSet<>();

        map.antennas.forEach((freq, list) -> {
            if (list.size() == 1) {
                return;
            }
            Map<Antenna, Set<Antenna>> calculated = new HashMap<>();
            for (Antenna a1:list) {
                list.stream()
                    .filter(a -> a != a1)
                    .forEachOrdered(a2 -> {
                        if (calculated.getOrDefault(a1, Set.of()).contains(a2)) {
                            return;
                        }
                        antinodes.addAll(a1.antinodes(a2, 1).filter(map.space::contains).toList());
                        calculated.computeIfAbsent(a1, k -> new HashSet<>()).add(a2);
                        calculated.computeIfAbsent(a2, k -> new HashSet<>()).add(a1);
                    });

            }
        });
        return Integer.toString(antinodes.size());
    }

    @Override
    public String part2(InputStream input, String... params) {
        AntennaMap map = parseInput(input);
        Set<Point> res = new TreeSet<>();

        map.antennas.forEach((freq, list) -> {
            if (list.size() == 1) {
                return;
            }
            Map<Antenna, Set<Antenna>> calculated = new HashMap<>();
            for (Antenna a1:list) {
                list.stream()
                        .filter(a -> a != a1)
                        .forEachOrdered(a2 -> {
                            if (calculated.getOrDefault(a1, Set.of()).contains(a2)) {
                                return;
                            }
                            //The antennas are also antinodes
                            res.add(a1.position);
                            res.add(a2.position);

                            boolean found = true;
                            int multipler = 1;
                            while (found) {
                                List<Point> antinodes = a1.antinodes(a2, multipler).filter(map.space::contains).toList();
                                found = !antinodes.isEmpty();
                                res.addAll(antinodes);
                                multipler ++;

                            }
                            calculated.computeIfAbsent(a1, k -> new HashSet<>()).add(a2);
                            calculated.computeIfAbsent(a2, k -> new HashSet<>()).add(a1);
                        });

            }
        });
        return Integer.toString(res.size());
    }
}
