package aoc2022.day11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/11}. */
public class MonkeyInTheMiddle {

  private static final String INPUT = "aoc2022/day11/input.txt";

  public static void main(String[] args) throws Exception {
    Iterator<String> input = InputHelper.linesFromResource(INPUT).iterator();
    List<Monkey> monkeys = new ArrayList<>();
    while (input.hasNext()) {
      monkeys.add(Monkey.parse(input));
      if (input.hasNext()) {
        input.next();
      }
    }

    // System.out.println(monkeys);
    // System.out.println(Monkey.ITEMS);

  }

  private static record Monkey(int id, Operation operation, Test test, Condition trueCondition, Condition falseCondition) {   
    private static final Map<Integer, LinkedList<Integer>> ITEMS = new HashMap<>();
    private static final Map<Integer, AtomicInteger> INSPECTION_COUNT = new HashMap<>();

    // Monkey 3:
    private static final Pattern ID_PATTERN = Pattern.compile("^Monkey\\ (\\d+):$");

    // Starting items: 54, 65, 75, 74
    private static final Pattern STARTING_ITEMS_PATTERN = Pattern.compile("^\\ *Starting\\ items:\\ (.+)$");

    private static Monkey parse(Iterator<String> iterator) {
      String line = iterator.next();
      Matcher m = ID_PATTERN.matcher(line);
      if (!m.matches()) {
        throw new IllegalArgumentException(line);
      }
      int id = Integer.parseInt(m.group(1));

      LinkedList<Integer> items = new LinkedList<>();
      line = iterator.next();
      m = STARTING_ITEMS_PATTERN.matcher(line);
      if (!m.matches()) {
        throw new IllegalArgumentException(line);
      }
      Arrays.stream(m.group(1).split(", ")).map(Integer::parseInt).forEach(items::add);

      ITEMS.put(id, items);

      return new Monkey(
          id,
          Operation.parse(iterator.next()),
          Test.parse(iterator.next()),
          Condition.parse(iterator.next()),
          Condition.parse(iterator.next()));
    }

    private void takeTurn() {
      Iterator<Integer> iterator = ITEMS.get(id).iterator();
      while (iterator.hasNext()) {
        int item = iterator.next();
      }
    }
  }

  private static record Operation(String left, char op, String right) {

    // Operation: new = old * 19
    // Operation: new = old + 6
    // Operation: new = old * old
    private static final Pattern PATTERN = Pattern
        .compile("^\\ *Operation:\\ new\\ =\\ ([a-z0-9]+)\\ ([\\+\\*])\\ ([a-z0-9]+)$");

    private static Operation parse(String line) {
      Matcher m = PATTERN.matcher(line);
      if (!m.matches()) {
        throw new IllegalArgumentException(line);
      }
      return new Operation(m.group(1), m.group(2).charAt(0), m.group(3));
    }

    private long apply(long old) {
      long eqLeft = left.equals("old") ? old : Long.parseLong(left);
      long eqRight = right.equals("old") ? old : Long.parseLong(right);
      if (op == '+') {
        return eqLeft + eqRight;
      } else if (op == '*') {
        return eqLeft * eqRight;
      } else {
        throw new UnsupportedOperationException();
      }
    }
  }

  private static record Test(int divisibleBy) {

    // Test: divisible by 23
    private static final Pattern PATTERN = Pattern.compile("^\\ *Test:\\ divisible\\ by\\ (\\d+)$");

    private static Test parse(String line) {
      Matcher m = PATTERN.matcher(line);
      if (!m.matches()) {
        throw new IllegalArgumentException(line);
      }
      return new Test(Integer.parseInt(m.group(1)));
    }

    private boolean apply(long input) {
      return (input % divisibleBy) == 0;
    }
  }

  private static record Condition(boolean expected, int monkeyToThrowTo) {
    // If true: throw to monkey 0
    // If false: throw to monkey 1
    private static final Pattern PATTERN = Pattern
        .compile("^\\ *If\\ ([a-z]+):\\ throw\\ to\\ monkey\\ (\\d+)$");

    private static Condition parse(String line) {
      Matcher m = PATTERN.matcher(line);
      if (!m.matches()) {
        throw new IllegalArgumentException(line);
      }
      return new Condition(Boolean.parseBoolean(m.group(1)), Integer.parseInt(m.group(2)));
    }
  }
}
