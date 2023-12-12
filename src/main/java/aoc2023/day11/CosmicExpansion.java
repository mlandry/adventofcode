package aoc2023.day11;

import aoccommon.Debug;
import aoccommon.InputHelper;
import aoccommon.Point;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Solution for {@link https://adventofcode.com/2023/day/11}.
 */
public class CosmicExpansion {

  private static final String INPUT = "aoc2023/day11/input.txt";
  private static final String EXAMPLE = "aoc2023/day11/example.txt";

  private static final char GALAXY = '#';
  private static final char EMPTY = '.';

  private record Universe(List<List<Character>> image, Set<Integer> emptyRows, Set<Integer> emptyCols) {
    static Universe create(List<List<Character>> image) {
      Set<Integer> emptyRows = IntStream.range(0, image.size())
          .filter(row -> image.get(row).stream().allMatch(i -> i == EMPTY))
          .mapToObj(i -> i)
          .collect(Collectors.toSet());
      Set<Integer> emptyCols = IntStream.range(0, image.get(0).size())
          .filter(col -> image.stream().map(row -> row.get(col)).allMatch(i -> i == EMPTY))
          .mapToObj(i -> i)
          .collect(Collectors.toSet());
      return new Universe(image, emptyRows, emptyCols);
    }

    List<Point> galaxies() {
      return IntStream.range(0, image.size())
          .mapToObj(i -> i)
          .flatMap(row -> IntStream.range(0, image.get(row).size())
              .filter(col -> image.get(row).get(col) == GALAXY)
              .mapToObj(col -> Point.of(col, row)))
          .toList();
    }

    long distance(Point a, Point b, long age) {
      int minRow = Math.min(a.getY(), b.getY());
      int maxRow = Math.max(a.getY(), b.getY());
      int minCol = Math.min(a.getX(), b.getX());
      int maxCol = Math.max(a.getX(), b.getX());

      long expandingRows = emptyRows.stream().filter(row -> row > minRow && row < maxRow).count();
      long expandingCols = emptyCols.stream().filter(col -> col > minCol && col < maxCol).count();

      return (expandingRows * (age + 1))
          + (expandingCols * (age + 1))
          + (maxRow - minRow - expandingRows)
          + (maxCol - minCol - expandingCols);
    }

    void print() {
      for (List<Character> row : image) {
        StringBuilder sb = new StringBuilder();
        for (char c : row) {
          sb.append(c);
        }
        Debug.println(sb.toString());
      }
    }
  }

  public static void main(String[] args) throws Exception {
    // Debug.enablePrint();
    Universe universe = Universe.create(InputHelper.linesFromResource(INPUT)
        .map(str -> str.chars().mapToObj(i -> (char) i).toList())
        .toList());

    // Part 1.
    List<Point> galaxies = universe.galaxies();
    long sum = 0;
    for (int i = 0; i < galaxies.size(); i++) {
      Point galaxy = galaxies.get(i);
      sum += IntStream.range(i + 1, galaxies.size())
          .mapToObj(galaxies::get)
          .mapToLong(g -> universe.distance(galaxy, g, 1))
          .sum();
    }
    System.out.println("Part 1: " + sum);

    // Part 2.
    sum = 0;
    for (int i = 0; i < galaxies.size(); i++) {
      Point galaxy = galaxies.get(i);
      sum += IntStream.range(i + 1, galaxies.size())
          .mapToObj(galaxies::get)
          .mapToLong(g -> universe.distance(galaxy, g, 1000000 - 1))
          .sum();
    }
    System.out.println("Part 1: " + sum);
  }
}
