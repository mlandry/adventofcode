package aoc2022.day21;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import aoccommon.Debug;
import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/21}. */
public class MonkeyMath {

  private static final String INPUT = "aoc2022/day21/input.txt";
  private static final String ROOT = "root";
  private static final String HUMAN = "humn";

  public static void main(String[] args) throws Exception {
    // Debug.enablePrint();
    Map<String, Monkey> monkeys = InputHelper.linesFromResource(INPUT)
        .map(Monkey::parse)
        .collect(Collectors.toMap(Monkey::id, m -> m));

    System.out.println("Part 1: " + yell(monkeys, "root"));

    MonkeySolver solver = new MonkeySolver(monkeys);
    long humn = solver.solveForHuman();

    System.out.println("Part 2: " + humn);
  }

  private static long yell(Map<String, Monkey> monkeys, String id) {
    Monkey monkey = monkeys.get(id);
    if (monkey.job == Job.NUMBER) {
      return monkey.number();
    }

    long left = yell(monkeys, monkey.left());
    long right = yell(monkeys, monkey.right());
    return monkey.operation().apply(left, right);
  }

  private static class MonkeySolver {
    private final Map<String, Monkey> monkeys;
    private final Map<String, Long> resolved = new HashMap<>();

    MonkeySolver(Map<String, Monkey> monkeys) {
      this.monkeys = monkeys;
    }

    long solveForHuman() {
      int resolvedSize = -1;
      while (resolvedSize < resolved.size()) {
        resolvedSize = resolved.size();
        monkeys.keySet().forEach(this::tryResolveMonkey);
        Debug.println("Resolved %d more monkeys", resolved.size() - resolvedSize);
      }
      resolveFromRoot();
      return resolved.get(HUMAN);
    }

    void resolveFromRoot() {
      Monkey root = monkeys.get(ROOT);
      Long left = resolved.get(root.left());
      Long right = resolved.get(root.right());
      if (left == null && right == null) {
        throw new IllegalStateException("Cannot resolve from root without solving one side");
      }
      if (left == null) {
        resolveFromRoot(root.left(), right);
        return;
      } else if (right == null) {
        resolveFromRoot(root.right(), left);
        return;
      }
    }

    void resolveFromRoot(String id, long expectedValue) {
      if (id.equals(HUMAN)) {
        return;
      }
      Monkey root = monkeys.get(id);
      Long left = resolved.get(root.left());
      Long right = resolved.get(root.right());
      Debug.println("Resolving [%s]=%d with left=%d and right=%d", root, expectedValue, left, right);

      if (left == null && right == null) {
        return;
      }
      if (left != null && right != null) {
        resolved.put(id, root.operation().apply(left, right));
      }
      if (left == null) {
        left = root.operation().solveForLeft(expectedValue, right);
        resolved.put(root.left(), left);
        Debug.printlnAndWaitForInput("Traversing left after resolving %s to %d", root.left(), left);
        resolveFromRoot(root.left(), left);
      } else {
        right = root.operation().solveForRight(expectedValue, left);
        resolved.put(root.right(), right);
        Debug.printlnAndWaitForInput("Traversing right after resolving %s to %d", root.right(), right);
        resolveFromRoot(root.right(), right);
      }
    }

    private boolean tryResolveMonkey(String id) {
      if (id.equals(ROOT) || id.equals(HUMAN)) {
        return false;
      }
      Monkey monkey = monkeys.get(id);
      if (monkey.job == Job.NUMBER) {
        resolved.put(id, (long) monkey.number());
        return true;
      }

      Long left = resolved.get(monkey.left());
      if (left == null) {
        return false;
      }
      Long right = resolved.get(monkey.right());
      if (right == null) {
        return false;
      }
      resolved.put(id, monkey.operation().apply(left, right));
      return true;      
    }
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
          OperationMonkey.OPERATIONS.get(m.group(3).charAt(0)));
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

    Operation operation() {
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

    @Override
    public String toString() {
      return String.format("%s: %d", id, number);
    }
  }

  private static enum Operation {
    ADD((left, right) -> left + right),
    SUBTRACT((left, right) -> left - right),
    MULTIPLY((left, right) -> left * right),
    DIVIDE((left, right) -> left / right);

    private final BiFunction<Long, Long, Long> function;

    Operation(BiFunction<Long, Long, Long> function) {
      this.function = function;
    }
    
    long apply(long left, long right) {
      return function.apply(left, right);
    }

    long solveForLeft(long result, long right) {
      return solveForLeftFunction().apply(result, right);
    }

    long solveForRight(long result, long left) {
      return solveForRightFunction().apply(result, left);
    }

    private BiFunction<Long, Long, Long> solveForLeftFunction() {
      switch (this) {
        case ADD:
          return SUBTRACT.function;
        case SUBTRACT:
          // left - right = result
          // left = result + right
          return ADD.function;
        case MULTIPLY:
          // left * right = result
          // left = result / right
          return DIVIDE.function;
        case DIVIDE:
          // left / right = result
          // left = result * right
          return MULTIPLY.function;
        default:
          throw new IllegalArgumentException();
      }
    }

    private BiFunction<Long, Long, Long> solveForRightFunction() {
      switch (this) {
        case ADD:
          return SUBTRACT.function;
        case SUBTRACT:
          // left - right = result
          // right = left - result
          return (result, left) -> left - result;
        case MULTIPLY:
          // left * right = result
          // right = result / left
          return DIVIDE.function;
        case DIVIDE:
          // left / right = result
          // left = result * right
          // right = left / result
          return (result, left) -> left / result;
        default:
          throw new IllegalArgumentException();
      }
    }
  }

  private static class OperationMonkey extends Monkey {
    private static final Map<Character, Operation> OPERATIONS = Map.of(
        '+', Operation.ADD,
        '-', Operation.SUBTRACT,
        '*', Operation.MULTIPLY,
        '/', Operation.DIVIDE);

    private final String left;
    private final String right;
    private final Operation operation;

    OperationMonkey(String id, String left, String right, Operation operation) {
      super(id, Job.OPERATION);
      this.left = left;
      this.right = right;
      this.operation = operation;
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
    Operation operation() {
      return operation;
    }

    @Override
    public String toString() {
      return String.format("%s: %s %s %s", id, left, operation, right);
    }
  }
}
