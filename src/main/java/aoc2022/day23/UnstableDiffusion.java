package aoc2022.day23;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import aoccommon.InputHelper;
import aoccommon.Point;

/** Solution for {@link https://adventofcode.com/2022/day/23}. */
public class UnstableDiffusion {

  private static final String INPUT = "aoc2022/day23/example.txt";

  private static enum Direction {
    NW(-1, -1),
    N(0, -1),
    NE(1, -1),
    E(1, 0),
    SE(1, 1),
    S(0, 1),
    SW(-1, 1),
    W(-1, 0);

    static final Map<Direction, EnumSet<Direction>> LOOKS = Map.of(
        N, EnumSet.of(N, NE, NW),
        S, EnumSet.of(S, SE, SW),
        W, EnumSet.of(W, NW, SW),
        E, EnumSet.of(E, NE, SE));

    static final Direction[] LOOK_ORDER = new Direction[] { N, S, W, E };

    private final int xd;
    private final int yd;

    Direction(int xd, int yd) {
      this.xd = xd;
      this.yd = yd;
    }

    Point move(Point point) {
      return Point.of(point.getX() + xd, point.getY() + yd);
    }
  }

  public static void main(String[] args) throws Exception {
    int xsize = 0;
    Set<Point> elves = new HashSet<>();

    List<String> input = InputHelper.linesFromResource(INPUT).collect(Collectors.toList());
    for (int y = 0; y < input.size(); y++) {
      String line = input.get(y);
      xsize = Math.max(xsize, line.length());
      for (int x = 0; x < line.length(); x++) {
        if (line.charAt(x) == '#') {
          elves.add(Point.of(x, y));
        }
      }
    }
    int ysize = input.size();

    for (int round = 0; round < 10; round++) {
      int lookIndex = round % Direction.LOOK_ORDER.length;
      Map<Point, Point> moves = new HashMap<>();
      // First half, each elf determines if and where they are going to move.
      final Set<Point> elvesRef = elves;
      for (Point elf : elves) {
        boolean anyElvesAdjacent = adjacent(elf, EnumSet.allOf(Direction.class), xsize, ysize)
            .anyMatch(p -> elvesRef.contains(p));
        if (!anyElvesAdjacent) {
          continue;
        }
        for (int i = 0; i < Direction.LOOK_ORDER.length; i++) {
          Direction direction = Direction.LOOK_ORDER[lookIndex + i % Direction.LOOK_ORDER.length];
          anyElvesAdjacent = adjacent(elf, Direction.LOOKS.get(direction), xsize, ysize)
              .anyMatch(p -> elvesRef.contains(p));
          if (anyElvesAdjacent) {
            continue;
          }
          moves.put(elf, direction.move(elf));
        }
      }

      Map<Point, Long> moveCounts = moves.values().stream()
          .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

      // Second half, each elf moves if they were able and no other elf selected the
      // same spot.
      Set<Point> newElves = new HashSet<>();
      for (Point elf : elves) {
        Point newSpot = moves.get(elf);
        if (newSpot == null) {
          // Stay put.
          newElves.add(elf);
          continue;
        }

        if (moveCounts.get(newSpot) > 1) {
          // Multiple elves want this spot, stay put.
          newElves.add(elf);
          continue;
        }

        // All clear to move!
        newElves.add(newSpot);
      }
      elves = newElves;
    }

    System.out.println("Part 1: " + countEmptyGroupTilesInRectangleContainingAllElves(elves));
  }

  private static Stream<Point> adjacent(Point point, EnumSet<Direction> directions, int xsize, int ysize) {
    return directions.stream()
        .map(d -> d.move(point))
        .filter(p -> p.getX() < 0)
        .filter(p -> p.getX() >= xsize)
        .filter(p -> p.getY() < 0)
        .filter(p -> p.getY() >= ysize);

  }

  private static int countEmptyGroupTilesInRectangleContainingAllElves(Set<Point> elves) {
    int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = 0, maxY = 0;
    for (Point elf : elves) {
      minX = Math.min(minX, elf.getX());
      minY = Math.min(minY, elf.getY());
      maxX = Math.max(maxX, elf.getX());
      maxY = Math.max(maxY, elf.getY());
    }

    return (maxX - minX + 1) * (maxY - minY + 1) - elves.size();
  }
}
