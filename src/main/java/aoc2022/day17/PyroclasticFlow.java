package aoc2022.day17;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import aoccommon.InputHelper;
import aoccommon.Point;

/** Solution for {@link https://adventofcode.com/2022/day/17}. */
public class PyroclasticFlow {

  private static record Rock(Set<Point> relativePoints) {
    Set<Point> getActualPoints(Point origin) {
      return relativePoints.stream().map(p -> Point.of(origin.getX() + p.getX(), origin.getY() + p.getY()))
          .collect(Collectors.toSet());
    }

    int rightEdge(Point origin) {
      return relativePoints.stream().mapToInt(Point::getX).map(x -> origin.getX() + x).max().orElse(0);
    }
  }

  private static final String INPUT = "aoc2022/day17/input.txt";
  private static final boolean DEBUG = false;

  // Rock shapes are points relative to bottom left corner.
  private static final List<Rock> SHAPES = List.of(
      new Rock(Set.of(Point.of(0, 0), Point.of(1, 0), Point.of(2, 0), Point.of(3, 0))),
      new Rock(Set.of(Point.of(0, -1), Point.of(1, -1), Point.of(2, -1), Point.of(1, -2), Point.of(1, 0))),
      new Rock(Set.of(Point.of(0, 0), Point.of(1, 0), Point.of(2, 0), Point.of(2, -1), Point.of(2, -2))),
      new Rock(Set.of(Point.of(0, 0), Point.of(0, -1), Point.of(0, -2), Point.of(0, -3))),
      new Rock(Set.of(Point.of(0, 0), Point.of(1, 0), Point.of(0, -1), Point.of(1, -1))));

  private static final int WIDTH = 7;
  private static final int LEFT_OFFSET = 2;
  private static final int BOTTOM_OFFSET = 3;

  private static class RockChamber {
    private final List<Character> sequence;
    private final Set<Point> filled = new HashSet<>();

    private RockChamber(List<Character> sequence) {
      this.sequence = Collections.unmodifiableList(sequence);
    }

    static RockChamber initialize(List<Character> sequence) {
      RockChamber chamber = new RockChamber(sequence);
      IntStream.range(0, WIDTH).mapToObj(x -> Point.of(x, 0)).forEach(chamber.filled::add);
      return chamber;
    }

    int getHeight() {
      return 0 - filled.stream().mapToInt(Point::getY).min().orElse(0);
    }

    void clear() {
      filled.clear();
    }

    void dropRocks(long numRocks) {
      int seqIndex = 0;

      int iterations = (int) Math.min(numRocks, (long) Integer.MAX_VALUE);
      for (int i = 0; i < iterations; i++) {
        debug("height=%d after %d rocks", getHeight(), i);
        Rock shape = SHAPES.get(i % SHAPES.size());
        int highestRock = filled.stream().mapToInt(Point::getY).min().orElse(0);

        Point position = Point.of(LEFT_OFFSET, highestRock - BOTTOM_OFFSET - 1);
        print(shape, position);
        while (true) {
          char move = sequence.get(seqIndex++ % sequence.size());
          debug("move %s", move);
          Point next = Point.of(position.getX() + (move == '>' ? 1 : -1), position.getY());
          if (next.getX() >= 0 && shape.rightEdge(next) < WIDTH
              && Collections.disjoint(shape.getActualPoints(next), filled)) {
            position = next;
          }
          print(shape, position);

          next = Point.of(position.getX(), position.getY() + 1);
          boolean atRest = shape.getActualPoints(next).stream().anyMatch(filled::contains);
          if (atRest) {
            shape.getActualPoints(position).forEach(filled::add);
            break;
          } else {
            position = next;
          }
          print(shape, position);
        }
      }
    }

    void debug(String fmt, Object ... args) {
      if (!DEBUG) {
        return;
      }
      System.out.println(String.format(fmt, args));
    }

    void print(Rock shape, Point position) {
      if (!DEBUG) {
        return;
      }
      Set<Point> currentRock = shape.getActualPoints(position);
      StringBuilder sb = new StringBuilder();
      int height = getHeight() + BOTTOM_OFFSET + 4;
      for (int y = 0 - height; y < 0; y++) {
        sb.append('|');
        for (int x = 0; x < WIDTH; x++) {
          Point p = Point.of(x, y);
          if (currentRock.contains(p)) {
            sb.append('@');
          } else if (filled.contains(p)) {
            sb.append('#');
          } else {
            sb.append('.');
          }
        }
        sb.append('|');
        sb.append('\n');
      }
      sb.append('+');
      for (int i = 0; i < WIDTH; i++) {
        sb.append('-');
      }
      sb.append('+');
      System.out.println(sb.toString());

      try {
        System.in.read();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }


  public static void main(String[] args) throws Exception {
    RockChamber chamber = RockChamber.initialize(
        InputHelper.linesFromResource(INPUT).flatMapToInt(String::chars).mapToObj(c -> (char) c)
            .collect(Collectors.toList()));
    chamber.dropRocks(2022);
    System.out.println("Part 1: " + chamber.getHeight());
    chamber.clear();
    // chamber.dropRocks(1000000000000L);
    System.out.println("Part 2: " + chamber.getHeight());
  }
}
