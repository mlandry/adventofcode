package aoc2021.day19;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import aoccommon.InputHelper;
import aoccommon.Point;
import aoccommon.Stats;

/** Solution for {@link https://adventofcode.com/2021/day/19}. */
public class BeaconScanner {

  private static final String INPUT = "aoc2021/day19/input.txt";
  private static final Pattern SCANNER = Pattern.compile("^--- scanner (\\d+) ---$");

  // Rotations to determine which direction is facing forward (x).
  private static final List<Function<Point, Point>> FACE_ROTATIONS = List.of(
      Function.identity(),
      p -> Point.of(p.y(), -p.x(), p.z()),
      p -> Point.of(-p.x(), -p.y(), p.z()),
      p -> Point.of(-p.y(), p.x(), p.z()),
      p -> Point.of(p.z(), p.y(), -p.x()),
      p -> Point.of(-p.z(), p.y(), p.x()));

  private static final List<Function<Point, Point>> YZ_ROTATIONS = List.of(
      Function.identity(),
      p -> Point.of(p.x(), -p.z(), p.y()),
      p -> Point.of(p.x(), -p.y(), -p.z()),
      p -> Point.of(p.x(), p.z(), -p.y()));

  private static final List<Function<Point, Point>> ROTATIONS = FACE_ROTATIONS.stream()
      .flatMap(f -> YZ_ROTATIONS.stream().map(xy -> chain(f, xy)))
      .collect(Collectors.toList());

  private static Function<Point, Point> chain(Function<Point, Point> func1, Function<Point, Point> func2) {
    return p -> func2.apply(func1.apply(p));
  }

  private static Point subtract(Point absolute, Point relative) {
    return absolute.merge(relative, (a, b) -> a - b);
  }

  private static Point add(Point absolute, Point relative) {
    return absolute.merge(relative, (a, b) -> a + b);
  }

  private static long manhattanDistance(Point p1, Point p2) {
    return p1.merge(p2, (a, b) -> Math.abs(a - b)).stream().sum();
  }

  public static void main(String[] args) throws Exception {
    // Debug.enablePrint();
    // Stats.enablePrintOnExit();
    Stats.startTimer("main");

    List<String> lines = InputHelper.linesFromResource(INPUT).collect(Collectors.toList());
    Set<Point> current = null;

    // Map of scanner ID -> relative beacon points.
    Map<Integer, Set<Point>> scanners = new HashMap<>();

    for (String line : lines) {
      Matcher m = SCANNER.matcher(line);
      if (m.matches()) {
        current = new HashSet<>();
        scanners.put(Integer.parseInt(m.group(1)), current);
      } else if (line.isBlank()) {
        continue;
      } else {
        current.add(Point.parse(line));
      }
    }

    Set<Point> beacons = new HashSet<>();
    Map<Integer, Point> resolvedScanners = new HashMap<>();
    resolvedScanners.put(0, Point.of(0, 0, 0));
    beacons.addAll(scanners.get(0));

    while (resolvedScanners.size() < scanners.size()) {
      outer: for (int candidateScanner : scanners.keySet()) {
        if (resolvedScanners.containsKey(candidateScanner)) {
          continue;
        }
        for (Function<Point, Point> rotation : ROTATIONS) {
          for (Point seedBeacon : scanners.get(candidateScanner)) {
            Point rotatedSeedBeacon = rotation.apply(seedBeacon);
            for (Point potentialMatch : beacons) {
              Stats.incrementCounter("beaconPairsCompared");
              Point candidatePosition = subtract(potentialMatch, rotatedSeedBeacon);
              Set<Point> candidateBeacons = scanners.get(candidateScanner).stream()
                  .map(rotation::apply)
                  .map(b -> add(candidatePosition, b))
                  .collect(Collectors.toSet());
              long matches = candidateBeacons.stream()
                  .filter(beacons::contains)
                  .count();
              Stats.incrementHistogramValue("matches", matches);
              if (matches >= 12) {
                beacons.addAll(candidateBeacons);
                resolvedScanners.put(candidateScanner, candidatePosition);
                break outer;
              }
            }
          }
        }
      }
    }
    System.out.println("Part 1: " + beacons.size());

    long longestDistanceBetweenScanners = resolvedScanners.values().stream()
        .flatMapToLong(p1 -> resolvedScanners.values().stream().mapToLong(p2 -> manhattanDistance(p1, p2)))
        .max()
        .orElse(0);

    System.out.println("Part 2: " + longestDistanceBetweenScanners);

    Stats.endTimer("main");
  }
}
