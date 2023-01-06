package aoc2022.day22;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import aoccommon.InputHelper;
import aoccommon.Pair;
import aoccommon.Point;

/** Solution for {@link https://adventofcode.com/2022/day/22}. */
public class MonkeyMap {

  private static final String INPUT = "aoc2022/day22/input.txt";

  private static boolean DEBUG = false;

  // TODO(maybe?): Make this generic to handle any folding cube shape rather than
  // hard-coding.
  private static final Function<Integer, CubeTeleporter> TELEPORTER = (sideLength) -> INPUT.contains("example")
      ? new ExampleCubeTeleporter(sideLength)
      : new InputCubeTeleporter(sideLength);

  private static enum Direction {
    RIGHT,
    DOWN,
    LEFT,
    UP,
  }

  private static final Map<Direction, Character> DIRECTION_CHARACTERS = Map.of(
      Direction.RIGHT, '>',
      Direction.DOWN, 'v',
      Direction.LEFT, '<',
      Direction.UP, '^');

  private static final Map<Direction, Direction> REVERSE = Map.of(
      Direction.RIGHT, Direction.LEFT,
      Direction.DOWN, Direction.UP,
      Direction.UP, Direction.DOWN,
      Direction.LEFT, Direction.RIGHT);

  private static final boolean FLIPPED = true;

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
        print();
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
          print();
        }
      }
    }

    void turn(char c) {
      if (c == 'R') {
        dir = Direction.values()[(dir.ordinal() + 1) % Direction.values().length];
      } else if (dir.ordinal() == 0) {
        dir = Direction.values()[Direction.values().length - 1];
      } else {
        dir = Direction.values()[dir.ordinal() - 1];
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

    void print() {
      if (!DEBUG) {
        return;
      }
      StringBuilder sb = new StringBuilder();
      for (int r = 0; r < lines.size(); r++) {
        String line = lines.get(r);
        for (int c = 0; c < line.length(); c++) {
          if (r == row && c == col) {
            sb.append(DIRECTION_CHARACTERS.get(dir));
          } else {
            sb.append(line.charAt(c));
          }
        }
        sb.append('\n');
      }
      System.out.println(sb.toString());
    }
  }

  private static long computeFinalPositionAndFacing(Board board) {
    long row = board.row + 1;
    long col = board.col + 1;
    long facing = board.dir.ordinal();
    return (row * 1000L) + (col * 4L) + facing;
  }

  private static enum Side {
    TOP,
    FRONT,
    RIGHT,
    BACK,
    LEFT,
    BOTTOM,
  }

  private static abstract class CubeTeleporter {
    private final int sideLength;
    private final Map<Side, Point> cubes = new HashMap<>();
    private final Map<Pair<Side, Direction>, Pair<Pair<Side, Direction>, Boolean>> edges = new HashMap<>();
    private final Map<Direction, Set<Side>> boundaries = new HashMap<>();

    CubeTeleporter(int sideLength) {
      this.sideLength = sideLength;
    }

    Optional<Teleport> computeTeleport(int row, int col, Direction direction) {
      Set<Side> boundarySides = boundaries.get(direction);

      Side source = null;
      switch (direction) {
        case RIGHT:
          for (Side boundary : boundarySides) {
            if (row >= firstRow(boundary) && row <= lastRow(boundary) && col > lastCol(boundary)) {
              source = boundary;
              break;
            }
          }
          break;
        case DOWN:
          for (Side boundary : boundarySides) {
            if (col >= firstCol(boundary) && col <= lastCol(boundary) && row > lastRow(boundary)) {
              source = boundary;
              break;
            }
          }
          break;
        case LEFT:
          for (Side boundary : boundarySides) {
            if (row >= firstRow(boundary) && row <= lastRow(boundary) && col < firstCol(boundary)) {
              source = boundary;
              break;
            }
          }
          break;
        case UP:
          for (Side boundary : boundarySides) {
            if (col >= firstCol(boundary) && col <= lastCol(boundary) && row < firstRow(boundary)) {
              source = boundary;
              break;
            }
          }
          break;
        default:
          throw new IllegalArgumentException();
      }

      if (source == null) {
        return Optional.empty();
      }
      return Optional.of(computeTeleport(source, direction, row, col));
    }

    private Teleport computeTeleport(Side srcSide, Direction srcDirection, int row, int col) {
      Pair<Pair<Side, Direction>, Boolean> destEdge = edges.get(Pair.of(srcSide, srcDirection));

      Side destSide = destEdge.first().first();
      Direction destDirection = destEdge.first().second();
      boolean flipped = destEdge.second();

      int rowIn = rowIn(srcSide, row);
      int colIn = colIn(srcSide, col);

      switch (srcDirection) {
        case RIGHT:
          switch (destDirection) {
            case RIGHT:
              return new Teleport(flipped ? lastRow(destSide) - rowIn : firstRow(destSide) + rowIn, firstCol(destSide),
                  destDirection);
            case DOWN:
              return new Teleport(firstRow(destSide), flipped ? lastCol(destSide) - rowIn : firstCol(destSide) + rowIn,
                  destDirection);
            case LEFT:
              return new Teleport(flipped ? lastRow(destSide) - rowIn : firstRow(destSide) + rowIn, lastCol(destSide),
                  destDirection);
            case UP:
              return new Teleport(lastRow(destSide), flipped ? lastCol(destSide) - rowIn : firstCol(destSide) + rowIn,
                  destDirection);
            default:
              throw new IllegalStateException();
          }
        case DOWN:
          switch (destDirection) {
            case RIGHT:
              return new Teleport(flipped ? lastRow(destSide) - colIn : firstRow(destSide) + colIn, firstCol(destSide),
                  destDirection);
            case DOWN:
              return new Teleport(firstRow(destSide), flipped ? lastCol(destSide) - colIn : firstCol(destSide) + colIn,
                  destDirection);
            case LEFT:
              return new Teleport(flipped ? lastRow(destSide) - colIn : firstRow(destSide) + colIn, lastCol(destSide),
                  destDirection);
            case UP:
              return new Teleport(lastRow(destSide), flipped ? lastCol(destSide) - colIn : firstCol(destSide) + colIn,
                  destDirection);
            default:
              throw new IllegalStateException();

          }
        case LEFT:
          switch (destDirection) {
            case RIGHT:
              return new Teleport(flipped ? lastRow(destSide) - rowIn : firstRow(destSide) + rowIn, firstCol(destSide),
                  destDirection);
            case DOWN:
              return new Teleport(firstRow(destSide), flipped ? lastCol(destSide) - rowIn : firstCol(destSide) + rowIn,
                  destDirection);
            case LEFT:
              return new Teleport(flipped ? lastRow(destSide) - rowIn : firstRow(destSide) + rowIn, lastCol(destSide),
                  destDirection);
            case UP:
              return new Teleport(lastRow(destSide), flipped ? lastCol(destSide) - rowIn : firstCol(destSide) + rowIn,
                  destDirection);
            default:
              throw new IllegalStateException();

          }
        case UP:
          switch (destDirection) {
            case RIGHT:
              return new Teleport(flipped ? lastRow(destSide) - colIn : firstRow(destSide) + colIn, firstCol(destSide),
                  destDirection);
            case DOWN:
              return new Teleport(firstRow(destSide), flipped ? lastCol(destSide) - colIn : firstCol(destSide) + colIn,
                  destDirection);
            case LEFT:
              return new Teleport(flipped ? lastRow(destSide) - colIn : firstRow(destSide) + colIn, lastCol(destSide),
                  destDirection);
            case UP:
              return new Teleport(lastRow(destSide), flipped ? lastCol(destSide) - colIn : firstCol(destSide) + colIn,
                  destDirection);
            default:
              throw new IllegalStateException();

          }
        default:
          throw new IllegalStateException();
      }
    }

    int sides(int num) {
      return num * sideLength;
    }

    void addSide(Side side, int cubeRow, int cubeCol) {
      cubes.put(side, Point.of(sides(cubeCol), sides(cubeRow)));
    }

    void addEdge(Side srcSide, Direction srcDirection, Side destSide, Direction destDirection, boolean flipped) {
      edges.put(Pair.of(srcSide, srcDirection), Pair.of(Pair.of(destSide, destDirection), flipped));
      edges.put(Pair.of(destSide, REVERSE.get(destDirection)), Pair.of(Pair.of(srcSide, REVERSE.get(srcDirection)), flipped));
    }

    void addBoundarySides(Direction direction, Side... sides) {
      boundaries.put(direction, Set.of(sides));
    }

    int firstRow(Side side) {
      return cubes.get(side).getY();
    }

    int lastRow(Side side) {
      return firstRow(side) + sides(1) - 1;
    }

    int firstCol(Side side) {
      return cubes.get(side).getX();
    }

    int lastCol(Side side) {
      return firstCol(side) + sides(1) - 1;
    }

    int rowIn(Side side, int row) {
      return row - firstRow(side);
    }

    int colIn(Side side, int col) {
      return col - firstCol(side);
    }
  }

  /**
   * [ ][ ][T][ ]
   * [K][L][F][ ]
   * [ ][ ][B][R]
   */
  private static class ExampleCubeTeleporter extends CubeTeleporter {
    ExampleCubeTeleporter(int sideLength) {
      super(sideLength);
      addSide(Side.TOP, 0, 2);
      addSide(Side.FRONT, 1, 2);
      addSide(Side.BACK, 1, 0);
      addSide(Side.LEFT, 1, 1);
      addSide(Side.BOTTOM, 2, 2);
      addSide(Side.RIGHT, 2, 3);

      addEdge(Side.TOP, Direction.RIGHT, Side.RIGHT, Direction.LEFT, FLIPPED);
      addEdge(Side.TOP, Direction.DOWN, Side.FRONT, Direction.DOWN, !FLIPPED);
      addEdge(Side.TOP, Direction.LEFT, Side.LEFT, Direction.DOWN, !FLIPPED);
      addEdge(Side.TOP, Direction.UP, Side.BACK, Direction.DOWN, FLIPPED);

      addEdge(Side.BACK, Direction.RIGHT, Side.LEFT, Direction.RIGHT, !FLIPPED);
      addEdge(Side.LEFT, Direction.RIGHT, Side.FRONT, Direction.RIGHT, !FLIPPED);
      addEdge(Side.FRONT, Direction.RIGHT, Side.RIGHT, Direction.DOWN, FLIPPED);
      addEdge(Side.RIGHT, Direction.DOWN, Side.BACK, Direction.LEFT, FLIPPED);

      addEdge(Side.BOTTOM, Direction.UP, Side.FRONT, Direction.UP, !FLIPPED);
      addEdge(Side.BOTTOM, Direction.RIGHT, Side.RIGHT, Direction.RIGHT, !FLIPPED);
      addEdge(Side.BOTTOM, Direction.LEFT, Side.LEFT, Direction.UP, FLIPPED);
      addEdge(Side.BOTTOM, Direction.DOWN, Side.BACK, Direction.UP, FLIPPED);

      addBoundarySides(Direction.RIGHT, Side.TOP, Side.FRONT, Side.RIGHT);
      addBoundarySides(Direction.UP, Side.BACK, Side.LEFT, Side.TOP, Side.RIGHT);
      addBoundarySides(Direction.LEFT, Side.TOP, Side.BACK, Side.BOTTOM);
      addBoundarySides(Direction.DOWN, Side.BACK, Side.LEFT, Side.BOTTOM, Side.RIGHT);
    }
  }

  /**
   * [ ][T][R]
   * [ ][F][ ]
   * [L][B][ ]
   * [K][ ][ ]
   */
  private static class InputCubeTeleporter extends CubeTeleporter {
    InputCubeTeleporter(int sideLength) {
      super(sideLength);
      addSide(Side.TOP, 0, 1);
      addSide(Side.RIGHT, 0, 2);
      addSide(Side.FRONT, 1, 1);
      addSide(Side.BOTTOM, 2, 1);
      addSide(Side.LEFT, 2, 0);
      addSide(Side.BACK, 3, 0);

      addEdge(Side.TOP, Direction.RIGHT, Side.RIGHT, Direction.RIGHT, !FLIPPED);
      addEdge(Side.TOP, Direction.DOWN, Side.FRONT, Direction.DOWN, !FLIPPED);
      addEdge(Side.TOP, Direction.LEFT, Side.LEFT, Direction.RIGHT, FLIPPED);
      addEdge(Side.TOP, Direction.UP, Side.BACK, Direction.RIGHT, !FLIPPED);

      addEdge(Side.FRONT, Direction.RIGHT, Side.RIGHT, Direction.UP, !FLIPPED);
      addEdge(Side.FRONT, Direction.LEFT, Side.LEFT, Direction.DOWN, !FLIPPED);
      addEdge(Side.LEFT, Direction.DOWN, Side.BACK, Direction.DOWN, !FLIPPED);
      addEdge(Side.RIGHT, Direction.UP, Side.BACK, Direction.UP, !FLIPPED);

      addEdge(Side.BOTTOM, Direction.UP, Side.FRONT, Direction.UP, !FLIPPED);
      addEdge(Side.BOTTOM, Direction.RIGHT, Side.RIGHT, Direction.LEFT, FLIPPED);
      addEdge(Side.BOTTOM, Direction.LEFT, Side.LEFT, Direction.LEFT, !FLIPPED);
      addEdge(Side.BOTTOM, Direction.DOWN, Side.BACK, Direction.LEFT, !FLIPPED);

      addBoundarySides(Direction.RIGHT, Side.RIGHT, Side.FRONT, Side.BACK, Side.BOTTOM);
      addBoundarySides(Direction.UP, Side.LEFT, Side.TOP, Side.RIGHT);
      addBoundarySides(Direction.LEFT, Side.TOP, Side.FRONT, Side.LEFT, Side.BACK);
      addBoundarySides(Direction.DOWN, Side.BACK, Side.BOTTOM, Side.RIGHT);
    }
  }

  private static class Cube extends Board {

    private final CubeTeleporter teleporter;

    Cube(List<String> lines, String instruction) {
      super(lines, instruction);

      int sideLength = lines.stream().map(String::trim).mapToInt(String::length).min().orElse(0);
      teleporter = TELEPORTER.apply(sideLength);
    }

    @Override
    Optional<Teleport> computeTeleport(int row, int col, Direction direction) {
      return teleporter.computeTeleport(row, col, direction);
    }

    @Override
    void print() {
      if (!DEBUG) {
        return;
      }
      super.print();
      try {
        System.in.read();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static void main(String[] args) throws Exception {
    List<String> lines = InputHelper.linesFromResource(INPUT).collect(Collectors.toList());

    String instruction = lines.get(lines.size() - 1);

    // Part 1.
    Board board = new Board(lines.subList(0, lines.size() - 2), instruction);
    board.navigate();

    System.out.println("Part 1: " + computeFinalPositionAndFacing(board));

    // Part 2.
    Cube cube = new Cube(lines.subList(0, lines.size() - 2), instruction);
    cube.navigate();
    // 35361 too low
    // 132144 too low
    System.out.println("Part 2: " + computeFinalPositionAndFacing(cube));
  }
}
