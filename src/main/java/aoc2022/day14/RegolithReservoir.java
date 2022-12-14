package aoc2022.day14;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import aoccommon.InputHelper;
import aoccommon.Point;

/** Solution for {@link https://adventofcode.com/2022/day/14}. */
public class RegolithReservoir {

  private static final String INPUT = "aoc2022/day14/input.txt";
  private static final boolean DEBUG = false;

  public static void main(String[] args) throws Exception {
    List<RockStructure> rockStructures = InputHelper.linesFromResource(INPUT).map(RockStructure::parse)
        .collect(Collectors.toList());

    Map map = Map.from(rockStructures);
    System.out.println("Part 1: " + map.fillWithSand().size());

    map = Map.withFloor(rockStructures);
    System.out.println("Part 2: " + map.fillWithSand().size());
  }

  private static class Map {
    private static final Point ORIGIN = Point.of(500, 0);

    private final Set<Point> rockPoints;
    private final int maxY;
    private final boolean withFloor;

    private Map(Set<Point> rockPoints, int maxY, boolean withFloor) {
      this.rockPoints = rockPoints;
      this.maxY = maxY;
      this.withFloor = withFloor;
    }

    static Map from(List<RockStructure> rockStructures) {
      Set<Point> rockPoints = rockStructures.stream().flatMap(RockStructure::allPoints).collect(Collectors.toSet());
      int maxY = rockStructures.stream().map(RockStructure::vertices).flatMap(List::stream).mapToInt(Point::getY).max()
          .getAsInt();
      return new Map(rockPoints, maxY, /* withFloor= */ false);
    }

    static Map withFloor(List<RockStructure> rockStructures) {
      Set<Point> rockPoints = rockStructures.stream().flatMap(RockStructure::allPoints).collect(Collectors.toSet());
      int maxY = rockStructures.stream().map(RockStructure::vertices).flatMap(List::stream).mapToInt(Point::getY).max()
          .getAsInt();
      return new Map(rockPoints, maxY, /* withFloor= */ true);
    }

    private Set<Point> fillWithSand() throws Exception {
      Set<Point> sandAtRest = new HashSet<>();
      Point sand = ORIGIN;
      Set<Point> flowPath = new HashSet<>();
      while (true) {
        Optional<Point> next = next(sandAtRest, sand);
        if (next.isPresent()) {
          sand = next.get();
          flowPath.add(sand);
          if (sand.getY() >= maxY && !withFloor) {
            // Sand flowing into the abyss.
            printAndWait(sandAtRest, flowPath);
            break;
          }
          continue;
        }
        if (sand.equals(ORIGIN)) {
          // Source is blocked!
          sandAtRest.add(sand);
          break;
        }
        // No move possible, sand comes to rest here.
        sandAtRest.add(sand);
        flowPath.clear();
        // New sand unit produced at origin.
        sand = ORIGIN;
        printAndWait(sandAtRest, flowPath);
      }
      return sandAtRest;
    }

    private Optional<Point> next(Set<Point> sandAtRest, Point sand) {
      for (SandMove move : SandMove.values()) {
        Point next = Point.of(sand.getX() + move.xd, sand.getY() + move.yd);
        boolean hitFloor = withFloor && (next.getY() >= maxY + 2);
        if (!rockPoints.contains(next) && !sandAtRest.contains(next) && !hitFloor) {
          return Optional.of(next);
        }
      }
      return Optional.empty();
    }

    private void printAndWait(Set<Point> sandAtRest, Set<Point> flowPath)
        throws Exception {
      if (!DEBUG) {
        return;
      }
      // System.out.print("\033[H\033[2J");
      // System.out.flush();
      int minX = Stream.concat(flowPath.stream(), Stream.concat(rockPoints.stream(), sandAtRest.stream()))
          .mapToInt(Point::getX)
          .min()
          .getAsInt();
      int maxX = Stream.concat(flowPath.stream(), Stream.concat(rockPoints.stream(), sandAtRest.stream()))
          .mapToInt(Point::getX)
          .max()
          .getAsInt();
      for (int y = 0; y <= maxY; y++) {
        System.out.print(String.format("%02d ", y));
        for (int x = minX; x <= maxX; x++) {
          Point point = Point.of(x, y);
          if (rockPoints.contains(point)) {
            System.out.print('#');
          } else if (sandAtRest.contains(point)) {
            System.out.print('o');
          } else if (ORIGIN.equals(point)) {
            System.out.print('+');
          } else if (flowPath.contains(point)) {
            System.out.print('~');
          } else {
            System.out.print('.');
          }
        }
        System.out.println();
      }
      // Thread.sleep(1000);
      // System.in.read();
    }
  }

  private static enum SandMove {
    DOWN(0, 1),
    DOWN_LEFT(-1, 1),
    DOWN_RIGHT(1, 1);

    private final int xd;
    private final int yd;

    private SandMove(int xd, int yd) {
      this.xd = xd;
      this.yd = yd;
    }
  }

  private static record RockStructure(List<Point> vertices) {
    static RockStructure parse(String line) {
      return new RockStructure(Arrays.stream(line.split(" -> ")).map(Point::parse).collect(Collectors.toList()));
    }

    Stream<Point> allPoints() {
      return IntStream.range(1, vertices.size())
          .mapToObj(i -> pointsBetween(vertices.get(i - 1), vertices.get(i)))
          .flatMap(s -> s);
    }

    @Override
    public String toString() {
      return vertices.stream().map(Point::toString).collect(Collectors.joining(" -> "));
    }
  }

  static Stream<Point> pointsBetween(Point a, Point b) {
    return IntStream.rangeClosed(Math.min(a.getX(), b.getX()), Math.max(a.getX(), b.getX()))
        .mapToObj(x -> IntStream.rangeClosed(Math.min(a.getY(), b.getY()), Math.max(a.getY(), b.getY()))
            .mapToObj(y -> Point.of(x, y)))
        .flatMap(s -> s);

  }
}
