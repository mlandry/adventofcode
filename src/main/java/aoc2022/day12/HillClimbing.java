package aoc2022.day12;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import aoccommon.InputHelper;
import aoccommon.Point;

/** Solution for {@link https://adventofcode.com/2022/day/12}. */
public class HillClimbing {

  private static final String INPUT = "aoc2022/day12/input.txt";

  public static void main(String[] args) throws Exception {
    List<String> input = InputHelper.linesFromResource(INPUT).collect(Collectors.toList());
    Point start = null, end = null;
    int maxX = -1;
    List<Point> allPossibleStarts = new ArrayList<>();

    for (int y = 0; y < input.size(); y++) {
      for (int x = 0; x < input.get(y).length(); x++) {
        char c = input.get(y).charAt(x);
        if (c == 'S') {
          start = Point.of(x, y);
          allPossibleStarts.add(start);
        } else if (c == 'E') {
          end = Point.of(x, y);
        } else if (c == 'a') {
          allPossibleStarts.add(Point.of(x, y));
        }
      }
      maxX = input.get(y).length() - 1; // Assume all rows same length.
    }
    Point maxPoint = Point.of(maxX, input.size() - 1);

    Climber climber = new Climber(input, start, end, maxPoint);
    System.out.println("Part 1: " + climber.traverse());

    int shortestPath = Integer.MAX_VALUE;
    for (Point possibleStart : allPossibleStarts) {
      climber = new Climber(input, possibleStart, end, maxPoint);
      shortestPath = Math.min(climber.traverse(), shortestPath);
    }
    System.out.println("Part 2: " + shortestPath);
  }

  private static class Climber {
    private final List<String> input;
    private final Point start;
    private final Point end;
    private final Point maxPoint;

    private final Map<Point, Integer> visited = new HashMap<>();
    private final PriorityQueue<Step> queue = new PriorityQueue<>();

    private Climber(List<String> input, Point start, Point end, Point maxPoint) {
      this.input = input;
      this.start = start;
      this.end = end;
      this.maxPoint = maxPoint;
    }

    int traverse() {
      queue.clear();
      visited.clear();

      queue.offer(new Step(start, 0));
      visited.put(start, 0);

      while (!queue.isEmpty()) {
        final Step step = queue.poll();
        if (step.point.equals(end)) {
          break;
        }
        int elevation = getElevation(step.point);
        getAdjacent(step.point).forEach(next -> {
          int nextElevation = getElevation(next);
          if (nextElevation - elevation > 1) {
            return;
          }
          int nextSteps = step.steps + 1;
          if (nextSteps < visited.getOrDefault(next, Integer.MAX_VALUE)) {
            queue.offer(new Step(next, nextSteps));
            visited.put(next, nextSteps);
          }
        });
      }
      return visited.getOrDefault(end, Integer.MAX_VALUE);
    }

    private Stream<Point> getAdjacent(Point point) {
      return Arrays.stream(Direction.values())
          .map(direction -> getAdjacent(point, direction))
          .filter(Optional::isPresent)
          .map(Optional::get);
    }

    private Optional<Point> getAdjacent(Point point, Direction direction) {
      int newX = point.getX() + direction.xd;
      int newY = point.getY() + direction.yd;
      if (newX < 0 || newX > maxPoint.getX()) {
        return Optional.empty();
      }
      if (newY < 0 || newY > maxPoint.getY()) {
        return Optional.empty();
      }
      return Optional.of(Point.of(newX, newY));
    }

    private int getElevation(Point point) {
      char c = input.get(point.getY()).charAt(point.getX());
      if (c == 'S') {
        c = 'a';
      } else if (c == 'E') {
        c = 'z';
      }
      return c - 'a';
    }
  }

  private static record Step(Point point, int steps) implements Comparable<Step> {
    @Override
    public int compareTo(Step other) {
      return Integer.compare(steps, other.steps);
    }
  }

  private static enum Direction {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    private final int xd;
    private final int yd;

    private Direction(int xd, int yd) {
      this.xd = xd;
      this.yd = yd;
    }
  }
}
