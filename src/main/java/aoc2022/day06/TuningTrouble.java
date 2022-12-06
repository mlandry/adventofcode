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
    int marker = findMarker(datastream, 4);
    System.out.println("Part 1: " + marker);

    marker = findMarker(datastream, 14);
    System.out.println("Part 2: " + marker);
  }

  private static boolean allDistinct(List<Character> buffer) {
    return new HashSet<>(buffer).size() == buffer.size();
  }

  private static int findMarker(String datastream, int length) {
    LinkedList<Character> buffer = new LinkedList<>();
    for (int i = 0; i < datastream.length(); i++) {
      char c = datastream.charAt(i);
      if (buffer.size() == length) {
        buffer.removeFirst();
      }
      buffer.addLast(c);
      if (buffer.size() == length && allDistinct(buffer)) {
        return i + 1;
      }
    }
    return -1;
  }
}
