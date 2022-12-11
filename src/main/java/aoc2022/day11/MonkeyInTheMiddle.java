package aoc2022.day11;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/11}. */
public class MonkeyInTheMiddle {

  private static final String INPUT = "aoc2022/day11/input.txt";

  private static final boolean DEBUG = false;

  private static void debug(String fmt, Object... args) {
    if (DEBUG) {
      System.out.println(String.format(fmt, args));
    }
  }

  public static void main(String[] args) throws Exception {
    Iterator<String> input = InputHelper.linesFromResource(INPUT).iterator();
    List<Monkey> monkeys = new ArrayList<>();
    while (input.hasNext()) {
      monkeys.add(Monkey.parse(input));
      if (input.hasNext()) {
        input.next();
      }
    }

    debug("Parsed monkeys: %s", monkeys);
    debug("Starting items: %s", Monkey.ITEMS);

    // Part 1.
    Monkey.reset();
    for (int i = 0; i < 20; i++) {
      monkeys.forEach(monkey -> monkey.takeTurn(worry -> worry.divide(BigInteger.valueOf(3))));
    }
    System.out.println("Part 1: " + Monkey.getMonkeyBusiness());

    // Part 2.
    Monkey.reset();
    // Use a ~LCM for all the test divisors to keep the worry from exploding while maintaing properties.
    // (Or just multiply them all together I guess.)
    BigInteger reducer = monkeys.stream()
        .map(Monkey::test)
        .map(Test::divisibleBy)
        .reduce((a, b) -> a.multiply(b))
        .get();

    for (int i = 0; i < 10000; i++) {
      monkeys.forEach(monkey -> monkey.takeTurn(worry -> worry.mod(reducer)));
    }

    System.out.println("Part 2: " + Monkey.getMonkeyBusiness());
  }

  private static record Monkey(int id, Operation operation, Test test, Condition trueCondition,
      Condition falseCondition) {

    private static final Map<Integer, LinkedList<BigInteger>> STARTING_ITEMS = new HashMap<>();
    private static final Map<Integer, LinkedList<BigInteger>> ITEMS = new HashMap<>();
    private static final Map<Integer, Integer> INSPECTION_COUNT = new HashMap<>();

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

      LinkedList<BigInteger> items = new LinkedList<>();
      line = iterator.next();
      m = STARTING_ITEMS_PATTERN.matcher(line);
      if (!m.matches()) {
        throw new IllegalArgumentException(line);
      }
      Arrays.stream(m.group(1).split(", ")).map(BigInteger::new).forEach(items::add);

      STARTING_ITEMS.put(id, items);

      return new Monkey(
          id,
          Operation.parse(iterator.next()),
          Test.parse(iterator.next()),
          Condition.parse(iterator.next()),
          Condition.parse(iterator.next()));
    }

    private static void reset() {
      INSPECTION_COUNT.clear();
      ITEMS.clear();
      for (Map.Entry<Integer, LinkedList<BigInteger>> entry : STARTING_ITEMS.entrySet()) {
        ITEMS.put(entry.getKey(), new LinkedList<>(entry.getValue()));
      }
    }

    private static long getMonkeyBusiness() {
      return Monkey.INSPECTION_COUNT.values().stream()
          .sorted(Comparator.reverseOrder())
          .limit(2)
          .mapToLong(Integer::longValue)
          .reduce((a, b) -> a * b)
          .getAsLong();
    }

    private void takeTurn(Function<BigInteger, BigInteger> worryReducer) {
      debug("Monkey %d:", id);
      Iterator<BigInteger> iterator = ITEMS.get(id).iterator();
      while (iterator.hasNext()) {
        INSPECTION_COUNT.compute(id, (id, old) -> old == null ? 1 : old + 1);
        BigInteger worryLevel = iterator.next();
        debug("  Monkey inspects an item with a worry level of %d.", worryLevel);
        worryLevel = operation.apply(worryLevel);
        debug("    Worry level is increased to %d.", worryLevel);
        worryLevel = worryReducer.apply(worryLevel);
        debug("    Monkey gets bored with item. Worry level is divded by 3 to %d.", worryLevel);
        boolean result = test.apply(worryLevel);
        if (result) {
          debug("    Current worry level is divisible by %d.", test.divisibleBy());
          debug("    Item with worry level %d is thrown to monkey %d.", worryLevel, trueCondition.monkeyToThrowTo());
          ITEMS.get(trueCondition.monkeyToThrowTo()).add(worryLevel);
        } else {
          debug("    Current worry level is not divisible by %d.", test.divisibleBy());
          debug("    Item with worry level %d is thrown to monkey %d.", worryLevel, falseCondition.monkeyToThrowTo());
          ITEMS.get(falseCondition.monkeyToThrowTo()).add(worryLevel);
        }
        iterator.remove();
      }
    }
  }

  private static record Operation(Optional<BigInteger> left, char op, Optional<BigInteger> right) {

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
      String left = m.group(1);
      String right = m.group(3);
      return new Operation(
          left.equals("old") ? Optional.empty() : Optional.of(new BigInteger(left)),
          m.group(2).charAt(0),
          right.equals("old") ? Optional.empty() : Optional.of(new BigInteger(right)));
    }

    private BigInteger apply(BigInteger old) {
      BigInteger eqLeft = left.orElse(old);
      BigInteger eqRight = right.orElse(old);
      if (op == '+') {
        return eqLeft.add(eqRight);
      } else if (op == '*') {
        return eqLeft.multiply(eqRight);
      } else {
        throw new UnsupportedOperationException();
      }
    }
  }

  private static record Test(BigInteger divisibleBy) {

    // Test: divisible by 23
    private static final Pattern PATTERN = Pattern.compile("^\\ *Test:\\ divisible\\ by\\ (\\d+)$");

    private static Test parse(String line) {
      Matcher m = PATTERN.matcher(line);
      if (!m.matches()) {
        throw new IllegalArgumentException(line);
      }
      return new Test(new BigInteger(m.group(1)));
    }

    private boolean apply(BigInteger input) {
      return input.mod(divisibleBy).intValue() == 0;
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
