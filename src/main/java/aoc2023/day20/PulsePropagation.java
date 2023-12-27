package aoc2023.day20;

import aoccommon.Debug;
import aoccommon.InputHelper;
import aoccommon.MoreMath;
import aoccommon.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Solution for {@link <a href="https://adventofcode.com/2023/day/20">AoC 2023 Day 20</a>}.
 */
public class PulsePropagation {

  private static final String INPUT = "aoc2023/day20/input.txt";
  private static final String EXAMPLE = "aoc2023/day20/example2.txt";

  private static final Pattern REGEX = Pattern.compile("^([&%]?)([a-z]+)\\s+->\\s+([a-z,\\s]+)$");

  private enum Pulse {
    LOW,
    HIGH,
  }

  interface Module {
    default void initialize(Set<String> inputs) {
    }

    List<Pair<String, Pulse>> process(String input, Pulse pulse);
  }

  private static class FlipFLopModule implements Module {
    private final String name;
    private final List<String> receivers;
    private boolean on = false;

    FlipFLopModule(String name, List<String> receivers) {
      this.name = name;
      this.receivers = receivers;
    }

    @Override
    public List<Pair<String, Pulse>> process(String input, Pulse pulse) {
      List<Pair<String, Pulse>> output = new ArrayList<>();
      if (pulse == Pulse.LOW) {
        on = !on;
        Pulse p = on ? Pulse.HIGH : Pulse.LOW;
        this.receivers.forEach(d -> output.add(Pair.of(d, p)));
      }
      return output;
    }

    @Override
    public String toString() {
      return "%" + name;
    }
  }

  private static class ConjunctionModule implements Module {

    private final String name;
    private final List<String> receivers;
    private final Map<String, Pulse> previous = new HashMap<>();

    ConjunctionModule(String name, List<String> receivers) {
      this.name = name;
      this.receivers = receivers;
    }

    @Override
    public void initialize(Set<String> inputs) {
      inputs.forEach(in -> previous.put(in, Pulse.LOW));
    }

    @Override
    public List<Pair<String, Pulse>> process(String input, Pulse pulse) {
      List<Pair<String, Pulse>> output = new ArrayList<>();
      previous.put(input, pulse);
      if (previous.values().stream().allMatch(p -> p == Pulse.HIGH)) {
        this.receivers.forEach(r -> output.add(Pair.of(r, Pulse.LOW)));
      } else {
        this.receivers.forEach(r -> output.add(Pair.of(r, Pulse.HIGH)));
      }
      return output;
    }

    @Override
    public String toString() {
      return "&" + name;
    }
  }

  private record BroadcastModule(List<String> receivers) implements Module {

    @Override
    public List<Pair<String, Pulse>> process(String input, Pulse pulse) {
      List<Pair<String, Pulse>> output = new ArrayList<>();
      this.receivers.forEach(d -> output.add(Pair.of(d, pulse)));
      return output;
    }

    @Override
    public String toString() {
      return "broadcast";
    }
  }

  private static class OutputModule implements Module {
    private final String name;
    private Pulse pulse;

    OutputModule(String name) {
      this.name = name;
    }

    @Override
    public List<Pair<String, Pulse>> process(String input, Pulse pulse) {
      this.pulse = pulse;
      return List.of();
    }

    @Override
    public String toString() {
      return name;
    }
  }

  private static Map<String, Module> parseModules(String input) throws Exception {
    Map<String, Module> modules = new HashMap<>();
    Map<String, Set<String>> inputs = new HashMap<>();

    for (String line : InputHelper.linesFromResource(input).toList()) {
      Matcher m = REGEX.matcher(line);
      if (!m.matches()) {
        throw new IllegalArgumentException(line);
      }
      List<String> receivers = Arrays.stream(m.group(3).split(",\\s*")).toList();
      String name = m.group(2);
      Module module = switch (m.group(1)) {
        case "%" -> new FlipFLopModule(name, receivers);
        case "&" -> new ConjunctionModule(name, receivers);
        case "" -> new BroadcastModule(receivers);
        default -> throw new IllegalArgumentException();
      };
      modules.put(name, module);
      receivers.forEach(r -> inputs.computeIfAbsent(r, s -> new HashSet<>()).add(name));
    }

    inputs.forEach((m, ins) -> {
      Module mod = modules.get(m);
      if (mod == null) {
        mod = new OutputModule(m);
        modules.put(m, mod);
      }
      mod.initialize(ins);
    });
    return modules;
  }

  public static void main(String[] args) throws Exception {
    // Debug.enablePrint();

    // Part 1.
    Map<String, Module> modules = parseModules(INPUT);
    Map<Pulse, Integer> counter = new HashMap<>();
    Function<Pulse, Void> countFunc = (p) -> {
      counter.compute(p, (pulse, count) -> count == null ? 1 : count + 1);
      return null;
    };
    for (int i = 0; i < 1000; i++) {
      Queue<Pair<String, Pair<String, Pulse>>> queue = new ArrayDeque<>();
      queue.add(Pair.of("button", Pair.of("broadcaster", Pulse.LOW)));
      while (!queue.isEmpty()) {
        int size = queue.size();
        for (int q = 0; q < size; q++) {
          Pair<String, Pair<String, Pulse>> next = queue.remove();
          String src = next.first();
          String dest = next.second().first();
          Pulse pulse = next.second().second();
          Debug.println("%s -%s-> %s", src, pulse.name().toLowerCase(), dest);
          countFunc.apply(pulse);
          modules.get(dest).process(src, pulse).forEach(o -> queue.add(Pair.of(dest, o)));
        }
      }
      Debug.println();
    }
    System.out.println("Part 1: " + counter.values().stream().reduce(1, Math::multiplyExact));

    // Part 2.
    modules = parseModules(INPUT);
    // Find fewest number of button presses required to deliver a single low pulse to the module named rx,
    // Brute force is not feasible so inspect input data.
    // rx has a single conjunction input %bb
    // &bb -> rx
    // bb has four flip-flop inputs
    // &ct -> bb
    // &kp -> bb
    // &ks -> bb
    // &xc -> bb
    Set<String> srcs = Set.of("ct", "kp", "ks", "xc");
    String target = "bb";
    Map<String, Integer> cycles = new HashMap<>();
    for (int i = 1; cycles.size() < srcs.size(); i++) {
      Queue<Pair<String, Pair<String, Pulse>>> queue = new ArrayDeque<>();
      queue.add(Pair.of("button", Pair.of("broadcaster", Pulse.LOW)));
      while (!queue.isEmpty()) {
        int size = queue.size();
        for (int q = 0; q < size; q++) {
          Pair<String, Pair<String, Pulse>> next = queue.remove();
          String src = next.first();
          String dest = next.second().first();
          Pulse pulse = next.second().second();
          if (srcs.contains(src) && target.equals(dest) && pulse == Pulse.HIGH && !cycles.containsKey(src)) {
            cycles.put(src, i);
          }
          modules.get(dest).process(src, pulse).forEach(o -> queue.add(Pair.of(dest, o)));
        }
      }
    }
    Debug.println("cycles=%s", cycles);
    long lcm = MoreMath.lcm(cycles.values().stream().mapToLong(i -> i).toArray());
    System.out.println("Part 2: " + lcm);
  }
}
