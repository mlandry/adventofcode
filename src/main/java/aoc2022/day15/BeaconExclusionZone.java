package aoc2022.day15;

import java.util.HashSet;
import java.util.Map;
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

    System.out.println("Part 1: " + coveredPointsInRow(sensorBeacons, TARGET_ROW).size());

    Point beacon = findBeacon(sensorBeacons, MAX_XY);
    System.out.println(beacon);
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

  private static Point findBeacon(Map<Point, Point> sensorBeacons, int maxXY) {
    for (int x = 0; x <= maxXY; x++) {
      System.out.println(x);
      pointLoop: for (int y = 0; y <= maxXY; y++) {
        Point point = Point.of(x, y);
        for (Map.Entry<Point, Point> sensor : sensorBeacons.entrySet()) {
          if (manhattanDistance(sensor.getKey(), point) <= manhattanDistance(sensor.getKey(), sensor.getValue())) {
            continue pointLoop;
          }
        }
        // Point is outside coverage of all sensors.
        return point;
      }
    }
    return null;
  }
}
