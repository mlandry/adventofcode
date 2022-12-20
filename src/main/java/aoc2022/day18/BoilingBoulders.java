package aoc2022.day18;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import aoccommon.InputHelper;
import aoccommon.Point;

/** Solution for {@link https://adventofcode.com/2022/day/18}. */
public class BoilingBoulders {

  private static final String INPUT = "aoc2022/day18/input.txt";

  public static void main(String[] args) throws Exception {
    List<Cube> cubes = InputHelper.linesFromResource(INPUT).map(Cube::parse).collect(Collectors.toList());

    Map<Side, Integer> sideCounts = new HashMap<>();
    for (Cube cube : cubes) {
      for (Side side : cube.sides) {
        sideCounts.compute(side, (k, v) -> v == null ? 1 : v + 1);
      }
    }

    Set<Side> externalSides = sideCounts.entrySet().stream()
        .filter(e -> e.getValue() == 1)
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
    System.out.println("Part 1: " + externalSides.size());

    Set<Side> reachableExternalSides = findExternallyReachableSides(cubes, sideCounts, externalSides);
    System.out.println("Part 2: " + reachableExternalSides.size());

  }

  private static Set<Side> findExternallyReachableSides(
      List<Cube> cubes,
      Map<Side, Integer> sideCounts,
      Set<Side> externalSides) {

    Set<Point> externalSidePoints = externalSides.stream()
        .map(Side::corners)
        .flatMap(Set::stream)
        .collect(Collectors.toSet());

    // Build a bounding box to enclose the lava droplets
    int[] xrange = new int[2];
    int[] yrange = new int[2];
    int[] zrange = new int[2];
    for (Cube cube : cubes) {
      xrange[0] = Math.min(cube.coordinates.getX() - 1, xrange[0]);
      yrange[0] = Math.min(cube.coordinates.getY() - 1, yrange[0]);
      zrange[0] = Math.min(cube.coordinates.getZ() - 1, zrange[0]);

      xrange[1] = Math.max(cube.coordinates.getX() + 2, xrange[1]);
      yrange[1] = Math.max(cube.coordinates.getY() + 2, yrange[1]);
      zrange[1] = Math.max(cube.coordinates.getZ() + 2, zrange[1]);
    }

    // State for BFS traversal. The BFS finds all externally-reachable sides.
    final Set<Point> visited = new HashSet<>();
    final Queue<Point> queue = new LinkedList<>();

    Point current = Point.of(xrange[0], yrange[0], zrange[0]);
    queue.offer(current);
    visited.add(current);

    while (!queue.isEmpty()) {
      current = queue.poll();
      neighbours(current)
          .filter(n -> !visited.contains(n))
          .filter(n -> n.getX() >= xrange[0] && n.getX() <= xrange[1])
          .filter(n -> n.getY() >= yrange[0] && n.getY() <= yrange[1])
          .filter(n -> n.getZ() >= zrange[0] && n.getZ() <= zrange[1])
          .forEach(n -> {
            visited.add(n);
            if (!externalSidePoints.contains(n)) {
              queue.add(n);
            }
          });
    }
    return externalSides.stream()
        .filter(side -> side.corners().stream().anyMatch(visited::contains))
        .collect(Collectors.toSet());
  }

  private static Stream<Point> neighbours(Point point) {
    return Stream.of(
        Point.of(point.getX() - 1, point.getY(), point.getZ()),
        Point.of(point.getX() + 1, point.getY(), point.getZ()),
        Point.of(point.getX(), point.getY() - 1, point.getZ()),
        Point.of(point.getX(), point.getY() + 1, point.getZ()),
        Point.of(point.getX(), point.getY(), point.getZ() - 1),
        Point.of(point.getX(), point.getY(), point.getZ() + 1));
  }

  private static record Side(Set<Point> corners) {
    static Side of(Point a, Point b, Point c, Point d) {
      return new Side(Set.of(a, b, c, d));
    }
  }

  private static class Cube {

    private final Point coordinates;
    private final Set<Side> sides;

    private Cube(Point coordinates, Set<Side> sides) {
      this.coordinates = coordinates;
      this.sides = sides;
    }

    static Cube parse(String csv) {
      Point coordinates = Point.parse(csv);
      return new Cube(coordinates, sides(coordinates));
    }

    static Set<Side> sides(Point coordinates) {
      Function<Integer, int[]> axisRange = i -> new int[] { i, i + 1 };
      int[] xaxis = axisRange.apply(coordinates.getX());
      int[] yaxis = axisRange.apply(coordinates.getY());
      int[] zaxis = axisRange.apply(coordinates.getZ());

      Set<Side> sides = new HashSet<>();
      for (int xside = xaxis[0]; xside <= xaxis[1]; xside++) {
        sides.add(Side.of(
            Point.of(xside, yaxis[0], zaxis[0]),
            Point.of(xside, yaxis[0], zaxis[1]),
            Point.of(xside, yaxis[1], zaxis[0]),
            Point.of(xside, yaxis[1], zaxis[1])));
      }

      for (int yside = yaxis[0]; yside <= yaxis[1]; yside++) {
        sides.add(Side.of(
            Point.of(xaxis[0], yside, zaxis[0]),
            Point.of(xaxis[0], yside, zaxis[1]),
            Point.of(xaxis[1], yside, zaxis[0]),
            Point.of(xaxis[1], yside, zaxis[1])));
      }

      for (int zside = zaxis[0]; zside <= zaxis[1]; zside++) {
        sides.add(Side.of(
            Point.of(xaxis[0], yaxis[0], zside),
            Point.of(xaxis[0], yaxis[1], zside),
            Point.of(xaxis[1], yaxis[0], zside),
            Point.of(xaxis[1], yaxis[1], zside)));
      }

      return sides;
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof Cube)) {
        return false;
      }
      Cube otherCube = (Cube) other;
      return coordinates.equals(otherCube.coordinates);
    }

    @Override
    public int hashCode() {
      return coordinates.hashCode();
    }

    @Override
    public String toString() {
      return coordinates.toString();
    }
  }
}
