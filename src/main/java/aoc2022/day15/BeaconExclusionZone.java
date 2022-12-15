package aoc2022.day15;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import aoccommon.InputHelper;
import aoccommon.Point;

/** Solution for {@link https://adventofcode.com/2022/day/15}. */
public class BeaconExclusionZone {

  private static final String INPUT = "aoc2022/day15/input.txt";
  private static final int TARGET_ROW = 2000000; // 10;
  private static final int MAX_XY = 4000000; // 20;

  private static final Pattern REGEX = Pattern
      .compile("^Sensor.*x=(\\-?\\d+),\\ y=(\\-?\\d+).*x=(\\-?\\d+),\\ y=(\\-?\\d+)$");

  public static void main(String[] args) throws Exception {
    Map<Point, Point> sensorBeacons = InputHelper.linesFromResource(INPUT)
        .map(REGEX::matcher)
        .filter(Matcher::matches)
        .collect(Collectors.toMap(
            m -> Point.of(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))),
            m -> Point.of(Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)))));

    System.out.println("Part 1: "
        + (coveredRangesInRow(sensorBeacons, TARGET_ROW, Integer.MIN_VALUE, Integer.MAX_VALUE).disjointSize()
            - beaconsInRow(sensorBeacons, TARGET_ROW)));

    Point beacon = findBeacon(sensorBeacons, MAX_XY);
    long frequency = (4000000L * (long) beacon.getX()) + beacon.getY();
    System.out.println("Part 2: " + frequency);
  }

  private static Point findBeacon(Map<Point, Point> sensorBeacons, int maxXY) {
    for (int y = 0; y <= maxXY; y++) {
      RangeSet coveredX = coveredRangesInRow(sensorBeacons, y, 0, maxXY);
      if (coveredX.disjointSize() < (maxXY + 1)) {
        for (int x = 0; x <= maxXY; x++) {
          if (!coveredX.contains(x)) {
            return Point.of(x, y);
          }
        }
      }
    }
    return null;
  }

  private static int manhattanDistance(Point a, Point b) {
    return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
  }

  private static int beaconsInRow(Map<Point, Point> sensorBeacons, int row) {
    return (int) sensorBeacons.values().stream().map(Point::getY).filter(y -> y == row).collect(Collectors.toSet())
        .size();
  }

  private static RangeSet coveredRangesInRow(Map<Point, Point> sensorBeacons, int row, int min, int max) {
    final RangeSet rangeSet = RangeSet.create();
    sensorBeacons.entrySet().stream()
        .forEach(entry -> coveredRangeInRow(entry.getKey(), entry.getValue(), row, min, max).ifPresent(rangeSet::add));
    return rangeSet;
  }

  private static Optional<Range> coveredRangeInRow(Point sensor, Point beacon, int row, int min, int max) {
    int dist = manhattanDistance(sensor, beacon);
    int ydelta = Math.abs(sensor.getY() - row);
    if (ydelta > dist) {
      return Optional.empty();
    }
    int xdelta = dist - ydelta;
    return Optional.of(new Range(Math.max(min, sensor.getX() - xdelta), Math.min(max, sensor.getX() + xdelta)));
  }

  private static record Range(int lower, int upper) {
    boolean contains(int value) {
      return value >= lower && value <= upper;
    }

    int size() {
      return upper - lower + 1;
    }
  }

  private static record RangeSet(Set<Range> ranges) {
    static RangeSet create() {
      return new RangeSet(new HashSet<>());
    }

    boolean contains(int value) {
      return ranges.stream().anyMatch(r -> r.contains(value));
    }

    int disjointSize() {
      return ranges.stream().mapToInt(Range::size).sum();
    }

    void add(Range newRange) {
      Optional<Range> containsLower = Optional.empty();
      Optional<Range> containsUpper = Optional.empty();
      Optional<Range> toAdd = Optional.empty();
      for (Range range : ranges) {
        if (range.contains(newRange.lower())) {
          containsLower = Optional.of(range);
        }
        if (range.contains(newRange.upper())) {
          containsUpper = Optional.of(range);
        }
      }
      if (containsLower.isEmpty() && containsUpper.isEmpty()) {
        toAdd = Optional.of(newRange);
      } else if (containsLower.isPresent()) {
        Range lower = containsLower.get();
        if (containsUpper.isPresent()) {
          Range upper = containsUpper.get();
          if (lower.equals(upper)) {
            // Range contained by existing range.
            return;
          } else {
            // Merge the lower and upper ranges.
            ranges.remove(lower);
            ranges.remove(upper);
            toAdd = Optional.of(new Range(lower.lower(), upper.upper()));
          }
        } else {
          // Merge the new range with the lower range.
          ranges.remove(lower);
          toAdd = Optional.of(new Range(lower.lower(), newRange.upper()));
        }
      } else {
        Range upper = containsUpper.get();
        // Merge the new range with the upper range.
        ranges.remove(upper);
        toAdd = Optional.of(new Range(newRange.lower(), upper.upper()));
      }
      if (toAdd.isEmpty()) {
        return;
      }

      // Check for existing ranges contained in the new range.
      Iterator<Range> iter = ranges.iterator();
      while (iter.hasNext()) {
        Range range = iter.next();
        if (toAdd.get().contains(range.lower()) && toAdd.get().contains(range.upper())) {
          iter.remove();
        }
      }
      ranges.add(toAdd.get());
    }
  }
}
