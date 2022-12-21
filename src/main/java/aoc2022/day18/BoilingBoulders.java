package aoc2022.day18;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import aoccommon.InputHelper;
import aoccommon.Point;

/** Solution for {@link https://adventofcode.com/2022/day/18}. */
public class BoilingBoulders {

  private static final String INPUT = "aoc2022/day18/input.txt";

  public static void main(String[] args) throws Exception {
    Set<Point> cubes = InputHelper.linesFromResource(INPUT).map(Point::parse).collect(Collectors.toSet());

    long externalSides = cubes.stream()
        .map(BoilingBoulders::neighbours)
        .mapToLong(s -> s.filter(n -> !cubes.contains(n)).count())
        .sum();

    System.out.println("Part 1: " + externalSides);

    long reachableSides = countExternallyReachableSides(cubes);

    System.out.println("Part 2: " + reachableSides);
  }

  private static Stream<Point> neighbours(Point cube) {
    return Stream.of(
        Point.of(cube.getX() - 1, cube.getY(), cube.getZ()),
        Point.of(cube.getX() + 1, cube.getY(), cube.getZ()),
        Point.of(cube.getX(), cube.getY() - 1, cube.getZ()),
        Point.of(cube.getX(), cube.getY() + 1, cube.getZ()),
        Point.of(cube.getX(), cube.getY(), cube.getZ() - 1),
        Point.of(cube.getX(), cube.getY(), cube.getZ() + 1));
  }

  private static long countExternallyReachableSides(Set<Point> cubes) {
    // Creating a bounding box larger than all the lava cubes.
    int[] xrange = new int[2];
    int[] yrange = new int[2];
    int[] zrange = new int[2];
    for (Point cube : cubes) {
      xrange[0] = Math.min(cube.getX() - 1, xrange[0]);
      yrange[0] = Math.min(cube.getY() - 1, yrange[0]);
      zrange[0] = Math.min(cube.getZ() - 1, zrange[0]);

      xrange[1] = Math.max(cube.getX() + 1, xrange[1]);
      yrange[1] = Math.max(cube.getY() + 1, yrange[1]);
      zrange[1] = Math.max(cube.getZ() + 1, zrange[1]);
    }

    Point start = Point.of(xrange[0], yrange[0], zrange[0]);

    Queue<Point> queue = new LinkedList<>();
    queue.offer(start);

    Set<Point> visited = new HashSet<>();
    visited.add(start);

    Map<Point, Integer> sideCounter = new HashMap<>();

    while (!queue.isEmpty()) {
      final Point current = queue.poll();
      neighbours(current)
          .filter(n -> !visited.contains(n))
          .filter(n -> inRange(n.getX(), xrange))
          .filter(n -> inRange(n.getY(), yrange))
          .filter(n -> inRange(n.getZ(), zrange))
          .forEach(n -> {
            if (cubes.contains(n)) {
              sideCounter.compute(n, (k, v) -> v == null ? 1 : v + 1);
            } else {
              visited.add(n);
              queue.offer(n);
            }
          });
    }
    return sideCounter.values().stream().mapToInt(i -> i).sum();
  }

  private static boolean inRange(int p, int[] range) {
    return p >= range[0] && p <= range[1];
  }
}
