package aoc2023.day02;

import aoccommon.InputHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Solution for {@link https://adventofcode.com/2023/day/2}.
 */
public class CubeConundrum {

  private static final String INPUT = "aoc2023/day02/input.txt";

  private static class Game {
    private final int number;
    private final List<Map<String, Integer>> reveals;

    private Game(int number, List<Map<String, Integer>> reveals) {
      this.number = number;
      this.reveals = reveals;
    }

    static Game parse(String line) {
      int number = Integer.parseInt(line.split(": ")[0].split(" ")[1]);
      List<Map<String, Integer>> reveals = Arrays.stream(line.split(": ")[1].split("; "))
          .map(reveal -> Arrays.stream(reveal.split(", "))
              .collect(Collectors.toMap(
                  s -> s.split(" ")[1],
                  s -> Integer.parseInt(s.split(" ")[0]))))
          .toList();
      return new Game(number, reveals);
    }

    int getNumber() {
      return number;
    }
  }

  public static void main(String[] args) throws Exception {
    // Part 1.
    List<Game> games = InputHelper.linesFromResource(INPUT).map(Game::parse).collect(Collectors.toList());
    Map<String, Integer> constraints = Map.of("red", 12, "green", 13, "blue", 14);
    long sum = games.stream()
        .filter(g -> g.reveals.stream()
            .allMatch(r -> constraints.entrySet().stream()
                .allMatch(c -> !r.containsKey(c.getKey()) || c.getValue() >= r.get(c.getKey()))))
        .mapToInt(Game::getNumber)
        .sum();
    System.out.println("Part 1: " + sum);

    // Part 2.
    sum = games.stream()
        .mapToLong(g -> {
          Map<String, Integer> maxCubes = new HashMap<>();
          g.reveals.forEach(reveal -> reveal.forEach((cube, count) -> {
            Integer prev = maxCubes.get(cube);
            if (prev == null || count > prev) {
              maxCubes.put(cube, count);
            }
          }));
          return maxCubes.values().stream().mapToLong(c -> c).reduce((left, right) -> left * right).getAsLong();
        })
        .sum();
    System.out.println("Part 2: " + sum);
  }
}
