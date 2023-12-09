package aoc2023.day08;

import aoccommon.Debug;
import aoccommon.InputHelper;
import aoccommon.MoreMath;
import aoccommon.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Solution for {@link https://adventofcode.com/2023/day/8}.
 */
public class HauntedWasteland {

  private static final Pattern REGEX =
      Pattern.compile("^([A-Z0-9]{3})\\s+=\\s+\\(([A-Z0-9]{3}),\\s+([A-Z0-9]{3})\\)$");

  private static final String INPUT = "aoc2023/day08/input.txt";
  private static final String EXAMPLE = "aoc2023/day08/example.txt";

  private record Cycle(String node, long step, int instruction, long length) {
  }

  public static void main(String[] args) throws Exception {
    // Debug.enablePrint();
    List<String> lines = InputHelper.linesFromResource(INPUT).toList();
    String instructions = lines.get(0);
    Map<String, Pair<String, String>> nodes = new HashMap<>();
    for (int i = 1; i < lines.size(); i++) {
      if (lines.get(i).isBlank()) {
        continue;
      }
      Matcher m = REGEX.matcher(lines.get(i));
      if (!m.matches()) {
        throw new IllegalArgumentException();
      }
      nodes.put(m.group(1), Pair.of(m.group(2), m.group(3)));
    }

    // Part 1.
    int steps = 0;
    String current = "AAA";
    while (true) {
      char c = instructions.charAt(steps++ % instructions.length());
      Pair<String, String> next = nodes.get(current);
      current = c == 'L' ? next.first() : next.second();
      if (current.equals("ZZZ")) {
        break;
      }
    }
    System.out.println("Part 1: " + steps);

    // Part 2.
    List<String> ghosts = nodes.keySet().stream().filter(g -> g.endsWith("A")).toList();

    // Assuming the ghosts hit a terminal node on a fixed frequency (seems to be true from input inspection).
    long[] frequencies = new long[ghosts.size()];
    for (int i = 0; i < ghosts.size(); i++) {
      String g = ghosts.get(i);
      long step = 0;
      current = g;
      while (true) {
        int index = (int) (step++ % instructions.length());
        char c = instructions.charAt(index);
        Pair<String, String> next = nodes.get(current);
        current = c == 'L' ? next.first() : next.second();
        if (current.endsWith("Z")) {
          break;
        }
      }
      frequencies[i] = step;
    }
    Debug.println("frequencies=%s", Arrays.toString(frequencies));
    long lcm = MoreMath.lcm(frequencies);
    System.out.println("Part 2: " + lcm);
  }
}
