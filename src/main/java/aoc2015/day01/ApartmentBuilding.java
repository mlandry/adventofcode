package aoc2015.day01;

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2015/day/1}. */
public class ApartmentBuilding {

  private static final String INPUT = "aoc2015/day01/input.txt";

  public static void main(String[] args) throws Exception {
    String input = InputHelper.linesFromResource(INPUT).findFirst().get();
    int floor = 0;
    int firstBasementPos = -1;
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      floor += c == '(' ? 1 : -1;
      if (floor < 0 && firstBasementPos == -1) {
        firstBasementPos = i + 1;
      }
    }
    System.out.println("Part 1: " + floor);
    System.out.println("Part 2: " + firstBasementPos);
  }
}
