package aoc2022.day15;

import java.util.HashSet;
import java.util.List;
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

    // System.out.println("Part 1: " + coveredPointsInRow(sensorBeacons,
    // TARGET_ROW).size());
    System.out.println("Part 1: "
        + (coveredRangesInRow(sensorBeacons, TARGET_ROW).disjointSize() - beaconsInRow(sensorBeacons, TARGET_ROW)));
  }

  private static int manhattanDistance(Point a, Point b) {
    return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
  }

  private static Set<Point> coveredPointsInRow(Map<Point, Point> sensorBeacons, int row) {
    Set<Point> covered = new HashSet<>();
    for (Map.Entry<Point, Point> sensor : sensorBeacons.entrySet()) {
      int dist = manhattanDistance(sensor.getKey(), sensor.getValue());
      int ydelta = Math.abs(sensor.getKey().getY() - row);
      // Fan out and add points in both directions.
      for (int xdelta = 0; xdelta <= dist - ydelta; xdelta++) {
        covered.add(Point.of(sensor.getKey().getX() + xdelta, row));
        covered.add(Point.of(sensor.getKey().getX() - xdelta, row));
      }
      // Don't count the actual beacon.
      covered.remove(sensor.getValue());
    }
    return covered;
  }

  private static RangeSet coveredRangesInRow(Map<Point, Point> sensorBeacons, int row) {
    final RangeSet rangeSet = RangeSet.create();
    sensorBeacons.entrySet().stream()
        .forEach(entry -> coveredRangeInRow(entry.getKey(), entry.getValue(), row).ifPresent(rangeSet::add));
    return rangeSet;
  }

  private static int beaconsInRow(Map<Point, Point> sensorBeacons, int row) {
    return (int) sensorBeacons.values().stream().map(Point::getY).filter(y -> y == row).collect(Collectors.toSet())
        .size();
  }

  private static Optional<Range> coveredRangeInRow(Point sensor, Point beacon, int row) {
    int dist = manhattanDistance(sensor, beacon);
    int ydelta = Math.abs(sensor.getY() - row);
    if (ydelta > dist) {
      return Optional.empty();
    }
    int xdelta = dist - ydelta;
    return Optional.of(new Range(sensor.getX() - xdelta, sensor.getX() + xdelta));
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

    int disjointSize() {
      return ranges.stream().mapToInt(Range::size).sum();
    }

    void add(Range toAdd) {
      Optional<Range> containsLower = Optional.empty();
      Optional<Range> containsUpper = Optional.empty();
      for (Range range : ranges) {
        if (range.contains(toAdd.lower())) {
          containsLower = Optional.of(range);
        }
        if (range.contains(toAdd.upper())) {
          containsUpper = Optional.of(range);
        }
      }
      if (containsLower.isEmpty() && containsUpper.isEmpty()) {
        ranges.add(toAdd);
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
            ranges.add(new Range(lower.lower(), upper.upper()));
          }
        } else {
          // Merge the new range with the lower range.
          ranges.remove(lower);
          ranges.add(new Range(lower.lower(), toAdd.upper()));
        }
      } else {
        Range upper = containsUpper.get();
        // Merge the new range with the upper range.
        ranges.remove(upper);
        ranges.add(new Range(toAdd.lower(), upper.upper()));
      }
    }
  }
}
