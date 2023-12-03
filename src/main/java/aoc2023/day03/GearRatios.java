package aoc2023.day03;

import aoccommon.InputHelper;
import aoccommon.Point;

import java.util.*;

/**
 * Solution for {@link https://adventofcode.com/2023/day/3}.
 */
public class GearRatios {

  private static final String INPUT = "aoc2023/day03/input.txt";

  public static void main(String[] args) throws Exception {
    List<String> schematic = InputHelper.linesFromResource(INPUT).toList();

    // Part 1.
    List<Integer> partNumbers = new ArrayList<>();
    for (int line = 0; line < schematic.size(); line++) {
      Integer current = null;
      boolean adjacentToSymbol = false;
      for (int i = 0; i < schematic.get(line).length(); i++) {
        char c = schematic.get(line).charAt(i);
        if (Character.isDigit(c)) {
          if (current == null) {
            current = c - '0';
          } else {
            current = (current * 10) + (c - '0');
          }
          adjacentToSymbol = adjacentToSymbol || adjacentToSymbol(schematic, line, i);
          continue;
        }
        if (current != null) {
          if (adjacentToSymbol) {
            partNumbers.add(current);
          }
          current = null;
          adjacentToSymbol = false;
        }
      }
      if (current != null && adjacentToSymbol) {
        partNumbers.add(current);
      }
    }

    long sum = partNumbers.stream().mapToLong(p -> p).sum();
    System.out.println("Part 1: " + sum);

    // Part 2.
    Map<Point, List<Integer>> gearRatios = new HashMap<>();
    for (int line = 0; line < schematic.size(); line++) {
      Integer current = null;
      Set<Point> adjacentGears = new HashSet<>();
      for (int i = 0; i < schematic.get(line).length(); i++) {
        char c = schematic.get(line).charAt(i);
        if (Character.isDigit(c)) {
          if (current == null) {
            current = c - '0';
          } else {
            current = (current * 10) + (c - '0');
          }
          adjacentGears.addAll(adjacentGears(schematic, line, i));
          continue;
        }
        if (current != null) {
          if (!adjacentGears.isEmpty()) {
            final int ratio = current;
            adjacentGears.forEach(g -> gearRatios.computeIfAbsent(g, p -> new ArrayList<>()).add(ratio));
          }
          current = null;
          adjacentGears.clear();
        }
      }
      if (current != null && !adjacentGears.isEmpty()) {
        final int ratio = current;
        adjacentGears.forEach(g -> gearRatios.computeIfAbsent(g, p -> new ArrayList<>()).add(ratio));
      }
    }

    sum = gearRatios.values().stream()
        .filter(p -> p.size() == 2)
        .mapToLong(p -> p.stream().mapToLong(i -> i).reduce((left, right) -> left * right).getAsLong())
        .sum();
    System.out.println("Part 2: " + sum);
  }

  private static boolean adjacentToSymbol(List<String> schematic, int line, int i) {
    for (int row = Math.max(line - 1, 0); row <= Math.min(line + 1, schematic.size() - 1); row++) {
      for (int col = Math.max(i - 1, 0); col <= Math.min(i + 1, schematic.get(row).length() - 1); col++) {
        if (row == line && col == i) {
          continue;
        }
        char c = schematic.get(row).charAt(col);
        if (c == '.' || Character.isDigit(c)) {
          continue;
        }
        return true;
      }
    }
    return false;
  }

  private static Set<Point> adjacentGears(List<String> schematic, int line, int i) {
    Set<Point> gears = new HashSet<>();
    for (int row = Math.max(line - 1, 0); row <= Math.min(line + 1, schematic.size() - 1); row++) {
      for (int col = Math.max(i - 1, 0); col <= Math.min(i + 1, schematic.get(row).length() - 1); col++) {
        if (row == line && col == i) {
          continue;
        }
        char c = schematic.get(row).charAt(col);
        if (c == '*') {
          gears.add(Point.of(row, col));
        }
      }
    }
    return gears;
  }
}
