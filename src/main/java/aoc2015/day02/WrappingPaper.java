package aoc2015.day02;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import aoccommon.InputHelper;
import aoccommon.Subsets;

/** Solution for {@link https://adventofcode.com/2015/day/2}. */
public class WrappingPaper {

  private static final String INPUT = "aoc2015/day02/input.txt";

  public static void main(String[] args) throws Exception {
    List<List<Integer>> boxes = InputHelper.linesFromResource(INPUT)
        .map(s -> s.split("x"))
        .map(sp -> Arrays.stream(sp).map(Integer::parseInt).collect(Collectors.toList()))
        .collect(Collectors.toList());

    // Wrapping paper needed per box = area of each side + area of the smallest side
    // for slack.
    long wrappingPaperNeeded = boxes.stream()
        .mapToLong(box -> {
          List<Integer> areas = Subsets.getAllSubsets(box, 2)
              .map(side -> side.stream().reduce(1, (a, b) -> a * b))
              .collect(Collectors.toList());
          return areas.stream().mapToInt(area -> 2 * area).sum()
              + areas.stream().mapToInt(area -> area).min().getAsInt();
        })
        .sum();
    System.out.println("Part 1: " + wrappingPaperNeeded);

    // Ribbon needed per box = minimum perimeter of box + volume of box (for bow).
    long ribbonNeeded = boxes.stream()
        .mapToLong(box -> Subsets.getAllSubsets(box, 2)
            .mapToInt(side -> side.stream().reduce(1, (a, b) -> (2 * a) + (2 * b)))
            .min().getAsInt() + box.stream().reduce(1, (a, b) -> a * b))
        .sum();
    System.out.println("Part 2: " + ribbonNeeded);
  }
}
