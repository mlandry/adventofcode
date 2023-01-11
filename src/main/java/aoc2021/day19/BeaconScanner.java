package aoc2021.day19;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import aoccommon.InputHelper;
import aoccommon.Point;

/** Solution for {@link https://adventofcode.com/2021/day/19}. */
public class BeaconScanner {

  private static final String INPUT = "aoc2021/day19/input.txt";
  private static final Pattern SCANNER = Pattern.compile("^--- scanner (\\d+) ---$");

  // Rotations to determine which direction is facing forward.
  private static final List<Function<Point, Point>> FACE_ROTATIONS = List.of(
      Function.identity(), // x
      p -> Point.of(-p.getX(), p.getY(), p.getZ()), // -x
      p -> Point.of(p.getY(), -p.getX(), p.getZ()), // y
      p -> Point.of(-p.getY(), p.getX(), p.getZ()), // -y
      p -> Point.of(p.getZ(), p.getY(), -p.getX()), // z
      p -> Point.of(-p.getZ(), p.getY(), p.getX()) // -z
  );

  private static final Function<Point, Point> UP_ROTATION = p -> Point.of(p.getX(), -p.getZ(), p.getY());

  private static final List<Function<Point, Point>> ROTATIONS = FACE_ROTATIONS.stream()
      .flatMap(face -> IntStream.rangeClosed(0, 3)
          .mapToObj(num -> chain(face, repeat(UP_ROTATION, num))))
      .collect(Collectors.toList());

  private static Function<Point, Point> chain(Function<Point, Point> func1, Function<Point, Point> func2) {
    return p -> func2.apply(func1.apply(p));
  }

  private static Function<Point, Point> repeat(Function<Point, Point> func, int times) {
    return p -> {
      for (int i = 0; i < times; i++) {
        p = func.apply(p);
      }
      return p;
    };
  }
  

  public static void main(String[] args) throws Exception {
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

    // Coordinates for resolved scanners, relative to scanner 0.
    Map<Integer, Point> scannerCoordinates = new HashMap<>();
    scannerCoordinates.put(0, Point.of(0, 0, 0));
    // Rotations for resolved scanners, relative to scanner 0.
    Map<Integer, Function<Point, Point>> scannerRotations = new HashMap<>();
    scannerRotations.put(0, Function.identity());
  }
}
