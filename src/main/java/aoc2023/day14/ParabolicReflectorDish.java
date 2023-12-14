package aoc2023.day14;

import aoccommon.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Solution for {@link https://adventofcode.com/2023/day/14}.
 */
public class ParabolicReflectorDish {

  private static final String INPUT = "aoc2023/day14/input.txt";
  private static final String EXAMPLE = "aoc2023/day14/example.txt";

  private static final char ROUNDED_ROCK = 'O';
  private static final char CUBE_ROCK = '#';
  private static final char EMPTY = '.';

  private record Platform(Matrix<Character> matrix) {
    Platform tilt(Direction dir) {
      Matrix<Character> copy = matrix.copy();
      Stream<Point> points = switch (dir) {
        case UP, LEFT -> IntStream.range(0, copy.height())
            .mapToObj(r -> r)
            .flatMap(r -> IntStream.range(0, copy.width())
                .mapToObj(c -> Point.of(c, r)));
        case RIGHT, DOWN -> IntStream.range(0, copy.height())
            .mapToObj(r -> copy.height() - 1 - r)
            .flatMap(r -> IntStream.range(0, copy.width())
                .map(c -> copy.width() - 1 - c)
                .mapToObj(c -> Point.of(c, r)));
      };
      points.forEach(point -> {
        char c = copy.get(point);
        if (c == ROUNDED_ROCK) {
          Optional<Point> moved = findSpot(copy, point, dir);
          if (moved.isPresent()) {
            copy.set(moved.get(), ROUNDED_ROCK);
            copy.set(point, EMPTY);
          }
        }
      });
      return new Platform(copy);
    }

    static Optional<Point> findSpot(Matrix<Character> matrix, Point point, Direction dir) {
      Optional<Point> candidate = Optional.empty();
      point = dir.apply(point);
      while (point.getX() >= 0
          && point.getX() < matrix.width()
          && point.getY() >= 0
          && point.getY() < matrix.height()) {
        if (matrix.get(point) == EMPTY) {
          candidate = Optional.of(point);
        } else {
          break;
        }
        point = dir.apply(point);
      }
      return candidate;
    }

    long calculateLoad() {
      return IntStream.range(0, matrix.height())
          .mapToLong(r ->
              matrix.row(r).filter(c -> c == ROUNDED_ROCK).count() * (matrix.height() - r))
          .sum();
    }

    void print() {
      matrix.rows().stream()
          .forEach(r -> Debug.println(r.stream()
              .map(c -> Character.toString(c))
              .collect(Collectors.joining())));
    }
  }


  public static void main(String[] args) throws Exception {
    // Debug.enablePrint();
    Platform platform = new Platform(new Matrix(InputHelper.linesFromResource(INPUT)
        .filter(line -> !line.isBlank())
        .map(line -> line.chars().mapToObj(c -> (char) c).collect(Collectors.toList()))
        .toList()));

    // Part 1.
    Platform tilted = platform.tilt(Direction.UP);
    long load = tilted.calculateLoad();
    System.out.println("Part 1: " + load);

    // Part 2.
    tilted = platform;
    Direction[] cycle = new Direction[]{Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT};
    Map<Integer, Long> cachedStates = new HashMap<>();

    long i = 0;
    long previous = -1;
    cachedStates.put(tilted.hashCode(), i);
    for (; i < 1000000000; i++) {
      for (Direction dir : cycle) {
        tilted = tilted.tilt(dir);
      }

      int hash = tilted.hashCode();
      Long cached = cachedStates.put(hash, i);
      if (cached != null) {
        previous = cached;
        Debug.println("Found cycle: %d-%d", previous, i);
        break;
      }
    }
    // We found a repeating cycle. Now just extrapolate where we will end up.
    long remaining = (1000000000 - i) % (i - previous) - 1;
    for (long j = 0; j < remaining; j++) {
      for (Direction dir : cycle) {
        tilted = tilted.tilt(dir);
      }
    }
    load = tilted.calculateLoad();
    System.out.println("Part 2: " + load);
  }
}
