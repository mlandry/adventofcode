package aoc2022.day05;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/5}. */
public class SupplyStacks {

  private static final Pattern LABEL_PATTERN = Pattern.compile("^\\ *\\d+\\ *.*$");
  private static final Pattern INSTRUCTION_PATTERN = Pattern.compile("^move (\\d+) from (\\d+) to (\\d+)$");

  private static final String INPUT = "aoc2022/day05/input.txt";

  public static void main(String[] args) throws Exception {
    List<String> lines = InputHelper.linesFromResource(INPUT).collect(Collectors.toList());

    int i = 0;
    String line;
    do {
      line = lines.get(i++);
    } while (!LABEL_PATTERN.matcher(line).matches());
    // Stack number -> position in line (for parsing initial state).
    Map<Integer, Integer> stackIndex = new HashMap<>();
    for (int j = 0; j < line.length(); j++) {
      char c = line.charAt(j);
      if (Character.isWhitespace(c)) {
        continue;
      }
      stackIndex.put(c - '0', j);
    }
    List<Instruction> instructions = lines.stream().skip(i + 1).map(Instruction::parse).collect(Collectors.toList());

    // Part 1.
    Map<Integer, Deque<Character>> stacks = initializeStacks(lines, i - 2, stackIndex);
    for (Instruction instruction : instructions) {
      for (int n = 0; n < instruction.number; n++) {
        stacks.get(instruction.to).push(stacks.get(instruction.from).pop());
      }
    }
    System.out.println("Part 1: " + getTopOfStackString(stacks));

    // Part 2.
    stacks = initializeStacks(lines, i - 2, stackIndex);
    for (Instruction instruction : instructions) {
      Deque<Character> staging = new ArrayDeque<>();
      for (int n = 0; n < instruction.number; n++) {
        staging.push(stacks.get(instruction.from).pop());
      }
      for (int n = 0; n < instruction.number; n++) {
        stacks.get(instruction.to).push(staging.pop());
      }
    }
    System.out.println("Part 2: " + getTopOfStackString(stacks));
  }

  private static Map<Integer, Deque<Character>> initializeStacks(List<String> lines, int start,
      Map<Integer, Integer> stackIndex) {
    Map<Integer, Deque<Character>> stacks = new HashMap<>();
    for (int j = start; j >= 0; j--) {
      String line = lines.get(j);
      for (Map.Entry<Integer, Integer> entry : stackIndex.entrySet()) {
        char c = line.charAt(entry.getValue());
        if (Character.isWhitespace(c)) {
          continue;
        }
        stacks.computeIfAbsent(entry.getKey(), k -> new ArrayDeque<>()).push(c);
      }
    }
    return stacks;
  }

  private static String getTopOfStackString(Map<Integer, Deque<Character>> stacks) {
    return IntStream.range(1, stacks.size() + 1)
        .mapToObj(n -> stacks.get(n).peek().toString())
        .collect(Collectors.joining());
  }

  private static record Instruction(int number, int from, int to) {
    public static Instruction parse(String line) {
      Matcher matcher = INSTRUCTION_PATTERN.matcher(line);
      if (!matcher.matches()) {
        throw new IllegalArgumentException();
      }
      return new Instruction(
          Integer.parseInt(matcher.group(1)),
          Integer.parseInt(matcher.group(2)),
          Integer.parseInt(matcher.group(3)));
    }
  }
}
