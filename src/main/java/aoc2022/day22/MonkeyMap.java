package aoc2022.day22;

import java.util.List;
import java.util.stream.Collectors;

import aoccommon.InputHelper;
import aoccommon.Point;

/** Solution for {@link https://adventofcode.com/2022/day/22}. */
public class MonkeyMap {

  private static final String INPUT = "aoc2022/day22/input.txt";

  private static enum Direction {
    RIGHT(1, 0),
    DOWN(0, 1),
    LEFT(-1, 0),
    UP(0, -1);

    private final int xd;
    private final int yd;

    Direction(int xd, int yd) {
      this.xd = xd;
      this.yd = yd;
    }
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

    FinalPosition navigate() {
      while (i < instruction.length()) {
        char c = instruction.charAt(i++);
        if (Character.isDigit(c)) {
          int steps = c - '0';
          while (Character.isDigit(c = instruction.charAt(i))) {
            i++;
            steps = (steps * 10) + (c - '0');
          }

        } else {
          turn(c);
        }
      }
    }

    private void walk(int steps) {
      for (int s = 0; s < steps; s++) {
        row = row + dir.yd;
        col = col + dir.xd;
        // Check for walls or off end of map.
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
  }

  private static record FinalPosition(Point point, Direction direction) {
  }

  public static void main(String [] args) throws Exception {
    List<String> lines = InputHelper.linesFromResource(INPUT).collect(Collectors.toList());

    String instruction = lines.get(lines.size() - 1);
    Board board = Board.create(lines.subList(0, lines.size() - 2), instruction);
  }
}
