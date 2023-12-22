package aoc2023.day20;

import aoccommon.InputHelper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Solution for {@link https://adventofcode.com/2023/day/20}. */
public class PulsePropagation {

  private static final String INPUT = "aoc2023/day20/input.txt";
  private static final String EXAMPLE = "aoc2023/day20/example2.txt";

  private static final Pattern REGEX = Pattern.compile("^([&%]?)([a-z]+)\\s+\\->\\s+([a-z,\\s]+)$");

  private enum Pulse {
    LOW,
    HIGH,
  }

  private static abstract class Module {
    final String name;
    final List<Module> receivers = new ArrayList<>();

    Module(String name) {
      this.name = name;
    }

    abstract void send(String input, Pulse pulse);
  }

  private static class FlipFLopModule extends Module {
    private boolean on = false;

    FlipFLopModule(String name) {
      super(name);
    }

    @Override
    public void send(String input, Pulse pulse) {
      if (pulse == Pulse.LOW) {
        on = !on;
        this.receivers.get(0).send(this.name, on ? Pulse.HIGH : Pulse.LOW);
      }
    }
  }

  private static class ConjunctionModule extends Module {

    private final Map<String, Pulse> previous = new HashMap<>();

    ConjunctionModule(String name) {
      super(name);
    }

    @Override
    public void send(String input, Pulse pulse) {
      previous.put(input, pulse);
      if (previous.values().stream().allMatch(p -> p == Pulse.HIGH)) {
        this.receivers.forEach(r -> r.send(this.name, Pulse.LOW));
      } else {
        this.receivers.forEach(r -> r.send(this.name, Pulse.HIGH));
      }
    }
  }

  private static class BroadcastModule extends Module {

    BroadcastModule(String name) {
      super(name);
      if (!name.equals("broadcaster")) {
        throw new IllegalArgumentException();
      }
    }

    @Override
    public void send(String input, Pulse pulse) {
      this.receivers.forEach(d -> d.send(this.name, pulse));
    }
  }

  private static class OutputModule extends Module {
    private Pulse pulse;

    OutputModule(String name) {
      super(name);
    }

    @Override
    void send(String input, Pulse pulse) {
      this.pulse = pulse;
    }
  }

  public static void main(String [] args) throws Exception {
    Map<String, List<String>> inputs = new HashMap<>();
    Map<String, List<String>> outputs = new HashMap<>();
    Map<String, Module> modules = new HashMap<>();

    for (String line : InputHelper.linesFromResource(EXAMPLE).toList()) {
      Matcher m = REGEX.matcher(line);
      if (!m.matches()) {
        throw new IllegalArgumentException(line);
      }
      Module module = switch (m.group(1)) {
        case "%" -> new FlipFLopModule(m.group(2));
        case "&" -> new ConjunctionModule(m.group(2));
        case "" -> new BroadcastModule(m.group(2));
        default -> throw new IllegalArgumentException();
      };
      modules.put(module.name, module);
      Arrays.stream(m.group(3).split(",\\s*")).forEach(output -> {
        inputs.computeIfAbsent(output, s -> new ArrayList<>()).add(module.name);
        outputs.computeIfAbsent(module.name, s -> new ArrayList<>()).add(output);
      });
    }
    inputs.forEach((name, ins) -> {
      Module m = modules.get(name);
      if (m == null) {
        OutputModule o = new OutputModule(name);
        modules.put(name, o);
      }
      if (m instanceof ConjunctionModule) {
        ins.stream().forEach(in -> ((ConjunctionModule) m).previous.put(in, Pulse.LOW));
      }
    });
    outputs.forEach((name, outs) -> {
      Module m = modules.get(name);
      outs.stream().map(modules::get).forEach(m.receivers::add);
    });

    // Part 1.
    modules.get("broadcaster").send("button", Pulse.LOW);
    System.out.println(((OutputModule) modules.get("output")).pulse);
  }
}
