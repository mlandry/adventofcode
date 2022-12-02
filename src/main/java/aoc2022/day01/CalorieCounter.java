package aoc2022.day01;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class CalorieCounter {
  public static void main(String[] args) throws Exception {
    List<String> lines = inputReader().lines().collect(Collectors.toList());

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

  private static BufferedReader inputReader() throws IOException {
    return new BufferedReader(
        new InputStreamReader(
            CalorieCounter.class.getClassLoader().getResourceAsStream("aoc2022/day01/input.txt")));
  }
}
