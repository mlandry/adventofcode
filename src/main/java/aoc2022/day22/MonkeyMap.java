package aoc2022.day22;

import java.util.List;
import java.util.stream.Collectors;

import aoccommon.InputHelper;
import aoccommon.Point;

/** Solution for {@link https://adventofcode.com/2022/day/22}. */
public class MonkeyMap {

  private static final String INPUT = "aoc2022/day22/input.txt";

  private static enum Direction {
    RIGHT,
    DOWN,
    LEFT,
    UP,
  }

  private static class Board {
    private final List<String> lines;
    private final String instruction;

    private int row = 0;
    private int col = 0;
    private Direction dir = Direction.RIGHT;
    private int i = 0;

    Board(List<String> lines, String instruction) {
      this.lines = lines;
      this.instruction = instruction;
    }

    static Board create(List<String> lines, String instruction) {
      Board board = new Board(lines, instruction);
      board.initialize();
      return board;
    }

    void initialize() {
      row = 0;
      col = startOfRow(0);
      dir = Direction.RIGHT;
      i = 0;
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

    protected void walk(int steps) {
      for (int s = 0; s < steps; s++) {
        int nextRow = row;
        int nextCol = col;
        switch (dir) {
          case RIGHT:
            nextCol++;
            if (nextCol >= lines.get(row).length() || Character.isWhitespace(lines.get(row).charAt(nextCol))) {
              nextCol = startOfRow(row);
            }
            break;
          case DOWN:
            nextRow++;
            if (nextRow >= lines.size() || col >= lines.get(nextRow).length()
                || Character.isWhitespace(lines.get(nextRow).charAt(col))) {
              nextRow = startOfCol(col);
            }
            break;
          case LEFT:
            nextCol--;
            if (nextCol < 0 || Character.isWhitespace(lines.get(row).charAt(nextCol))) {
              nextCol = endOfRow(row);
            }
            break;
          case UP:
            nextRow--;
            if (nextRow < 0 || col >= lines.get(nextRow).length()
                || Character.isWhitespace(lines.get(nextRow).charAt(col))) {
              nextRow = endOfCol(col);
            }
            break;
          default:
            throw new IllegalStateException();
        }
        if (lines.get(nextRow).charAt(nextCol) == '#') {
          break;
        }

        row = nextRow;
        col = nextCol;
      }
    }

    private void turn(char c) {
      if (c == 'R') {
        dir = dir.values()[(dir.ordinal() + 1) % dir.values().length];
      } else if (dir.ordinal() == 0) {
        dir = dir.values()[dir.values().length - 1];
      } else {
        dir = dir.values()[dir.ordinal() - 1];
      }
    }

    private int startOfRow(int row) {
      return Math.min(lines.get(row).indexOf('#'), lines.get(row).indexOf('.'));
    }

    private int endOfRow(int row) {
      return Math.max(lines.get(row).lastIndexOf('#'), lines.get(row).lastIndexOf('.'));
    }

    private int startOfCol(int col) {
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

    private int endOfCol(int col) {
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
  }

  private static class Cube extends Board {
    Cube(List<String> lines, String instruction) {
      super(lines, instruction);
    }

    static Cube create(List<String> lines, String instruction) {
      Cube cube = new Cube(lines, instruction);
      cube.initialize();
      return cube;
    }

    @Override
    void initialize() {
      super.initialize();
    }
    
    @Override
    protected void walk(int steps) {

    }
  }

  public static void main(String[] args) throws Exception {
    List<String> lines = InputHelper.linesFromResource(INPUT).collect(Collectors.toList());

    String instruction = lines.get(lines.size() - 1);

    // Part 1.
    Board board = Board.create(lines.subList(0, lines.size() - 2), instruction);
    board.navigate();

    long row = board.row + 1;
    long col = board.col + 1;
    long facing = board.dir.ordinal();
    long sum = (row * 1000L) + (col * 4L) + facing;
    System.out.println("Part 1: " + sum);
  }
}
