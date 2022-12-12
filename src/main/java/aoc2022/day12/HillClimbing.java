package aoc2022.day12;

import java.util.List;
import java.util.stream.Collectors;

import aoccommon.InputHelper;
import aoccommon.Point;

/** Solution for {@link https://adventofcode.com/2022/day/12}. */
public class HillClimbing {

  private static final String INPUT = "aoc2022/day12/input.txt";

  public static void main(String [] args) throws Exception {
    List<String> input = InputHelper.linesFromResource(INPUT).collect(Collectors.toList());
    Climber climber = Climber.create(input);
  }

  private static class Climber {
    private final List<String> input;
    private final Point start;
    private final Point end;
    private final int maxX;
    private final int maxY;

    private Climber(List<String> input, Point start, Point end, int maxX, int maxY) {
      this.input = input;
      this.start = start;
      this.end = end;
      this.maxX = maxX;
      this.maxY = maxY;
    }

    static Climber create(List<String> input) {
      Point start = null, end = null;
      int maxX = -1;
      for (int y = 0; y < input.size(); y++) {
        for (int x = 0; x < input.get(y).length(); x++) {
          char c = input.get(y).charAt(x);
          if (c == 'S') {
            start = Point.of(x, y);
          } else if (c == 'E') {
            end = Point.of(x, y);
          }
        }
        maxX = input.get(y).length() - 1; // Assume all rows same length.
      }
      return new Climber(input, start, end, maxX, input.size() - 1);
    }
  }
}
