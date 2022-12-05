package aoc2022.day05;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    Map<Integer, Deque<Character>> stacks = new HashMap<>();
    for (int j = i - 2; j >= 0; j--) {
      line = lines.get(j);
      for (Map.Entry<Integer, Integer> entry : stackIndex.entrySet()) {
        char c = line.charAt(entry.getValue());
        if (Character.isWhitespace(c)) {
          continue;
        }
        stacks.computeIfAbsent(entry.getKey(), k -> new ArrayDeque<>()).push(c);
      }
    }

    List<Instruction> instructions = lines.stream().skip(i + 1).map(Instruction::parse).collect(Collectors.toList());

    
  }

  private static class Instruction {
    private final int number;
    private final int from;
    private final int to;

    Instruction(int number, int from, int to) {
      this.number = number;
      this.from = from;
      this.to = to;
    }

    static Instruction parse(String line) {
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
