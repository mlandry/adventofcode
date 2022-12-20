package aoc2022.day18;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    long nonOverlappingSides = sideCounts.entrySet().stream()
        .filter(e -> e.getValue() == 1)
        .count();
    System.out.println("Part 1: " + nonOverlappingSides);
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
      int [] xaxis = axisRange.apply(coordinates.getX());
      int [] yaxis = axisRange.apply(coordinates.getY());
      int [] zaxis = axisRange.apply(coordinates.getZ());
      
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
