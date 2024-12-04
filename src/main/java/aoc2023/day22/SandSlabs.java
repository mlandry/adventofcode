package aoc2023.day22;

import aoccommon.InputHelper;
import aoccommon.Pair;
import aoccommon.Point;
import aoccommon.Range;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Solution for {@link https://adventofcode.com/2023/day/22}. */
public class SandSlabs {

  private static final String INPUT = "aoc2023/day22/input.txt";
  private static final String EXAMPLE = "aoc2023/day22/example.txt";

  private record Brick(Point a, Point b) {
    static Brick parse(String line) {
      String[] split = line.split("~");
      return new Brick(Point.parse(split[0]), Point.parse(split[1]));
    }

    Set<Point> points() {
      Pair<Integer, Integer> xRange = minMax(Point::getX);
      Pair<Integer, Integer> yRange = minMax(Point::getY);
      Pair<Integer, Integer> zRange = minMax(Point::getZ);
      return IntStream.rangeClosed(xRange.first(), xRange.second())
          .mapToObj(x -> x)
          .flatMap(x ->
              IntStream.rangeClosed(yRange.first(), yRange.second())
                  .mapToObj(y -> y)
                  .flatMap(y ->
                      IntStream.rangeClosed(zRange.first(), zRange.second())
                          .mapToObj(z -> Point.of(x, y, z))))
          .collect(Collectors.toSet());

    }

    Set<Point> plane(int z) {
      return points().stream().filter(p -> p.getZ() == z).collect(Collectors.toSet());
    }

    Pair<Integer, Integer> minMax(Function<Point, Integer> dimension) {
      return Pair.of(
          Math.min(dimension.apply(a), dimension.apply(b)),
          Math.max(dimension.apply(a), dimension.apply(b)));
    }
  }
  public static void main(String [] args) throws Exception {
    List<Brick> bricks = InputHelper.linesFromResource(EXAMPLE).map(Brick::parse).toList();
    
  }
}
