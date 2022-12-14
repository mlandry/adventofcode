package aoc2015.day03;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import aoccommon.InputHelper;
import aoccommon.Point;

/** Solution for {@link https://adventofcode.com/2015/day/3}. */
public class SphericalHouses {

  private static final String INPUT = "aoc2015/day03/input.txt";

  private static record Move(int xd, int yd) {
  }

  private static Map<Character, Move> MOVES = Map.of(
      '^', new Move(0, -1),
      'v', new Move(0, 1),
      '<', new Move(-1, 0),
      '>', new Move(1, 0));

  public static void main(String [] args) throws Exception {
    String input = InputHelper.linesFromResource(INPUT).findFirst().get();
    
    Point current = Point.of(0, 0);
    Set<Point> visited = new HashSet<>();
    visited.add(current);

    for (char c : input.toCharArray()) {
      Move move = MOVES.get(c);
      current = Point.of(current.getX() + move.xd, current.getY() + move.yd);
      visited.add(current);
    }

    System.out.println("Part 1: " + visited.size());

    visited.clear();
    Point santa = Point.of(0, 0);
    Point robo = Point.of(0, 0);
    visited.add(santa);

    for (int i = 0; i < input.length(); i++) {
      Move move = MOVES.get(input.charAt(i));
      if (i % 2 == 0) {
        santa = Point.of(santa.getX() + move.xd, santa.getY() + move.yd);
        visited.add(santa);
      } else {
        robo = Point.of(robo.getX() + move.xd, robo.getY() + move.yd);
        visited.add(robo);
      }
    }

    System.out.println("Part 2: " + visited.size());
  }
}
