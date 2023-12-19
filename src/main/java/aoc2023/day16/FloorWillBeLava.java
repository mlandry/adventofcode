package aoc2023.day16;

import aoccommon.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Solution for {@link https://adventofcode.com/2023/day/16}.
 */
public class FloorWillBeLava {

  private static final String INPUT = "aoc2023/day16/input.txt";
  private static final String EXAMPLE = "aoc2023/day16/example.txt";

  private record Contraption(Matrix<Character> matrix) {
//    void print(Map<Point, Set<Direction>> energized) {
//      for (int row = 0; row < matrix.height(); row++) {
//        StringBuilder sb = new StringBuilder();
//        for (int col = 0; col < matrix().width(); col++) {
//          Point p = Point.of(col, row);
//          Set<Direction> beams = energized.get(p);
//          if (beams == null) {
//            sb.append(matrix.get(p));
//          } else if (beams.size() == 1) {
//            sb.append('#');
//          } else {
//            sb.append(beams.size());
//          }
//        }
//        Debug.println(sb.toString());
//      }
//      // Debug.waitForInput();
//    }
    Map<Point, Set<Direction>> energize(Point start, Direction direction) {
      Map<Point, Set<Direction>> energized = new HashMap<>();
      Queue<Pair<Point, Direction>> queue = new LinkedList<>();
      queue.add(Pair.of(start, direction));
      while (!queue.isEmpty()) {
        Pair<Point, Direction> current = queue.remove();
        boolean inserted = energized.computeIfAbsent(current.first(), p -> new HashSet<>()).add(current.second());
        if (!inserted) {
          continue;
        }
        getNext(current.first(), current.second())
            .filter(pair -> matrix.contains(pair.first()))
            .forEach(queue::add);
      }
      return energized;
    }

    int maximize() {
      int max = 0;
      for (int i = 0; i < matrix.height(); i++) {
        max = Math.max(max, energize(Point.of(0, i), Direction.RIGHT).size());
        max = Math.max(max, energize(Point.of(matrix.width() - 1, i), Direction.LEFT).size());
      }
      for (int i = 0; i < matrix.width(); i++) {
        max = Math.max(max, energize(Point.of(i, 0), Direction.DOWN).size());
        max = Math.max(max, energize(Point.of(i, matrix.height() - 1), Direction.UP).size());
      }
      return max;
    }

    Stream<Pair<Point, Direction>> getNext(Point p, Direction d) {
      Function<Direction, Pair<Point, Direction>> moveInDirection = (move) -> Pair.of(move.apply(p), move);
      return switch (matrix.get(p)) {
        case '.' -> Stream.of(moveInDirection.apply(d));
        case '-' -> switch (d) {
          case LEFT, RIGHT -> Stream.of(moveInDirection.apply(d));
          case UP, DOWN -> Stream.of(moveInDirection.apply(Direction.LEFT), moveInDirection.apply(Direction.RIGHT));
        };
        case '|' -> switch (d) {
          case UP, DOWN -> Stream.of(moveInDirection.apply(d));
          case LEFT, RIGHT -> Stream.of(moveInDirection.apply(Direction.UP), moveInDirection.apply(Direction.DOWN));
        };
        case '/' -> switch (d) {
          case UP -> Stream.of(moveInDirection.apply(Direction.RIGHT));
          case RIGHT -> Stream.of(moveInDirection.apply(Direction.UP));
          case DOWN -> Stream.of(moveInDirection.apply(Direction.LEFT));
          case LEFT -> Stream.of(moveInDirection.apply(Direction.DOWN));
        };
        case '\\' -> switch (d) {
          case UP -> Stream.of(moveInDirection.apply(Direction.LEFT));
          case RIGHT -> Stream.of(moveInDirection.apply(Direction.DOWN));
          case DOWN -> Stream.of(moveInDirection.apply(Direction.RIGHT));
          case LEFT -> Stream.of(moveInDirection.apply(Direction.UP));
        };
        default -> throw new IllegalArgumentException(matrix.get(p).toString());
      };
    }
  }

  public static void main(String[] args) throws Exception {
    Debug.enablePrint();
    Contraption contraption = new Contraption(
        new Matrix(InputHelper.linesFromResource(INPUT)
            .map(line -> line.chars().mapToObj(c -> (char) c).toList())
            .toList()));

    // Part 1.
    Map<Point, Set<Direction>> energized = contraption.energize(Point.of(0, 0), Direction.RIGHT);
    System.out.println("Part 1: " + energized.size());

    // Part 2.
    System.out.println("Part 2: " + contraption.maximize());
  }
}
