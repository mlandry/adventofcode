package aoc2022.day06;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/6}. */
public class TuningTrouble {

  private static final String INPUT = "aoc2022/day06/input.txt";

  public static void main(String [] args) throws Exception {
    String datastream = InputHelper.linesFromResource(INPUT).findFirst().get();
    LinkedList<Character> buffer = new LinkedList<>();

    int i = 0;
    for (; i < datastream.length(); i++) {
      char c = datastream.charAt(i);
      if (buffer.size() == 4) {
        buffer.removeFirst();
      }
      buffer.addLast(c);
      if (buffer.size() == 4 && allDistinct(buffer)) {
        break;
      }
    }
    int marker = i + 1;
    System.out.println("Part 1: " + marker);
  }

  private static boolean allDistinct(List<Character> buffer) {
    return new HashSet<>(buffer).size() == buffer.size();
  }
}
