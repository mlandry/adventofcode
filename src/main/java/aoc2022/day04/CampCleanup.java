package aoc2022.day04;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import aoccommon.InputHelper;
import com.google.common.collect.Range;

/** Solution for {@link https://adventofcode.com/2022/day/4}. */
public class CampCleanup {
  private static final String INPUT = "aoc2022/day04/input.txt";

  public static void main(String[] args) throws Exception {
    List<List<Range<Integer>>> assignments = InputHelper.linesFromResource(INPUT)
        .map(CampCleanup::parse)
        .collect(Collectors.toList());

    long fullyContainedAssignmentPairs = assignments.stream()
        .filter(assignment -> assignment.get(0).encloses(assignment.get(1))
            || assignment.get(1).encloses(assignment.get(0)))
        .count();

    System.out.println("Part 1: " + fullyContainedAssignmentPairs);

    long overlappingAssignmentPairs = assignments.stream()
        .filter(assignment -> assignment.get(0).isConnected(assignment.get(1))
            || assignment.get(1).isConnected(assignment.get(0)))
        .count();

    System.out.println("Part 2: " + overlappingAssignmentPairs);
  }

  private static List<Range<Integer>> parse(String line) {
    return Arrays.stream(line.split(","))
        .map(CampCleanup::parseRange)
        .collect(Collectors.toList());
  }

  private static Range<Integer> parseRange(String range) {
    String[] split = range.split("-");
    return Range.closed(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
  }
}
