package aoc2023.day18;

import aoccommon.Direction;
import aoccommon.InputHelper;
import aoccommon.Pair;
import aoccommon.Point;

import java.util.*;
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
      Direction d = switch(this.color.charAt(6)) {
        case '0' -> Direction.RIGHT;
        case '1' -> Direction.DOWN;
        case '2' -> Direction.LEFT;
        case '3' -> Direction.UP;
        default -> throw new IllegalArgumentException(this.color);
      };
      return new Instruction(d, n, this.color);
    }
  }

  private static final Map<EnumSet<Direction>, Character> TRENCH_TYPE_MAP = Map.of(
      EnumSet.of(Direction.LEFT, Direction.RIGHT), '-',
      EnumSet.of(Direction.UP, Direction.DOWN), '|',
      EnumSet.of(Direction.UP, Direction.RIGHT), 'L',
      EnumSet.of(Direction.DOWN, Direction.RIGHT), 'F',
      EnumSet.of(Direction.LEFT, Direction.DOWN), '7',
      EnumSet.of(Direction.LEFT, Direction.UP), 'J'
  );

  private static class Digger {
    // TODO: track edge colors
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

//    long calculateArea() {
//      final Pair<Point, Point> bounds = Point.boundingBox(trench);
//      Map<Point, Character> types = new HashMap<>();
//      for (int i = 0; i < trench.size(); i++) {
//        Point p = trench.get(i);
//        Point prev = i == 0 ? trench.get(trench.size() - 1) : trench.get(i - 1);
//        Point next = i == trench.size() - 1 ? trench.get(0) : trench.get(i + 1);
//
//        Direction prevDir = null;
//        if (prev.getX() == p.getX()) {
//          prevDir = prev.getY() < p.getY() ? Direction.UP : Direction.DOWN;
//        } else {
//          prevDir = prev.getX() < p.getX() ? Direction.LEFT : Direction.RIGHT;
//        }
//        Direction nextDir = null;
//        if (next.getX() == p.getX()) {
//          nextDir = next.getY() < p.getY() ? Direction.UP : Direction.DOWN;
//        } else {
//          nextDir = next.getX() < p.getX() ? Direction.LEFT : Direction.RIGHT;
//        }
//        types.put(p, TRENCH_TYPE_MAP.get(EnumSet.of(prevDir, nextDir)));
//      }
//
//      // O(n^2) :(
//      long area = 0;
//      for (int y = bounds.first().getY(); y <= bounds.second().getY(); y++) {
//        for (int x = bounds.first().getX(); x <= bounds.second().getX(); x++) {
//          Point p = Point.of(x, y);
//          if (types.containsKey(p)) {
//            area++;
//            continue;
//          }
//
//
//          // Check if we're enclosed by the pit in all 4 directions.
//          // Up.
//          Stack<Character> elbows = new Stack<>();
//          int crossings = 0;
//          for (int i = y - 1; i >= bounds.first().getY(); i--) {
//            Point o = Point.of(x, i);
//            if (!types.containsKey(o)) {
//              continue;
//            }
//            char c = types.get(o);
//            switch (c) {
//              case '-':
//                crossings++;
//                break;
//              case 'J', 'L':
//                elbows.push(c);
//                break;
//              case 'F':
//                char e = elbows.pop();
//                if (e == 'J') {
//                  crossings++;
//                }
//                break;
//              case '7':
//                e = elbows.pop();
//                if (e == 'L') {
//                  crossings++;
//                }
//                break;
//            }
//          }
//          if (crossings % 2 == 0) {
//            continue;
//          }
//
//          // Right.
//          elbows.clear();
//          crossings = 0;
//          for (int i = x + 1; i <= bounds.second().getX(); i++) {
//            Point o = Point.of(i, y);
//            if (!types.containsKey(o)) {
//              continue;
//            }
//            char c = types.get(o);
//            switch (c) {
//              case '|':
//                crossings++;
//                break;
//              case 'F', 'L':
//                elbows.push(c);
//                break;
//              case '7':
//                char e = elbows.pop();
//                if (e == 'L') {
//                  crossings++;
//                }
//                break;
//              case 'J':
//                e = elbows.pop();
//                if (e == 'F') {
//                  crossings++;
//                }
//                break;
//            }
//          }
//          if (crossings % 2 == 0) {
//            continue;
//          }
//
//          // Down.
//          elbows.clear();
//          crossings = 0;
//          for (int i = y + 1; i <= bounds.second().getY(); i++) {
//            Point o = Point.of(x, i);
//            if (!types.containsKey(o)) {
//              continue;
//            }
//            char c = types.get(o);
//            switch (c) {
//              case '-':
//                crossings++;
//                break;
//              case 'F', '7':
//                elbows.push(c);
//                break;
//              case 'J':
//                char e = elbows.pop();
//                if (e == 'F') {
//                  crossings++;
//                }
//                break;
//              case 'L':
//                e = elbows.pop();
//                if (e == '7') {
//                  crossings++;
//                }
//                break;
//            }
//          }
//          if (crossings % 2 == 0) {
//            continue;
//          }
//
//          // Left.
//          elbows.clear();
//          crossings = 0;
//          for (int i = x - 1; i >= bounds.first().getX(); i--) {
//            Point o = Point.of(i, y);
//            if (!types.containsKey(o)) {
//              continue;
//            }
//            char c = types.get(o);
//            switch (c) {
//              case '|':
//                crossings++;
//                break;
//              case '7', 'J':
//                elbows.push(c);
//                break;
//              case 'L':
//                char e = elbows.pop();
//                if (e == '7') {
//                  crossings++;
//                }
//                break;
//              case 'F':
//                e = elbows.pop();
//                if (e == 'J') {
//                  crossings++;
//                }
//                break;
//            }
//          }
//          if (crossings % 2 == 0) {
//            continue;
//          }
//
//          area++;
//        }
//      }
//      return area;
//    }
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
