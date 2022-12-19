package aoc2022.day17;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

  private static record MoveAndShapeState(int moveIndex, int rockIndex) {
  }

  private static record RockAndHeightState(int rocksDropped, int highestRock) {
  }

  private static record Pattern(MoveAndShapeState state, int rockFrequency, int heightDelta) {
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

    private int highestRock = 0;

    private RockChamber(List<Character> sequence) {
      this.sequence = Collections.unmodifiableList(sequence);
    }

    static RockChamber initialize(List<Character> sequence) {
      RockChamber chamber = new RockChamber(sequence);
      IntStream.range(0, WIDTH).mapToObj(x -> Point.of(x, 0)).forEach(chamber.filled::add);
      return chamber;
    }

    int getHeight() {
      return 0 - highestRock;
    }

    void clear() {
      filled.clear();
      IntStream.range(0, WIDTH).mapToObj(x -> Point.of(x, 0)).forEach(filled::add);
      highestRock = 0;
    }

    boolean topRowFilled() {
      return IntStream.range(0, WIDTH).mapToObj(x -> Point.of(x, highestRock)).allMatch(filled::contains);
    }

    /**
     * @return maximum height reached.
     */
    long dropRocks(long numRocks) {
      clear();
      int moveSequence = 0;

      // Cached map of move sequence index -> rock shape index -> height when the top row is filled.
      // This is used to detect repeating cycles and extrapolate height for larger numbers of rocks.
      Map<MoveAndShapeState, RockAndHeightState> cache = new HashMap<>();

      int iterations = (int) Math.min(numRocks, (long) Integer.MAX_VALUE);
      Optional<Pattern> detectedPattern = Optional.empty();
      
      int i = 0;
      for (; i < iterations; i++) {
        Rock shape = SHAPES.get(i % SHAPES.size());

        if (topRowFilled()) {
          MoveAndShapeState state = new MoveAndShapeState(moveSequence % sequence.size(), i % SHAPES.size());
          RockAndHeightState lastSeen = cache.get(state);
          if (lastSeen != null) {
            detectedPattern = Optional.of(
              new Pattern(state, i - lastSeen.rocksDropped(), highestRock - lastSeen.highestRock()));
            debug("Repeat! pattern=%s", detectedPattern);
            break;
          }
          cache.put(state, new RockAndHeightState(i, highestRock));
        }

        moveSequence = dropRock(moveSequence, shape);
      }

      if (iterations == numRocks && detectedPattern.isEmpty()) {
        return getHeight();
      }

      // Extrapolate to a start from the detected pattern and then reply the rocks remaining.
      Pattern pattern = detectedPattern.get();
      long remainingRocks = numRocks - i;
      long patternRepeats = remainingRocks / (long) pattern.rockFrequency();
      long height = highestRock + (patternRepeats * (long) pattern.heightDelta());

      int extraRocks = (int) (remainingRocks % pattern.rockFrequency());
      int rockSequence = pattern.state().rockIndex();
      
      clear();
      for (int e = 0; e < extraRocks; e++) {
        Rock shape = SHAPES.get(rockSequence++ % SHAPES.size());
        moveSequence = dropRock(moveSequence, shape);
      }

      return 0 - (height + highestRock); 
    }

    /**
     * @return next moveSequence.
     */
    int dropRock(int moveSequence, Rock shape) {
      Point position = Point.of(LEFT_OFFSET, highestRock - BOTTOM_OFFSET - 1);
      // print(shape, position);
      while (true) {
        char move = sequence.get(moveSequence++ % sequence.size());
        // debug("move %s", move);
        Point next = Point.of(position.getX() + (move == '>' ? 1 : -1), position.getY());
        if (next.getX() >= 0 && shape.rightEdge(next) < WIDTH
            && Collections.disjoint(shape.getActualPoints(next), filled)) {
          position = next;
        }
        // print(shape, position);

        next = Point.of(position.getX(), position.getY() + 1);
        boolean atRest = shape.getActualPoints(next).stream().anyMatch(filled::contains);
        if (atRest) {
          shape.getActualPoints(position).forEach(p -> {
            filled.add(p);
            highestRock = Math.min(p.getY(), highestRock);
          });
          break;
        } else {
          position = next;
        }
        // print(shape, position);
      }
      return moveSequence;
    }

    void debug(String fmt, Object... args) {
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
    System.out.println("Part 1: " + chamber.dropRocks(2022));
    // chamber.dropRocks(1000000000000L);
    System.out.println("Part 2: " + chamber.dropRocks(1000000000000L));
  }
}
