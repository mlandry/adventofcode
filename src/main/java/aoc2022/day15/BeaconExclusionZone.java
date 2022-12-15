package aoc2022.day15;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import aoccommon.InputHelper;
import aoccommon.Point;

/** Solution for {@link https://adventofcode.com/2022/day/15}. */
public class BeaconExclusionZone {

  private static final String INPUT = "aoc2022/day15/input.txt";

  private static final Pattern REGEX = Pattern
      .compile("^Sensor.*x=(\\-?\\d+),\\ y=(\\-?\\d+).*x=(\\-?\\d+),\\ y=(\\-?\\d+)$");

  public static void main(String[] args) throws Exception {
    Map<Point, Point> closestBeacons = InputHelper.linesFromResource(INPUT)
        .map(REGEX::matcher)
        .filter(Matcher::matches)
        .collect(Collectors.toMap(
            m -> Point.of(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))),
            m -> Point.of(Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)))));
    System.out.println(closestBeacons);
  }
}
