package aoc2022.day22;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/22}. */
public class MonkeyMap {

  private static final String INPUT = "aoc2022/day22/input.txt";

  // TODO(maybe?): Make this generic to handle any folding cube shape rather than
  // hard-coding.
  private static final Supplier<CubeTeleporter> TELEPORTER = (sideLength) -> INPUT.contains("example" ? new ExampleCubeTeleporter(sideLength) : new InputCubeTeleporter(sideLength);

  private static enum Direction {
    RIGHT,
    DOWN,
    LEFT,
    UP,
  }

  private static record Teleport(int row, int col, Direction direction) {
  }

  private static class Board {
    final List<String> lines;
    final String instruction;

    int row = 0;
    int col = 0;
    Direction dir = Direction.RIGHT;
    int i = 0;

    Board(List<String> lines, String instruction) {
      this.lines = lines;
      this.instruction = instruction;
      row = 0;
      col = startOfRow(0);
      dir = Direction.RIGHT;
      i = 0;
    }

    Optional<Teleport> computeTeleport(int row, int col, Direction direction) {
      if (!Character.isWhitespace(charAt(row, col))) {
        return Optional.empty();
      }
      switch (dir) {
        case RIGHT:
          return Optional.of(new Teleport(row, startOfRow(row), dir));
        case DOWN:
          return Optional.of(new Teleport(startOfCol(col), col, dir));
        case LEFT:
          return Optional.of(new Teleport(row, endOfRow(row), dir));
        case UP:
          return Optional.of(new Teleport(endOfCol(col), col, dir));
        default:
          throw new IllegalStateException();
      }
    }

    void navigate() {
      while (i < instruction.length()) {
        char c = instruction.charAt(i++);
        if (Character.isDigit(c)) {
          int steps = c - '0';
          while (i < instruction.length() && Character.isDigit(c = instruction.charAt(i))) {
            i++;
            steps = (steps * 10) + (c - '0');
          }
          walk(steps);
        } else {
          turn(c);
        }
      }
    }

    void walk(int steps) {
      for (int s = 0; s < steps; s++) {
        int nextRow = dir == Direction.DOWN ? row + 1 : dir == Direction.UP ? row - 1 : row;
        int nextCol = dir == Direction.RIGHT ? col + 1 : dir == Direction.LEFT ? col - 1 : col;

        Optional<Teleport> teleport = computeTeleport(nextRow, nextCol, dir);
        if (teleport.isPresent()) {
          nextRow = teleport.get().row;
          nextCol = teleport.get().col;
        }

        if (charAt(nextRow, nextCol) == '#') {
          break;
        }

        row = nextRow;
        col = nextCol;
        if (teleport.isPresent()) {
          dir = teleport.get().direction;
        }
      }
    }

    void turn(char c) {
      if (c == 'R') {
        dir = dir.values()[(dir.ordinal() + 1) % dir.values().length];
      } else if (dir.ordinal() == 0) {
        dir = dir.values()[dir.values().length - 1];
      } else {
        dir = dir.values()[dir.ordinal() - 1];
      }
    }

    int startOfRow(int row) {
      return Math.min(lines.get(row).indexOf('#'), lines.get(row).indexOf('.'));
    }

    int endOfRow(int row) {
      return Math.max(lines.get(row).lastIndexOf('#'), lines.get(row).lastIndexOf('.'));
    }

    int startOfCol(int col) {
      for (int r = 0; r < lines.size(); r++) {
        String line = lines.get(r);
        if (col >= line.length()) {
          continue;
        }
        if (Character.isWhitespace(line.charAt(col))) {
          continue;
        }
        return r;
      }
      throw new IllegalStateException("Couldn't find start of  col " + col);
    }

    int endOfCol(int col) {
      for (int r = lines.size() - 1; r >= 0; r--) {
        String line = lines.get(r);
        if (col >= line.length()) {
          continue;
        }
        if (Character.isWhitespace(line.charAt(col))) {
          continue;
        }
        return r;
      }
      throw new IllegalStateException("Couldn't find end of  col " + col);
    }

    char charAt(int row, int col) {
      if (row < 0 || col < 0 || row >= lines.size() || col >= lines.get(row).length()) {
        return ' ';
      }
      return lines.get(row).charAt(col);
    }
  }

  private static long computeFinalPositionAndFacing(Board board) {
    long row = board.row + 1;
    long col = board.col + 1;
    long facing = board.dir.ordinal();
    return (row * 1000L) + (col * 4L) + facing;
  }

  private static abstract class CubeTeleporter {
    final int sideLength;

    CubeTeleporter(int sideLength) {
      this.sideLength = sideLength;
    }

    abstract Optional<Teleport> computeTeleport(int row, int col, Direction direction);
  }

  /**
   * [ ][ ][T][ ]
   * [K][L][F][ ]
   * [ ][ ][B][R]
   */
  private static class ExampleCubeTeleporter extends CubeTeleporter {
    ExampleCubeTeleporter(int sideLength) {
      super(sideLength);
    }

    @Override
    Optional<Teleport> computeTeleport(int row, int col, Direction direction) {
      switch (direction) {
        case RIGHT:
          if (row < sideLength) {
            if (col >= sideLength * 3) {
              // Teleport from right edge of TOP to right edge of RIGHT, now going left.
              return Optional.of(new Teleport(sideLength * 3 - row, sideLength * 4 - 1, Direction.LEFT));
            }
          } else if (row >= sideLength && row < sideLength * 2) {
            if (col >= sideLength * 3) {
              // Teleport from right edge of FRONT to top edge of RIGHT, now going down.
              return Optional.of(new Teleport(sideLength * 2, sideLength * 3 + (sideLength * 2 - row), Direction.DOWN));
            }
          } else {
            if (col >= sideLength * 4) {
              // Teleport from right edge of RIGHT to right edge of TOP, now going left.
              return Optional.of(new Teleport(sideLength * 3 - row, sideLength * 3, Direction.LEFT));
            }
          }
        case DOWN:
        case LEFT:
        case UP:
        default:
          throw new IllegalArgumentException();
      }
      return Optional.empty();
    }
  }

  /**
   * [ ][#][#]
   * [ ][#][ ]
   * [#][#][ ]
   * [#][ ][ ]
   */
  private static class InputCubeTeleporter extends CubeTeleporter {
    InputCubeTeleporter(int sideLength) {
      super(sideLength);
    }

    @Override
    Optional<Teleport> computeTeleport(int row, int col, Direction direction) {
      switch (direction) {
        case RIGHT:
        case DOWN:
        case LEFT:
        case UP:
        default:
          throw new IllegalArgumentException();
      }
      return Optional.empty();
    }
  }

  public static void main(String[] args) throws Exception {
    List<String> lines = InputHelper.linesFromResource(INPUT).collect(Collectors.toList());

    String instruction = lines.get(lines.size() - 1);

    // Part 1.
    Board board = new Board(lines.subList(0, lines.size() - 2), instruction);
    board.navigate();

    System.out.println("Part 1: " + computeFinalPositionAndFacing(board));
  }
}
