package aoc2022.day16;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/16}. */
public class ProboscideaVolcanium {

  private static final String INPUT = "aoc2022/day16/input.txt";

  private static final Pattern REGEX = Pattern
      .compile("^Valve\\ (.+)\\ has\\ flow\\ rate=(\\d+);\\ tunnels\\ lead\\ to\\ valves\\ (.+)$");

  public static void main(String[] args) throws Exception {
    Volcano volcano = Volcano.build(InputHelper.linesFromResource(INPUT));
  }

  private static record Valve(String name, int flowRate) {
  }

  private static class Volcano {
    private final Map<String, Valve> valves;
    private final Map<String, List<String>> tunnels;

    private Volcano(Map<String, Valve> valves, Map<String, List<String>> tunnels) {
      this.valves = valves;
      this.tunnels = tunnels;
    }

    static Volcano build(Stream<String> input) {
      final Map<String, Valve> valves = new HashMap<>();
      final Map<String, List<String>> tunnels = new HashMap<>();
      input.map(REGEX::matcher).filter(Matcher::matches)
          .forEach(m -> {
            String name = m.group(1);
            int flowRate = Integer.parseInt(m.group(2));
            valves.put(name, new Valve(name, flowRate));

            List<String> connected = Arrays.asList(m.group(3).split(", "));
            tunnels.put(name, connected);
          });
      return new Volcano(Collections.unmodifiableMap(valves), Collections.unmodifiableMap(tunnels));
    }
  }
}
