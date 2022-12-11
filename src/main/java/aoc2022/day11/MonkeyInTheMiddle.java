package aoc2022.day11;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Solution for {@link https://adventofcode.com/2022/day/11}. */
public class MonkeyInTheMiddle {

  private static final String INPUT = "aoc2022/day11/input.txt";

  public static void main(String[] args) throws Exception {
  }

  private static record Monkey(int id, LinkedList<Integer> items, Operation operation, Test test,
      Condition trueCondition, Condition falseCondition) {

    private static final Pattern ID_PATTERN = Pattern.compile("^Monkey\\ (\\d+):$");

    // Starting items: 54, 65, 75, 74
    private static final Pattern STARTING_ITEMS_PATTERN = Pattern.compile("^\\ *Starting\\ items:\\ (.+)$");

    private static Monkey parse(Iterator<String> iterator) {
      Matcher m = ID_PATTERN.matcher(iterator.next());
      if (!m.matches()) {
        throw new IllegalArgumentException();
      }
      int id = Integer.parseInt(m.group(1));

      LinkedList<Integer> items = new LinkedList<>();
      m = STARTING_ITEMS_PATTERN.matcher(iterator.next());
      if (!m.matches()) {
        throw new IllegalArgumentException();
      }
      Arrays.stream(m.group(1).split(", ")).map(Integer::parseInt).forEach(items::add);

      return new Monkey(
          id,
          items,
          Operation.parse(iterator.next()),
          Test.parse(iterator.next()),
          Condition.parse(iterator.next()),
          Condition.parse(iterator.next()));
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
        throw new IllegalArgumentException();
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
    private static final Pattern PATTERN = Pattern.compile("^\\ *Test:\\ divisble by (\\d+)$");

    private static Test parse(String line) {
      Matcher m = PATTERN.matcher(line);
      if (!m.matches()) {
        throw new IllegalArgumentException();
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
        throw new IllegalArgumentException();
      }
      return new Condition(Boolean.getBoolean(m.group(1)), Integer.parseInt(m.group(2));
    }
  }
}
