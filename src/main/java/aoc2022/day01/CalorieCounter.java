package aoc2022.day01;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/1}. */
class CalorieCounter {

  private static final String INPUT = "aoc2022/day01/input.txt";

  public static void main(String[] args) throws Exception {
    List<String> lines = InputHelper.linesFromResource(INPUT).collect(Collectors.toList());

    List<List<Integer>> elves = new ArrayList<>();
    List<Integer> calories = new ArrayList<>();
    for (String line : lines) {
      if (line.isEmpty()) {
        elves.add(calories);
        calories = new ArrayList<>();
        continue;
      }
      calories.add(Integer.parseInt(line));
    }

    List<Integer> totalCalories = elves.stream().map(cals -> cals.stream().mapToInt(c -> c).sum()).collect(Collectors.toList());
    int mostCalories = totalCalories.stream().mapToInt(t -> t).max().getAsInt();
    System.out.println("Part 1: " + mostCalories);

    List<Integer> topThree = totalCalories.stream().sorted((t1, t2) -> Integer.compare(t2, t1)).limit(3).collect(Collectors.toList());
    int topThreeSum = topThree.stream().mapToInt(t -> t).sum();
    System.out.println("Part 2: " + topThreeSum);
  }
}
