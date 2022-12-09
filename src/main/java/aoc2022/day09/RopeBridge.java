package aoc2022.day09;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import aoccommon.InputHelper;
import aoccommon.Point;

/** Solution for {@link https://adventofcode.com/2022/day/9}. */
public class RopeBridge {

  private static final String INPUT = "aoc2022/day09/input.txt";

  public static void main(String[] args) throws Exception {
    List<Motion> motions = InputHelper.linesFromResource(INPUT)
        .map(Motion::parse)
        .collect(Collectors.toList());

     System.out.println("Part 1: " + moveRope(motions, 2).size());
     System.out.println("Part 1: " + moveRope(motions, 10).size());
  }

  private static Set<Point> moveRope(List<Motion> motions, int numKnots) {
    Set<Point> visited = new HashSet<>();
    Point[] knots = new Point[numKnots];
    for (int k = 0; k < numKnots; k++) {
      knots[k] = Point.of(0, 0);
    }
    visited.add(knots[0]);

    for (Motion motion : motions) {
      for (int i = 0; i < motion.count(); i++) {
        knots[0] = move(knots[0], motion.dir());
        for (int k = 1; k < numKnots; k++) {
          int xd = knots[k - 1].getX() - knots[k].getX();
          int yd = knots[k - 1].getY() - knots[k].getY();
          if (Math.abs(xd) <= 1 && Math.abs(yd) <= 1) {
            // Head and Tail touching.
            continue;
          }
          // Move in the direciton of Head.
          int xmove = xd == 0 ? 0 : xd / Math.abs(xd);
          int ymove = yd == 0 ? 0 : yd / Math.abs(yd);
          knots[k] = Point.of(knots[k].getX() + xmove, knots[k].getY() + ymove);
        }
        visited.add(knots[numKnots - 1]);
      }
    }
    return visited;
  }

  private static enum Direction {
    U(0, -1),
    D(0, 1),
    L(-1, 0),
    R(1, 0);

    private final int xd;
    private final int yd;

    private Direction(int xd, int yd) {
      this.xd = xd;
      this.yd = yd;
    }
  }

  private static record Motion(Direction dir, int count) {
    private static Motion parse(String line) {
      String[] split = line.split(" ");
      return new Motion(Direction.valueOf(split[0]), Integer.parseInt(split[1]));
    }
  }

  private static Point move(Point point, Direction dir) {
    return Point.of(point.getX() + dir.xd, point.getY() + dir.yd);
  }
}
