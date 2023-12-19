package aoc2023.day17;

import aoccommon.Direction;
import aoccommon.InputHelper;
import aoccommon.Matrix;
import aoccommon.Point;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Solution for {@link https://adventofcode.com/2023/day/17}.
 */
public class ClumsyCrucible {

  private static final String INPUT = "aoc2023/day17/input.txt";
  private static final String EXAMPLE = "aoc2023/day17/example.txt";

  private static final Map<Direction, EnumSet<Direction>> TURNS = Arrays.stream(Direction.values())
      .collect(Collectors.toMap(d -> d, d -> EnumSet.complementOf(EnumSet.of(d, d.opposite()))));

  private record Node(Point point, int heat, Direction direction, int line) implements Comparable<Node> {
    Visited toVisited() {
      return new Visited(point, direction, line);
    }

    @Override
    public int compareTo(Node other) {
      return Integer.compare(heat, other.heat);
    }
  }

  private record Visited(Point p, Direction d, int line) {
  }

  static int findMinimumHeatLoss(Matrix<Integer> matrix, int minLine, int maxLine) {
    Point start = Point.of(0, 0);
    Point end = Point.of(matrix.width() - 1, matrix.height() - 1);

    PriorityQueue<Node> queue = new PriorityQueue<>();
    queue.add(new Node(start, 0, Direction.RIGHT, 0));
    Map<Visited, Integer> visited = new HashMap<>();
    while (!queue.isEmpty()) {
      Node node = queue.remove();
      if (node.point().equals(end) && node.line() >= minLine) {
        return node.heat();
      }
      if (node.line() < maxLine) {
        Point p = node.direction().apply(node.point());
        if (matrix.contains(p)) {
          Node next = new Node(p, node.heat() + matrix.get(p), node.direction(), node.line() + 1);
          Visited v = next.toVisited();
          Integer prev = visited.get(v);
          if (prev == null || next.heat() < prev) {
            visited.put(v, next.heat());
            queue.add(next);
          }
        }
      }
      if (node.line() < minLine) {
        continue;
      }
      TURNS.get(node.direction()).stream()
          .forEach(d -> {
            Point p = d.apply(node.point());
            if (matrix.contains(p)) {
              Node next = new Node(p, node.heat() + matrix.get(p), d, 1);
              Visited v = next.toVisited();
              Integer prev = visited.get(v);
              if (prev == null || next.heat() < prev) {
                visited.put(v, next.heat());
                queue.add(next);
              }
            }
          });
    }
    return -1;
  }

  public static void main(String[] args) throws Exception {
    Matrix<Integer> matrix = new Matrix(InputHelper.linesFromResource(INPUT)
        .map(line -> line.chars().mapToObj(c -> c - '0').toList())
        .toList());

    // Part 1.
    System.out.println("Part 1: " + findMinimumHeatLoss(matrix, 0, 3));
    System.out.println("Part 2: " + findMinimumHeatLoss(matrix, 4, 10));
  }
}
