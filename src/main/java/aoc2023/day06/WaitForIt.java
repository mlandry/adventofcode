package aoc2023.day06;

import aoccommon.InputHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Solution for {@link https://adventofcode.com/2023/day/6}. */
public class WaitForIt {

  private static final String INPUT = "aoc2023/day06/input.txt";
  private static final String EXAMPLE = "aoc2023/day06/example.txt";

  public static void main(String [] args) throws Exception {
    List<String> lines = InputHelper.linesFromResource(INPUT).toList();
    if (!lines.get(0).startsWith("Time:")) {
      throw new IllegalArgumentException("Expected first line to contain times");
    }
    int[] times = Arrays.stream(lines.get(0).split(":\\s+")[1].split("\\s+"))
        .mapToInt(Integer::parseInt)
        .toArray();

    if (!lines.get(1).startsWith("Distance:")) {
      throw new IllegalArgumentException("Expected second line to contain distances");
    }
    int[] distances = Arrays.stream(lines.get(1).split(":\\s+")[1].split("\\s+"))
        .mapToInt(Integer::parseInt)
        .toArray();

    if (times.length != distances.length) {
      throw new IllegalArgumentException("expected equal number of times and distances");
    }

    // Part 1.
    long result = IntStream.range(0, times.length)
        .mapToLong(i -> {
          int time = times[i];
          int dist = distances[i];

          int wins = 0;
          for (int t = 0; t <= time; t++) {
            int boat = (time - t) * t;
            if (boat > dist) {
              wins++;
            }
          }
          return wins;
        })
        .reduce(1L, (a, b) -> a * b);
    System.out.println("Part 1: " + result);

    // Part 2.
    long time = Long.parseLong(Arrays.stream(times)
        .mapToObj(Integer::toString)
        .collect(Collectors.joining("")));
    long distance = Long.parseLong(Arrays.stream(distances)
        .mapToObj(Integer::toString)
        .collect(Collectors.joining("")));

    long wins = 0;
    for (long t = 0; t <= time; t++) {
      long boat = (time - t) * t;
      if (boat > distance) {
        wins++;
      }
    }
    System.out.println("Part 2: " + wins);
  }
}
