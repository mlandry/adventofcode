package tangents;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import aoccommon.Point;

public final class Tetris {

  private static final int WIDTH = 7;
  private static final int HEIGHT = 15;

  private static record Shape(Set<Point> relativePoints) {
    Set<Point> getActualPoints(Point origin) {
      return relativePoints.stream().map(p -> Point.of(origin.getX() + p.getX(), origin.getY() + p.getY()))
          .collect(Collectors.toSet());
    }

    int rightEdge(Point origin) {
      return relativePoints.stream().mapToInt(Point::getX).map(x -> origin.getX() + x).max().orElse(0);
    }
  }

  private static final List<Shape> SHAPES = List.of(
      /**
       * ####
       */
      new Shape(Set.of(
          Point.of(0, 0), Point.of(1, 0), Point.of(2, 0), Point.of(3, 0))),
      /**
       * ##
       * ##
       */
      new Shape(Set.of(
          Point.of(0, -1), Point.of(1, -1),
          Point.of(0, 0), Point.of(1, 0))),
      /**
       * #.
       * #.
       * ##
       */
      new Shape(Set.of(
          Point.of(0, -2),
          Point.of(0, -1),
          Point.of(0, 0), Point.of(1, 0))),
      /**
       * #.
       * ##
       * .#
       */
      new Shape(Set.of(
          Point.of(0, -2),
          Point.of(0, -1), Point.of(1, -1),
          Point.of(1, 0))),
      /**
       * ###
       * .#.
       */
      new Shape(Set.of(
          Point.of(0, -1), Point.of(1, -1), Point.of(2, -1),
          Point.of(1, 0))));

  private static class Chamber {
    private final Set<Point> filled = new HashSet<>();

    private Shape activeShape;
    private Point position;
  }
}
