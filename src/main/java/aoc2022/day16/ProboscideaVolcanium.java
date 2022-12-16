package aoc2022.day16;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/16}. */
public class ProboscideaVolcanium {

  private static final String INPUT = "aoc2022/day16/input.txt";

  private static final Pattern REGEX = Pattern
      .compile("^Valve\\ (.+)\\ has\\ flow\\ rate=(\\d+);\\ tunnels?\\ leads?\\ to\\ valves?\\ (.+)$");

  public static void main(String[] args) throws Exception {
    Volcano volcano = Volcano.build(InputHelper.linesFromResource(INPUT));
    System.out.println("Part 1: " + volcano.findMaximumPressureReleased(State.start("AA", 30)));
  }

  private static record State(String current, Set<String> opened, int remainingTime) {
    static State start(String start, int timeLimit) {
      return new State(start, Set.of(), timeLimit);
    }

    State openCurrentValve() {
      Set<String> copy = new HashSet<>(opened);
      copy.add(current);
      return new State(current, copy, remainingTime - 1);
    }

    State move(String next) {
      return new State(next, new HashSet<>(opened), remainingTime - 1);
    }
  }

  private static class Volcano {
    private final Map<String, Integer> valves;
    private final Map<String, List<String>> tunnels;

    // Memoized cache of {currentNode + openedValves + remainingTime} => maximum
    // pressure.
    private final Map<State, Integer> cache = new HashMap<>();

    private Volcano(Map<String, Integer> valves, Map<String, List<String>> tunnels) {
      this.valves = valves;
      this.tunnels = tunnels;
    }

    static Volcano build(Stream<String> input) {
      final Map<String, Integer> valves = new HashMap<>();
      final Map<String, List<String>> tunnels = new HashMap<>();
      input.map(REGEX::matcher)
          .forEach(m -> {
            if (!m.matches()) {
              throw new IllegalArgumentException();
            }
            String name = m.group(1);
            int flowRate = Integer.parseInt(m.group(2));
            valves.put(name, flowRate);

            List<String> connected = Arrays.asList(m.group(3).split(", "));
            tunnels.put(name, connected);
          });
      return new Volcano(Collections.unmodifiableMap(valves), Collections.unmodifiableMap(tunnels));
    }

    int findMaximumPressureReleased(State state) {
      Integer cached = cache.get(state);
      if (cached != null) {
        return cached;
      }

      int flowRate = valves.get(state.current);

      IntStream.Builder possibilities = IntStream.builder();
      if (state.remainingTime >= 1 && flowRate > 0 && !state.opened.contains(state.current)) {
        State child = state.openCurrentValve();
        int pressureReleased = child.remainingTime * flowRate;
        possibilities.add(pressureReleased + findMaximumPressureReleased(child));
      }

      for (String next : tunnels.get(state.current)) {
        if (state.remainingTime > 2) {
          State child = state.move(next);
          possibilities.add(findMaximumPressureReleased(child));
        }
      }

      cached = possibilities.build().max().orElse(0);
      cache.put(state, cached);
      return cached;
    }
  }
}
