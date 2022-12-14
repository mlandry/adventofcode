package aoc2022.day24;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import aoccommon.InputHelper;
import aoccommon.Point;
import aoccommon.Stats;

/** Solution for {@link https://adventofcode.com/2022/day/24}. */
public class BlizzardBasin {

  private static final String INPUT = "aoc2022/day24/input.txt";

  private static enum Direction {
    UP('^'),
    DOWN('v'),
    LEFT('<'),
    RIGHT('>');

    private static Map<Character, Direction> CHAR_TO_DIR = Arrays.stream(values())
        .collect(Collectors.toMap(Direction::getChar, Function.identity()));

    static Direction forChar(char c) {
      return CHAR_TO_DIR.get(c);
    }

    private final char c;

    private Direction(char c) {
      this.c = c;
    }

    char getChar() {
      return c;
    }
  }

  private static record Blizzard(Direction direction, Point position) {
  }

  private static record Basin(int rightWall, int bottomWall, Point entrance, Point exit) {
    Set<Blizzard> moveBlizzards(Set<Blizzard> blizzards) {
      Set<Blizzard> result = blizzards.stream()
          .map(b -> moveBlizzard(b))
          .collect(Collectors.toSet());
      return result;
    }

    Blizzard moveBlizzard(Blizzard blizzard) {
      int row = blizzard.position().getY();
      int col = blizzard.position().getX();
      switch (blizzard.direction()) {
        case UP:
          return new Blizzard(blizzard.direction(), Point.of(col, row == 1 ? bottomWall - 1 : row - 1));
        case DOWN:
          return new Blizzard(blizzard.direction(), Point.of(col, row == bottomWall - 1 ? 1 : row + 1));
        case LEFT:
          return new Blizzard(blizzard.direction(), Point.of(col == 1 ? rightWall - 1 : col - 1, row));
        case RIGHT:
          return new Blizzard(blizzard.direction(), Point.of(col == rightWall - 1 ? 1 : col + 1, row));
        default:
          throw new IllegalStateException();
      }
    }
  }

  private static List<Set<Point>> precomputeBlizzardSpaces(Basin basin, Set<Blizzard> blizzards) {
    Set<Set<Point>> seen = new HashSet<>();
    List<Set<Point>> precomputed = new ArrayList<>();
    Set<Point> spaces = blizzards.stream().map(Blizzard::position).collect(Collectors.toSet());
    while (!seen.contains(spaces)) {
      seen.add(spaces);
      precomputed.add(spaces);
      blizzards = basin.moveBlizzards(blizzards);
      spaces = blizzards.stream().map(Blizzard::position).collect(Collectors.toSet());
    }
    return precomputed;
  }

  private static record State(Point position, int minutes) {
  }

  private static record Positions(Point position, int blizzardIndex) {
  }

  private static int compare(State s1, State s2, Basin basin) {
    // Prefer nodes with fewer minutes first.
    int result = Integer.compare(s1.minutes(), s2.minutes());
    if (result != 0) {
      return result;
    }
    // Then compare distance to goal (roughly).
    return Integer
        .compare(estimateDistance(s1.position(), basin.exit()), estimateDistance(s2.position(), basin.exit()));
  }

  private static int estimateDistance(Point point, Point dest) {
    return (dest.getY() - point.getY()) + (dest.getX() - point.getX());
  }

  private static class Navigator {
    private final Basin basin;
    private final Queue<State> queue;
    private final Set<Positions> visited;

    Navigator(Basin basin) {
      this.basin = basin;
      // queue = new PriorityQueue<>((s1, s2) -> compare(s1, s2, basin));
      queue = new LinkedList<>();
      visited = new HashSet<>();
    }

    int navigate(List<Set<Point>> blizzardSpaces, int startMinute) {
      queue.offer(new State(basin.entrance(), startMinute));
      while (!queue.isEmpty()) {
        Stats.incrementCounter("visited");
        State state = queue.poll();
        if (state.position().equals(basin.exit())) {
          return state.minutes();
        }

        int nextMinutes = state.minutes() + 1;
        int blizzardIndex = nextMinutes % blizzardSpaces.size();
        Set<Point> occupied = blizzardSpaces.get(blizzardIndex);

        // Option 1: move right.
        Stream.Builder<State> nextStates = Stream.builder();
        Point next = Point.of(state.position().getX() + 1, state.position().getY());
        if (next.getY() > 0 && next.getX() < basin.rightWall() && !occupied.contains(next)) {
          nextStates.add(new State(next, nextMinutes));
        }

        // Option 2: move down.
        next = Point.of(state.position().getX(), state.position().getY() + 1);
        if ((basin.exit().equals(next) || next.getY() < basin.bottomWall()) && !occupied.contains(next)) {
          nextStates.add(new State(next, nextMinutes));
        }

        // Option 3: stay put if a blizzard isn't coming.
        if (!occupied.contains(state.position())) {
          nextStates.add(new State(state.position(), nextMinutes));
        }

        // Option 4: move up.
        next = Point.of(state.position().getX(), state.position().getY() - 1);
        if ((basin.exit().equals(next) || next.getY() > 0) && !occupied.contains(next)) {
          nextStates.add(new State(next, nextMinutes));
        }

        // Option 5: move left.
        next = Point.of(state.position().getX() - 1, state.position().getY());
        if (next.getY() < basin.bottomWall() && next.getY() > 0 && next.getX() > 0 && !occupied.contains(next)) {
          nextStates.add(new State(next, nextMinutes));
        }

        nextStates.build().forEach(s -> {
          if (visited.add(new Positions(s.position(), blizzardIndex))) {
            queue.offer(s);
          }
        });
      }
      return -1;
    }
  }

  public static void main(String[] args) throws Exception {
    List<String> lines = InputHelper.linesFromResource(INPUT).collect(Collectors.toList());
    int entranceX = lines.get(0).indexOf('.');
    int exitX = lines.get(lines.size() - 1).indexOf('.');

    Basin basin = new Basin(lines.get(0).length() - 1, lines.size() - 1, Point.of(entranceX, 0),
        Point.of(exitX, lines.size() - 1));

    Set<Blizzard> blizzards = new HashSet<>();
    for (int y = 1; y < basin.bottomWall(); y++) {
      for (int x = 1; x < basin.rightWall(); x++) {
        char c = lines.get(y).charAt(x);
        if (c == '.') {
          continue;
        }
        blizzards.add(new Blizzard(Direction.forChar(c), Point.of(x, y)));
      }
    }

    Stats.startTimer("precompute");
    List<Set<Point>> blizzardSpaces = precomputeBlizzardSpaces(basin, blizzards);
    Stats.endTimer("precompute");

    Stats.startTimer("navigate");
    Navigator navigator = new Navigator(basin);
    int minutes = navigator.navigate(blizzardSpaces, 0);
    Stats.endTimer("navigate");
    System.out.println("Part 1: " + minutes);

    Basin reversed = new Basin(basin.rightWall(), basin.bottomWall(), basin.exit(), basin.entrance());

    Stats.startTimer("navigate");
    navigator = new Navigator(reversed);
    minutes = navigator.navigate(blizzardSpaces, minutes);
    Stats.endTimer("navigate");

    Stats.startTimer("navigate");
    navigator = new Navigator(basin);
    minutes = navigator.navigate(blizzardSpaces, minutes);
    Stats.endTimer("navigate");
    System.out.println("Part 2: " + minutes);

    // Stats.print(System.out);
  }

  private static void print(Basin basin, Point position, Set<Blizzard> blizzards) {
    StringBuilder sb = new StringBuilder();
    Map<Point, Character> blizzardMap = new HashMap<>();
    for (Blizzard blizzard : blizzards) {
      Character c = blizzardMap.get(blizzard.position());
      if (c == null) {
        blizzardMap.put(blizzard.position(), blizzard.direction().getChar());
      } else if (Character.isDigit(c)) {
        int num = (c - '0') + 1;
        blizzardMap.put(blizzard.position(), Character.forDigit(num, 10));
      } else {
        blizzardMap.put(blizzard.position(), '2');
      }
    }
    for (int x = 0; x <= basin.rightWall; x++) {
      if (position.equals(Point.of(x, 0))) {
        sb.append('E');
      } else if (basin.entrance.getX() == x) {
        sb.append('.');
      } else {
        sb.append('#');
      }
    }
    sb.append('\n');
    for (int y = 1; y < basin.bottomWall; y++) {
      sb.append('#');
      for (int x = 1; x < basin.rightWall; x++) {
        Point p = Point.of(x, y);
        if (position.equals(p)) {
          sb.append('E');
        } else if (blizzardMap.containsKey(p)) {
          sb.append(blizzardMap.get(p));
        } else {
          sb.append('.');
        }
      }
      sb.append('#');
      sb.append('\n');
    }
    for (int x = 0; x <= basin.rightWall; x++) {
      if (position.equals(Point.of(x, basin.bottomWall))) {
        sb.append('E');
      } else if (basin.exit.getX() == x) {
        sb.append('.');
      } else {
        sb.append('#');
      }
    }
    sb.append('\n');
    System.out.println(sb.toString());
  }
}
