package aoc2022.day21;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/21}. */
public class MonkeyMath {

  private static final String INPUT = "aoc2022/day21/input.txt";

  public static void main(String[] args) throws Exception {
    Map<String, Monkey> monkeys = InputHelper.linesFromResource(INPUT)
        .map(Monkey::parse)
        .collect(Collectors.toMap(Monkey::id, m -> m));

    System.out.println("Part 1: " + yell(monkeys, "root"));
  }

  private static long yell(Map<String, Monkey> monkeys, String id) {
    Monkey monkey = monkeys.get(id);
    if (monkey.job == Job.NUMBER) {
      return monkey.number();
    }

    long left = yell(monkeys, monkey.left());
    long right = yell(monkeys, monkey.right());
    return monkey.function().apply(left, right);
  }

  private static enum Job {
    NUMBER,
    OPERATION,
  }

  private static abstract class Monkey {
    private static final Pattern NUMBER_REGEX = Pattern.compile("^([a-z]+):\\ (\\d+)$");
    private static final Pattern OPERATION_REGEX = Pattern
        .compile("^([a-z]+):\\ ([a-z]+)\\ ([\\+\\-\\*\\/])\\ ([a-z]+)$");

    final String id;
    final Job job;

    Monkey(String id, Job job) {
      this.id = id;
      this.job = job;
    }

    static Monkey parse(String line) {
      Matcher m = NUMBER_REGEX.matcher(line);
      if (m.matches()) {
        return new NumberMonkey(m.group(1), Integer.parseInt(m.group(2)));
      }

      m = OPERATION_REGEX.matcher(line);
      if (!m.matches()) {
        throw new IllegalArgumentException();
      }
      return new OperationMonkey(m.group(1), m.group(2), m.group(4),
          OperationMonkey.FUNCTIONS.get(m.group(3).charAt(0)));
    }

    String id() {
      return id;
    }

    int number() {
      throw new UnsupportedOperationException();
    }

    String left() {
      throw new UnsupportedOperationException();
    }

    String right() {
      throw new UnsupportedOperationException();
    }

    BiFunction<Long, Long, Long> function() {
      throw new UnsupportedOperationException();
    }
  }

  private static class NumberMonkey extends Monkey {
    private final int number;

    NumberMonkey(String id, int number) {
      super(id, Job.NUMBER);
      this.number = number;
    }

    @Override
    int number() {
      return number;
    }
  }

  private static class OperationMonkey extends Monkey {
    private static final Map<Character, BiFunction<Long, Long, Long>> FUNCTIONS = Map.of(
        '+', (left, right) -> left + right,
        '-', (left, right) -> left - right,
        '*', (left, right) -> left * right,
        '/', (left, right) -> left / right);

    private final String left;
    private final String right;
    private final BiFunction<Long, Long, Long> function;

    OperationMonkey(String id, String left, String right, BiFunction<Long, Long, Long> function) {
      super(id, Job.OPERATION);
      this.left = left;
      this.right = right;
      this.function = function;
    }

    @Override
    String left() {
      return left;
    }

    @Override
    String right() {
      return right;
    }

    @Override
    BiFunction<Long, Long, Long> function() {
      return function;
    }
  }
}
