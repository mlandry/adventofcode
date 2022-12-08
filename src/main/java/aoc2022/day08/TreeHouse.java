package aoc2022.day08;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import aoccommon.InputHelper;
import aoccommon.Point;

/** Solution for {@link https://adventofcode.com/2022/day/8}. */
public class TreeHouse {

  private static final String INPUT = "aoc2022/day08/input.txt";

  public static void main(String[] args) throws Exception {
    List<String> lines = InputHelper.linesFromResource(INPUT).collect(Collectors.toList());

    Map<Point, Integer> trees = new HashMap<>();
    int maxX = 0;

    for (int y = 0; y < lines.size(); y++) {
      String line = lines.get(y);
      maxX = Math.max(maxX, line.length() - 1);
      for (int x = 0; x < line.length(); x++) {
        trees.put(Point.of(x, y), Character.getNumericValue(line.charAt(x)));
      }
    }

    VisibleTreeCounter counter = new VisibleTreeCounter(trees, Point.of(maxX, lines.size() - 1));
    System.out.println("Part 1: " + counter.countVisibleTrees());

    System.out.println("Part 2: " + counter.getMaxScenicScore());
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

  private static class VisibleTreeCounter {
    private final Map<Point, Integer> trees;
    private final Point maxPoint;

    // Max height between point and edge in direction.
    private final Map<Point, Map<Direction, Integer>> maxHeightCache = new HashMap<>();

    private VisibleTreeCounter(Map<Point, Integer> trees, Point maxPoint) {
      this.trees = trees;
      this.maxPoint = maxPoint;
    }

    private long countVisibleTrees() {
      return trees.keySet().stream().filter(this::isVisible).count();
    }

    // Part 1 entry point.
    private boolean isVisible(Point point) {
      if (isEdge(point)) {
        return true;
      }
      return Arrays.stream(Direction.values())
          .anyMatch(direction -> trees.get(point) > getMaxHeight(point, direction));
    }

    private int getMaxHeight(Point point, Direction direction) {
      Map<Direction, Integer> value = maxHeightCache.computeIfAbsent(point, p -> new HashMap<>());
      if (value.containsKey(direction)) {
        return value.get(direction);
      }

      Point adjacent = getAdjacent(point, direction);
      int result = 0;
      if (isEdge(adjacent)) {
        result = trees.get(adjacent);
      } else {
        result = Math.max(trees.get(adjacent), getMaxHeight(adjacent, direction));
      }
      value.put(direction, result);
      return result;
    }

    // Part 2 entry point.
    private long getMaxScenicScore() {
      return trees.keySet().stream()
          .mapToLong(this::getScenicScore)
          .max()
          .getAsLong();
    }

    private long getScenicScore(Point point) {
      // Brute-force?
      return Arrays.stream(Direction.values())
          .mapToLong(direction -> {
            Point adjacent = getAdjacent(point, direction);
            long count = 0;
            while (adjacent != null) {
              count++;
              if (trees.get(adjacent) < trees.get(point)) {
                adjacent = getAdjacent(adjacent, direction);
              } else {
                break;
              }
            }
            return count;
          })
          .reduce((a, b) -> a * b)
          .getAsLong();
    }

    private boolean isEdge(Point point) {
      return point.getX() == 0
          || point.getY() == 0
          || point.getX() == maxPoint.getX()
          || point.getY() == maxPoint.getY();
    }

    private Point getAdjacent(Point point, Direction direction) {
      int newX = point.getX() + direction.xd;
      int newY = point.getY() + direction.yd;
      if (newX < 0 || newX > maxPoint.getX()) {
        return null;
      }
      if (newY < 0 || newY > maxPoint.getY()) {
        return null;
      }
      return Point.of(newX, newY);
    }
  }
}
