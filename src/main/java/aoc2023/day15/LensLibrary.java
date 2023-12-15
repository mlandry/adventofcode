package aoc2023.day15;

import aoccommon.Debug;
import aoccommon.InputHelper;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Solution for {@link https://adventofcode.com/2023/day/15}.
 */
public class LensLibrary {

  private static final String INPUT = "aoc2023/day15/input.txt";
  private static final String EXAMPLE = "aoc2023/day15/example.txt";

  private static final Pattern REGEX = Pattern.compile("^(\\w+)([\\-=])(\\d?)$");

  public static void main(String[] args) throws Exception {
    // Debug.enablePrint();
    String input = INPUT;

    // Part 1.
    final ToIntFunction<String> hasher = (step) ->
        step.chars().reduce(0, (current, c) -> ((current + c) * 17) % 256);
    int sum = InputHelper.linesFromResource(input)
        .map(line -> line.split(","))
        .flatMap(Arrays::stream)
        .mapToInt(hasher)
        .sum();
    System.out.println("Part 1: " + sum);

    // Part 2.
    final LinkedHashMap<String, Integer>[] boxes = new LinkedHashMap[256];
    IntStream.range(0, 256).forEach(i -> boxes[i] = new LinkedHashMap<>());
    InputHelper.linesFromResource(input)
        .map(line -> line.split(","))
        .flatMap(Arrays::stream)
        .forEach(step -> {
          Matcher m = REGEX.matcher(step);
          if (!m.matches()) {
            throw new IllegalArgumentException();
          }
          String label = m.group(1);
          int box = hasher.applyAsInt(label);
          switch (m.group(2)) {
            case "-":
              boxes[box].remove(label);
              break;
            case "=":
              int focalLength = Integer.parseInt(m.group(3));
              boxes[box].compute(label, (l, old) -> focalLength);
              break;
            default:
              throw new IllegalArgumentException();
          }
          Debug.println("After %s:", step);
          IntStream.range(0, 256)
              .filter(i -> !boxes[i].isEmpty())
              .forEach(i -> {
                Debug.println("Box %d: %s", i, boxes[i].entrySet().stream()
                    .map(e -> String.format("[%s %d]", e.getKey(), e.getValue()))
                    .collect(Collectors.joining(" ")));
              });
        });

    long power = IntStream.range(0, 256)
        .mapToLong(box -> {
          long boxPower = 0;
          long slot = 1;
          Iterator<Integer> iter = boxes[box].values().iterator();
          while (iter.hasNext()) {
            boxPower += (box + 1) * slot++ * iter.next();
          }
          return boxPower;
        })
        .sum();
    System.out.println("Part 2: " + power);
  }
}
