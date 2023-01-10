package aoc2022.day24;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import aoccommon.Debug;
import aoccommon.InputHelper;
import aoccommon.Point;
import aoccommon.Stats;

/** Solution for {@link https://adventofcode.com/2022/day/24}. */
public class BlizzardBasin {

  private static final String INPUT = "aoc2022/day24/example.txt";

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
      return blizzards.stream()
          .map(b -> moveBlizzard(b))
          .collect(Collectors.toSet());
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

  private static record State(Point position, Set<Blizzard> blizzards) {
  }

  private static class Navigator {
    private final Basin basin;
    // Position -> blizzard state -> fewest minutes.
    private final Map<State, Integer> cache = new HashMap<>();

    Navigator(Basin basin) {
      this.basin = basin;
    }

    int navigate(Set<Blizzard> blizzards) {
      return navigate(new State(basin.entrance(), blizzards), new HashSet<>());
    }

    int navigate(State state, Set<State> visited) {
      // print(basin, state.position(), state.blizzards());
      // Debug.waitForInput();
      OptionalInt result = Stream.ofNullable(cache.get(state)).mapToInt(i -> i).findAny();
      if (result.isPresent()) {
        Stats.incrementCounter("cache-hit");
        return result.getAsInt();
      }

      if (state.position().equals(basin.exit())) {
        // System.out.println(visited.stream().map(State::position).collect(Collectors.toSet()));
        // Debug.waitForInput();
        return 0;
      }

      Set<Blizzard> nextBlizzards = basin.moveBlizzards(state.blizzards());
      Set<Point> occupied = nextBlizzards.stream().map(Blizzard::position).collect(Collectors.toSet());
      Stream.Builder<State> nextStates = Stream.builder();

      // Option 1: stay put if a blizzard isn't coming.
      if (!occupied.contains(state.position())) {
        nextStates.add(new State(state.position(), nextBlizzards));
      }

      // Option 2: move up.
      Point next = Point.of(state.position().getX(), state.position().getY() - 1);
      if (next.getY() > 0 && !occupied.contains(next)) {
        nextStates.add(new State(next, nextBlizzards));
      }

      // Option 3: move right.
      next = Point.of(state.position().getX() + 1, state.position().getY());
      if (next.getY() > 0 && next.getX() < basin.rightWall() && !occupied.contains(next)) {
        nextStates.add(new State(next, nextBlizzards));
      }

      // Option 4: move down.
      next = Point.of(state.position().getX(), state.position().getY() + 1);
      if ((basin.exit().equals(next) || next.getY() < basin.bottomWall()) && !occupied.contains(next)) {
        nextStates.add(new State(next, nextBlizzards));
      }

      // Option 5: move left.
      next = Point.of(state.position().getX() - 1, state.position().getY());
      if (next.getY() > 0 && next.getX() > 0 && !occupied.contains(next)) {
        nextStates.add(new State(next, nextBlizzards));
      }

      result = nextStates.build()
          .filter(s -> !visited.contains(s))
          .mapToInt(s -> 1 + navigate(s, Stream.concat(Stream.of(s), visited.stream()).collect(Collectors.toSet())))
          .filter(i -> i >= 1)
          .min();
      if (result.isPresent()) {
        cache.put(state, result.getAsInt());
      }
      return result.orElse(-1);
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

    Navigator navigator = new Navigator(basin);
    System.out.println("Part 1: " + navigator.navigate(blizzards));
    Stats.print(System.out);
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