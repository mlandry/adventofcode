package aoc2023.day19;

import aoccommon.InputHelper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Solution for {@link https://adventofcode.com/2023/day/19}.
 */
public class Aplenty {

  private static final String INPUT = "aoc2023/day19/input.txt";
  private static final String EXAMPLE = "aoc2023/day19/example.txt";

  private enum Result {
    ACCEPT,
    REJECT,
    SEND,
  }

  private record Outcome(Result result, Optional<String> destination) {
    static Outcome parse(String outcome) {
      return switch (outcome) {
        case "A" -> new Outcome(Result.ACCEPT, Optional.empty());
        case "R" -> new Outcome(Result.REJECT, Optional.empty());
        default -> new Outcome(Result.SEND, Optional.of(outcome));
      };
    }
  }

  private enum Operator {
    TRUE,
    GT,
    LT,
  }

  private record Condition(Operator op, Optional<Character> left, OptionalInt right) {
    private static final Pattern REGEX = Pattern.compile("^([xmas])([<>])(\\d+)$");

    static Condition trueCondition() {
      return new Condition(Operator.TRUE, Optional.empty(), OptionalInt.empty());
    }

    static Condition parse(String condition) {
      Matcher m = REGEX.matcher(condition);
      if (!m.matches()) {
        throw new IllegalArgumentException();
      }
      return new Condition(
          m.group(2).equals(">") ? Operator.GT : Operator.LT,
          Optional.of(m.group(1).charAt(0)),
          OptionalInt.of(Integer.parseInt(m.group(3))));
    }

    boolean evaluate(Part part) {
      return switch (op) {
        case TRUE -> true;
        case GT -> part.ratings().get(left.get()) > right.getAsInt();
        case LT -> part.ratings().get(left.get()) < right.getAsInt();
      };
    }
  }

  private record Rule(Condition condition, Outcome outcome) {

    private static final Pattern REGEX = Pattern.compile("^([xmas][<>]\\d+:)?([ARa-z]+)$");

    static Rule parse(String rule) {
      Matcher m = REGEX.matcher(rule);
      if (!m.matches()) {
        throw new IllegalArgumentException(rule);
      }
      if (m.group(1) != null) {
        String condition = m.group(1);
        condition = condition.substring(0, condition.length() - 1);
        return new Rule(Condition.parse(condition), Outcome.parse(m.group(2)));
      } else {
        return new Rule(Condition.trueCondition(), Outcome.parse(m.group(2)));
      }
    }
  }

  private record Workflow(String name, List<Rule> rules) {
    private static final Pattern REGEX = Pattern.compile("^([a-z]+)\\{(.+)\\}$");

    static Workflow parse(String line) {
      Matcher m = REGEX.matcher(line);
      if (!m.matches()) {
        throw new IllegalArgumentException();
      }
      return new Workflow(m.group(1), Arrays.stream(m.group(2).split(",")).map(Rule::parse).toList());
    }

    Outcome apply(Part part) {
      for (Rule rule : rules) {
        if (rule.condition().evaluate(part)) {
          return rule.outcome();
        }
      }
      throw new IllegalStateException("no rules applied");
    }
  }

  private record Part(Map<Character, Integer> ratings) {
    static Part parse(String line) {
      return new Part(Arrays.stream(line.substring(1, line.length() - 1).split(","))
          .collect(Collectors.toMap(s -> s.charAt(0), s -> Integer.parseInt(s.split("=")[1]))));
    }

    long sum() {
      return ratings.values().stream().mapToLong(i -> i).sum();
    }
  }

  private record Range(int low, int high, boolean empty) {
    Range {
      if (low > high) {
        throw new IllegalArgumentException();
      }
    }

    static Range of(int low, int high) {
      return new Range(low, high, false);
    }

    static Range none() {
      return new Range(0, 0, true);
    }

    Range alsoGreaterThan(int floor) {
      if (empty) {
        return Range.none();
      }
      if (low > floor) {
        return Range.of(low, high);
      }
      if (high > floor) {
        return Range.of(floor + 1, high);
      }
      return Range.none();
    }

    Range alsoLessThan(int ceiling) {
      if (empty) {
        return Range.none();
      }
      if (high < ceiling) {
        return Range.of(low, high);
      }
      if (low < ceiling) {
        return Range.of(low, ceiling - 1);
      }
      return Range.none();
    }

    int size() {
      if (empty) {
        return 0;
      }
      return high - low + 1;
    }
  }

  interface Combo {
    long count();
  }


  private record Ranges(Map<Character, Range> ranges) {
    static Ranges all() {
      return new Ranges(Stream.of('x', 'm', 'a', 's')
          .collect(Collectors.toMap(c -> c, c -> Range.of(1, 4000))));
    }

    Ranges greaterThan(char c, int floor) {
      Map<Character, Range> copy = new HashMap<>(ranges);
      copy.put(c, ranges.get(c).alsoGreaterThan(floor));
      return new Ranges(copy);
    }

    Ranges lessThan(char c, int ceiling) {
      Map<Character, Range> copy = new HashMap<>(ranges);
      copy.put(c, ranges.get(c).alsoLessThan(ceiling));
      return new Ranges(copy);
    }
  }

  private record RangesCombo(Ranges ranges, Result result) implements Combo {
    RangesCombo {
      if (result != Result.ACCEPT && result != Result.REJECT) {
        throw new IllegalArgumentException();
      }
    }

    @Override
    public long count() {
      if (result == Result.REJECT) {
        return 0;
      }
      return ranges.ranges().values().stream().mapToLong(Range::size).reduce(1, Math::multiplyExact);
    }
  }

  private record OrCombo(Combo a, Combo b) implements Combo {
    @Override
    public long count() {
      return a.count() + b.count();
    }
  }

  private static class ComboFinder {
    private final Map<String, Workflow> workflows;

    ComboFinder(Map<String, Workflow> workflows) {
      this.workflows = workflows;
    }

    Combo find(Workflow workflow) {
      return find(Ranges.all(), workflow);
    }

    Combo find(Ranges input, Workflow workflow) {
      Combo union = null;
      for (Rule rule : workflow.rules) {
        Combo combo = find(input, rule);
        if (union == null) {
          union = combo;
        } else {
          union = new OrCombo(union, combo);
        }
        Optional<Ranges> orElse = orElse(input, rule.condition);
        if (orElse.isEmpty()) {
          break;
        }
        input = orElse.get();
      }
      return union;
    }

    Combo find(Ranges input, Rule rule) {
      return switch (rule.condition.op) {
        case TRUE -> switch (rule.outcome.result) {
          case ACCEPT, REJECT -> new RangesCombo(input, rule.outcome.result);
          case SEND -> find(input, workflows.get(rule.outcome.destination.get()));
        };
        case GT -> switch (rule.outcome.result) {
          case ACCEPT, REJECT -> new RangesCombo(
              input.greaterThan(rule.condition.left.get(), rule.condition.right.getAsInt()),
              rule.outcome.result);
          case SEND -> find(
              input.greaterThan(rule.condition.left.get(), rule.condition.right.getAsInt()),
              workflows.get(rule.outcome.destination.get()));
        };
        case LT -> switch (rule.outcome.result) {
          case ACCEPT, REJECT -> new RangesCombo(
              input.lessThan(rule.condition.left.get(), rule.condition.right.getAsInt()),
              rule.outcome.result);
          case SEND -> find(
              input.lessThan(rule.condition.left.get(), rule.condition.right.getAsInt()),
              workflows.get(rule.outcome.destination.get()));
        };
      };
    }

    Optional<Ranges> orElse(Ranges input, Condition condition) {
      return switch (condition.op) {
        case TRUE -> Optional.empty();
        case GT -> Optional.of(input.lessThan(condition.left.get(), condition.right.getAsInt() + 1));
        case LT -> Optional.of(input.greaterThan(condition.left.get(), condition.right.getAsInt() - 1));
      };
    }
  }

  public static void main(String[] args) throws Exception {
    Iterator<String> lines = InputHelper.linesFromResource(INPUT).iterator();

    Map<String, Workflow> workflows = new HashMap<>();
    while (lines.hasNext()) {
      String line = lines.next();
      if (line.isBlank()) {
        break;
      }
      Workflow w = Workflow.parse(line);
      workflows.put(w.name(), w);
    }
    List<Part> parts = new ArrayList<>();
    while (lines.hasNext()) {
      String line = lines.next();
      parts.add(Part.parse(line));
    }

    // Part 1.
    long sum = 0;
    for (Part part : parts) {
      Outcome outcome = new Outcome(Result.SEND, Optional.of("in"));
      while (outcome.result() == Result.SEND) {
        Workflow w = workflows.get(outcome.destination().get());
        outcome = w.apply(part);
      }
      if (outcome.result() == Result.ACCEPT) {
        sum += part.sum();
      }
    }
    System.out.println("Part 1: " + sum);

    // Part 2.
    ComboFinder finder = new ComboFinder(workflows);
    System.out.println("Part 2: " + finder.find(workflows.get("in")).count());
  }
}
