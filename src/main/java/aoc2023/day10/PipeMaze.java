package aoc2023.day10;

import aoccommon.Debug;
import aoccommon.InputHelper;
import aoccommon.Pair;
import aoccommon.Point;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Solution for {@link https://adventofcode.com/2023/day/10}.
 */
public class PipeMaze {

  private static final String INPUT = "aoc2023/day10/input.txt";
  private static final String EXAMPLE = "aoc2023/day10/example.txt";

  private enum Direction {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    Direction opposite() {
      return Direction.values()[(this.ordinal() + 2) % 4];
    }
  }

  private record Maze(List<String> rows) {
    Optional<Point> move(Point p, Direction dir) {
      return switch (dir) {
        case NORTH -> p.getY() > 0
            ? Optional.of(Point.of(p.getX(), p.getY() - 1))
            : Optional.empty();
        case EAST -> p.getX() < rows.get(p.getY()).length() - 1
            ? Optional.of(Point.of(p.getX() + 1, p.getY()))
            : Optional.empty();
        case SOUTH -> p.getY() < rows.size() - 1
            ? Optional.of(Point.of(p.getX(), p.getY() + 1))
            : Optional.empty();
        case WEST -> p.getX() > 0
            ? Optional.of(Point.of(p.getX() - 1, p.getY()))
            : Optional.empty();
      };
    }

    Character at(Point p) {
      return rows.get(p.getY()).charAt(p.getX());
    }

    void print(Map<Point, Character> annotations) {
      for (int row = 0; row < rows.size(); row++) {
        StringBuilder sb = new StringBuilder();
        for (int col = 0; col < rows.get(row).length(); col++) {
          Point p = Point.of(col, row);
          if (annotations.containsKey(p)) {
            sb.append(annotations.get(p));
          } else {
            sb.append(at(p));
          }
        }
        Debug.println(sb.toString());
      }
    }
  }

  private static Map<Character, List<Direction>> SPACES = Map.of(
      '|', List.of(Direction.NORTH, Direction.SOUTH),
      '-', List.of(Direction.EAST, Direction.WEST),
      'L', List.of(Direction.NORTH, Direction.EAST),
      'J', List.of(Direction.NORTH, Direction.WEST),
      '7', List.of(Direction.SOUTH, Direction.WEST),
      'F', List.of(Direction.SOUTH, Direction.EAST),
      '.', List.of()
  );

  private static Set<Character> PIPES = SPACES.keySet().stream().filter(c -> c != '.').collect(Collectors.toSet());

  public static void main(String[] args) throws Exception {
    // Debug.enablePrint();

    Maze maze = new Maze(InputHelper.linesFromResource(INPUT).toList());
    Point start = IntStream.range(0, maze.rows.size())
        .mapToObj(row -> IntStream.range(0, maze.rows.get(row).length())
            .filter(col -> {
              return maze.rows.get(row).charAt(col) == 'S';
            })
            .mapToObj(col -> Point.of(col, row))
            .findFirst())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst()
        .get();

    // Part 1.
    Pair<Character, List<Point>> loop = PIPES.stream()
        .map(pipe -> Pair.of(pipe, findLoop(maze, start, pipe)))
        .filter(pair -> pair.second().isPresent())
        .map(pair -> Pair.of(pair.first(), pair.second().get()))
        .findFirst()
        .get();
    System.out.println("Part 1: " + loop.second().size() / 2);

    long area = areaInLoop(maze, new HashSet<>(loop.second()), loop.first());
    System.out.println("Part 2: " + area);
  }

  private static Optional<List<Point>> findLoop(Maze maze, Point start, Character startPipe) {
    Direction dir = SPACES.get(startPipe).get(0);
    Direction endDir = SPACES.get(startPipe).get(1).opposite();
    Point current = start;
    List<Point> loop = new ArrayList<>();
    while (true) {
      loop.add(current);
      Optional<Point> next = maze.move(current, dir);
      if (!next.isPresent()) {
        return Optional.empty();
      }
      if (next.get().equals(start)) {
        if (endDir == dir) {
          return Optional.of(loop);
        } else {
          return Optional.empty();
        }
      }
      List<Direction> nextDirs = SPACES.get(maze.at(next.get()));
      final Direction expected = dir.opposite();
      Optional<Direction> matching = nextDirs.stream().filter(d -> d == expected).findFirst();
      if (!matching.isPresent()) {
        return Optional.empty();
      }

      current = next.get();
      dir = nextDirs.stream().filter(d -> d != expected).findFirst().get();
    }
  }

  private static int areaInLoop(Maze maze, Set<Point> loop, char startPipe) {
    Set<Point> area = new HashSet<>();

    Function<Point, Character> charAt = p -> {
      char c= maze.at(p);
      if (c == 'S') {
        return startPipe;
      }
      return c;
    };

    for (int row = 0; row < maze.rows.size(); row++) {
      for (int col = 0; col < maze.rows.get(row).length(); col++) {
        Point p = Point.of(col, row);
        if (loop.contains(p)) {
          continue;
        }

        // Check if we're enclosed with the loop in all directions.

        // North.
        Stack<Character> elbows = new Stack<>();
        int crossings = 0;
        for (int i = row - 1; i >= 0; i--) {
          Point o = Point.of(col, i);
          if (!loop.contains(o)) {
            continue;
          }
          char c = charAt.apply(o);
          switch (c) {
            case '-':
              crossings++;
              break;
            case 'J', 'L':
              elbows.push(c);
              break;
            case 'F':
              char e = elbows.pop();
              if (e == 'J') {
                crossings++;
              }
              break;
            case '7':
              e = elbows.pop();
              if (e == 'L') {
                crossings++;
              }
              break;
          }
        }
        if (crossings % 2 == 0) {
          continue;
        }

        // East.
        elbows.clear();
        crossings = 0;
        for (int i = col + 1; i < maze.rows.get(row).length(); i++) {
          Point o = Point.of(i, row);
          if (!loop.contains(o)) {
            continue;
          }
          char c = charAt.apply(o);
          switch (c) {
            case '|':
              crossings++;
              break;
            case 'F', 'L':
              elbows.push(c);
              break;
            case '7':
              char e = elbows.pop();
              if (e == 'L') {
                crossings++;
              }
              break;
            case 'J':
              e = elbows.pop();
              if (e == 'F') {
                crossings++;
              }
              break;
          }
        }
        if (crossings % 2 == 0) {
          continue;
        }

        // South.
        elbows.clear();
        crossings = 0;
        for (int i = row + 1; i < maze.rows.size(); i++) {
          Point o = Point.of(col, i);
          if (!loop.contains(o)) {
            continue;
          }
          char c = charAt.apply(o);
          switch (c) {
            case '-':
              crossings++;
              break;
            case 'F', '7':
              elbows.push(c);
              break;
            case 'J':
              char e = elbows.pop();
              if (e == 'F') {
                crossings++;
              }
              break;
            case 'L':
              e = elbows.pop();
              if (e == '7') {
                crossings++;
              }
              break;
          }
        }
        if (crossings % 2 == 0) {
          continue;
        }

        // West.
        elbows.clear();
        crossings = 0;
        for (int i = col - 1; i >= 0; i--) {
          Point o = Point.of(i, row);
          if (!loop.contains(o)) {
            continue;
          }
          char c = charAt.apply(o);
          switch (c) {
            case '|':
              crossings++;
              break;
            case '7', 'J':
              elbows.push(c);
              break;
            case 'L':
              char e = elbows.pop();
              if (e == '7') {
                crossings++;
              }
              break;
            case 'F':
              e = elbows.pop();
              if (e == 'J') {
                crossings++;
              }
              break;
          }
        }
        if (crossings % 2 == 0) {
          continue;
        }

        area.add(p);
      }
    }
    maze.print(area.stream().collect(Collectors.toMap(p -> p, p -> 'I')));
    return area.size();
  }
}
