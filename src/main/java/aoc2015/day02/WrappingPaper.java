package aoc2015.day02;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2015/day/2}. */
public class WrappingPaper {

  private static final String INPUT = "aoc2015/day02/input.txt";

  public static void main(String [] args) throws Exception {
    List<int[]> boxes = InputHelper.linesFromResource(INPUT)
        .map(s -> s.split("x"))
        .map(sp -> Arrays.stream(sp).mapToInt(Integer::parseInt).toArray())
        .collect(Collectors.toList());

    long wrappingPaperNeeded = boxes.stream()
        .mapToLong(box -> {
          int area1 = box[0] * box[1];
          int area2 = box[0] * box[2];
          int area3 = box[1] * box[2];
          return (2 * area1) + (2 * area2) + (2 * area3) + Math.min(area1, Math.min(area2, area3));
        })
        .sum();
    System.out.println("Part 1: " + wrappingPaperNeeded);
  }
}
