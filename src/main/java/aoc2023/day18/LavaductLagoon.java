package aoc2023.day18;

import aoccommon.Direction;
import aoccommon.InputHelper;
import aoccommon.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Solution for {@link https://adventofcode.com/2023/day/18}.
 */
public class LavaductLagoon {

  private static final String INPUT = "aoc2023/day18/input.txt";
  private static final String EXAMPLE = "aoc2023/day18/example.txt";

  private static final Map<String, Direction> DIRECTIONS = Arrays.stream(Direction.values())
      .collect(Collectors.toMap(d -> d.name().substring(0, 1), d -> d));

  private record Instruction(Direction d, int n, String color) {
    private static final Pattern REGEX = Pattern.compile("^([URDL])\\s(\\d+)\\s\\((#\\S+)\\)$");

    static Instruction parse(String line) {
      Matcher m = REGEX.matcher(line);
      if (!m.matches()) {
        throw new IllegalArgumentException();
      }
      return new Instruction(DIRECTIONS.get(m.group(1)), Integer.parseInt(m.group(2)), m.group(3));
    }

    Instruction correct() {
      int n = Integer.parseInt(this.color.substring(1, 6), 16);
      Direction d = switch (this.color.charAt(6)) {
        case '0' -> Direction.RIGHT;
        case '1' -> Direction.DOWN;
        case '2' -> Direction.LEFT;
        case '3' -> Direction.UP;
        default -> throw new IllegalArgumentException(this.color);
      };
      return new Instruction(d, n, this.color);
    }
  }

  private static class Digger {
    private final static Point START = Point.of(0, 0);
    private final List<Point> trench = new ArrayList<>();

    void dig(List<Instruction> steps) {
      Point current = START;
      for (Instruction step : steps) {
        for (int i = 0; i < step.n(); i++) {
          current = step.d().apply(current);
          trench.add(current);
        }
      }
      if (!current.equals(START)) {
        throw new IllegalStateException(current.toString());
      }
    }

    long calculateArea() {
      // Shoelace formula
      // https://en.wikipedia.org/wiki/Shoelace_formula
      // A = 1/2 * sum of x(i) ^ x(i+1)
      long area = 0;
      for (int i = 0; i < trench.size(); i++) {
        Point a = trench.get(i);
        Point b = i < trench.size() - 1 ? trench.get(i + 1) : trench.get(0);
        area += crossProduct(a, b);
      }
      area = area / 2;

      // Pick's theorem
      // https://en.wikipedia.org/wiki/Pick%27s_theorem
      // A = i + b/2 - 1
      // i = A - b/2 + 1
      // i + b = A + b - b/2 + 1
      // i + b = A + b/2 + 1
      return area + (trench.size() / 2) + 1;
    }

    long crossProduct(Point a, Point b) {
      return (a.getX() * b.getY()) - (a.getY() * b.getX());
    }
  }

  public static void main(String[] args) throws Exception {
    // Debug.enablePrint();
    List<Instruction> plan = InputHelper.linesFromResource(INPUT)
        .map(Instruction::parse)
        .toList();

    // Part 1.
    Digger digger = new Digger();
    digger.dig(plan);
    System.out.println("Part 1: " + digger.calculateArea());

    // Part 2.
    plan = plan.stream().map(Instruction::correct).toList();
    digger = new Digger();
    digger.dig(plan);
    System.out.println("Part 2: " + digger.calculateArea());
  }
}
