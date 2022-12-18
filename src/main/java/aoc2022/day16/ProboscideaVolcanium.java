package aoc2022.day16;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import aoccommon.Debug;
import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/16}. */
public class ProboscideaVolcanium {

  private static final String INPUT = "aoc2022/day16/input.txt";

  private static final Pattern REGEX = Pattern
      .compile("^Valve\\ (.+)\\ has\\ flow\\ rate=(\\d+);\\ tunnels?\\ leads?\\ to\\ valves?\\ (.+)$");

  public static void main(String[] args) throws Exception {
    Debug.enablePrint();
    Volcano volcano = Volcano.build(InputHelper.linesFromResource(INPUT));
    Debug.startTimer("main");
    System.out.println("Part 1: " + volcano.maxmimizePressureReleased(State.start("AA", 30)));
    Debug.endTimer("main");
    // System.out.println("Part 2: " +
    // volcano.findMaximumPressureReleased(State.startWithElephant("AA", 30)));
  }

  private static class Volcano {
    private final Map<String, Integer> valves;
    private final Map<String, Map<String, Integer>> shortestPaths;
    
    private final Map<State, Set<ValveSequence>> cache = new HashMap<>();

    private Volcano(Map<String, Integer> valves, Map<String, Map<String, Integer>> shortestPaths) {
      this.valves = valves;
      this.shortestPaths = shortestPaths;
    }

    static Map<String, Map<String, Integer>> computeAllShortestPaths(Map<String, List<String>> tunnels) {
      return tunnels.keySet().stream()
          .collect(Collectors.toMap(key -> key, key -> computeShortestPaths(key, tunnels)));
    }

    static Map<String, Integer> computeShortestPaths(String start, Map<String, List<String>> tunnels) {
      Queue<String> queue = new LinkedList<>();
      Map<String, Integer> shortestPaths = new HashMap<>();

      shortestPaths.put(start, 0);
      queue.offer(start);

      while (!queue.isEmpty()) {
        String current = queue.poll();
        int path = shortestPaths.get(current);
        for (String next : tunnels.get(current)) {
          if (shortestPaths.containsKey(next)) {
            continue;
          }
          shortestPaths.put(next, path + 1);
          queue.offer(next);
        }
      }
      return shortestPaths;
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

      Map<String, Map<String, Integer>> shortestPaths = computeAllShortestPaths(tunnels);
      return new Volcano(
          Collections.unmodifiableMap(valves),
          Collections.unmodifiableMap(shortestPaths));
    }

    int maxmimizePressureReleased(State state) {
      return findAllValveSequences(state).stream().mapToInt(ValveSequence::pressureReleased).max().orElse(0);
    }



    Set<ValveSequence> findAllValveSequences(State state) {
      Set<ValveSequence> valveSequences = cache.get(state);
      if (valveSequences != null) {
        return valveSequences;
      }
      valveSequences = new HashSet<>();

      Set<String> remaining = new HashSet<>(valves.keySet());
      remaining.removeAll(state.opened);

      for (String valve : remaining) {
        int timeTaken = shortestPaths.get(state.current).get(valve) + 1;
        if (timeTaken < state.timeRemaining && valves.get(valve) > 0) {
          int pressureReleasedFromValve = (state.timeRemaining - timeTaken) * valves.get(valve);
          State newState = state.afterOpeningValve(valve, timeTaken);
          Set<ValveSequence> nextValveSequences = findAllValveSequences(newState);
          if (nextValveSequences.isEmpty()) {
            valveSequences.add(ValveSequence.of(valve, pressureReleasedFromValve));
          } else {
            nextValveSequences.stream()
                .map(vs -> vs.appendFront(valve, pressureReleasedFromValve))
                .forEach(valveSequences::add);
          }
        }
      }
      return valveSequences;
    }
  }

  static record State(String current, Set<String> opened, int timeRemaining) {
    static State start(String start, int timeRemaining) {
      return new State(start, Set.of(), timeRemaining);
    }

    State afterOpeningValve(String valve, int timeTaken) {
      Set<String> opened = new HashSet<>(this.opened);
      opened.add(valve);
      return new State(valve, opened, timeRemaining - timeTaken);
    }
  }

  static record ValveSequence(List<String> opened, int pressureReleased) {
    static ValveSequence of(String valve, int pressureReleased) {
      return new ValveSequence(List.of(valve), pressureReleased);
    }

    ValveSequence appendFront(String valve, int pressureReleasedFromValve) {
      return new ValveSequence(
          Stream.concat(Stream.of(valve), opened.stream()).collect(Collectors.toList()),
          pressureReleased + pressureReleasedFromValve);
    }
  }

}
