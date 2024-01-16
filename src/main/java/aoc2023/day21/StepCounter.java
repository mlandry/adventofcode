package aoc2023.day21;

import aoccommon.*;

import java.util.*;

/**
 * Solution for {@link https://adventofcode.com/2023/day/27}.
 */
public class StepCounter {

  private static final String INPUT = "aoc2023/day21/input.txt";
  private static final String EXAMPLE = "aoc2023/day21/example.txt";

  private static final char START = 'S';
  private static final char ROCK = '#';
  private static final char GARDEN = '.';

  private record GardenMap(List<String> rows) {
    private List<Point> getNextSteps(Point p) {
      return Arrays.stream(Direction.values()).map(d -> d.apply(p)).filter(this::isWalkable).toList();
    }

    private boolean inBounds(Point p) {
      return p.getY() >= 0 && p.getY() < rows.size() && p.getX() >= 0 && p.getX() < rows.get(p.getY()).length();
    }

    private char get(Point p) {
      return rows.get(p.getY()).charAt(p.getX());
    }

    private boolean isWalkable(Point p) {
      if (!inBounds(p)) {
        throw new IllegalStateException(p.toString());
        // Debug.println("out of bounds? %s", p);
        // return false;
      }
      char c = get(p);
      return c != ROCK;
    }

    private Point findStart() {
      for (int y = 0; y < rows.size(); y++) {
        for (int x = 0; x < rows.get(y).length(); x++) {
          Point p = Point.of(x, y);
          if (get(p) == START) {
            return p;
          }
        }
      }
      return null;
    }

    private int countPlotsReached(int steps) {
      Queue<Point> queue = new ArrayDeque<>();
      queue.add(this.findStart());
      for (int i = 0; i < steps; i++) {
        int size = queue.size();
        Set<Point> next = new HashSet<>();
        for (int j = 0; j < size; j++) {
          Point p = queue.remove();
          this.getNextSteps(p).forEach(next::add);
        }
        queue.addAll(next);
      }
      return queue.size();
    }
  }

  private static class InfiniteGardenMap {
    private final GardenMap map;
    private final int length;
    private final int height;

    InfiniteGardenMap(GardenMap map) {
      this.map = map;
      this.length = map.rows.get(0).length();
      this.height = map.rows.size();
    }

    private Point normalize(Point p) {
      return Point.of(Math.floorMod(p.getX(), length), Math.floorMod(p.getY(), height));
    }

    private List<Point> getNextSteps(Point point) {
      return Arrays.stream(Direction.values())
          .map(d -> d.apply(point))
          .filter(p -> map.isWalkable(normalize(p)))
          .toList();
    }

    private int countPlotsReached(int steps) {
      Queue<Point> queue = new ArrayDeque<>();
      queue.add(map.findStart());
      for (int i = 0; i < steps; i++) {
        int size = queue.size();
        Set<Point> next = new HashSet<>();
        for (int j = 0; j < size; j++) {
          Point p = queue.remove();
          this.getNextSteps(p).forEach(next::add);
        }
        queue.addAll(next);
      }
      return queue.size();
    }
  }

  public static void main(String[] args) throws Exception {
    // Debug.enablePrint();
    GardenMap map = new GardenMap(InputHelper.linesFromResource(INPUT).filter(s -> !s.isBlank()).toList());

    // Part 1.
    System.out.println("Part 1: " + map.countPlotsReached(64));

    // Part 2.
    InfiniteGardenMap infinite = new InfiniteGardenMap(map);
    // With some hints, come up with a polynomial formula to solve the infinite grid for N steps.
    // Assumptions:
    // - Start is in the center of the grid (i.e. [65,65] for 131x131 grid)
    // - Center column and row are empty (i.e. after 65 steps we break out into adjacent grids)
    // - It then takes another 131 steps to repeat the breakout cycle.
    // - The number of steps 26501365 can be expressed as a function of N * 131 + 65. (N=202300).
    // - If we solve x=0,y=f(65), x=1,y=f(131 + 65), x=2,y=f(2 * 131 + 65) we can compute a polynomial for f(x * 131 + 65)

    // We could implement our own polynomial interpolator.
    // ...or plug these values into a calculator
    for (int x = 0; x < 3; x++) {
      int y = infinite.countPlotsReached((131 * x) + 65);
      Debug.println("x=%d y=%d", x, y);
    }
    // x = {0, 1, 2} y = {3755, 33494, 92811}
    // which gives y = 14789x^2 + 14950x + 3755
    long x = (26501365L - 65) / 131L;
    long y = (14789 * x * x) + (14950 * x) + 3755;
    System.out.println("Part 2: " + y);
  }
}
