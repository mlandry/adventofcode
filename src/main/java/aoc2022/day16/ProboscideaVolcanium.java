package aoc2022.day16;

import java.util.ArrayList;
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
import java.util.TreeSet;
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
    // Debug.enablePrint();
    Volcano volcano = Volcano.build(InputHelper.linesFromResource(INPUT));
    Debug.startTimer("main");
    System.out.println("Part 1: " + volcano.maxmimizePressureReleased(State.start("AA", 30)));
    System.out.println("Part 2: " + volcano.maximizePressureReleasedWithElephant(State.start("AA", 30)));
    Debug.endTimer("main");
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
          valveSequences.add(ValveSequence.of(valve, pressureReleasedFromValve));
          nextValveSequences.stream()
              .map(vs -> vs.appendFront(valve, pressureReleasedFromValve))
              .forEach(valveSequences::add);
        }
      }
      cache.put(state, valveSequences);
      return valveSequences;
    }

    int maximizePressureReleasedWithElephant(State state) {
      state = state.subtractTime(4);
      Set<ValveSequence> sequences = findAllValveSequences(state);
      Debug.println("%d sequences", sequences.size());

      // "Optimization"? - only consider the top 1500 sequences by pressure released.
      sequences = sequences.stream().sorted((a, b) -> Integer.compare(b.pressureReleased(), a.pressureReleased()))
          .limit(1500).collect(Collectors.toSet());
      int maximum = 0;
      for (ValveSequence selfSeq : sequences) {
        Debug.printlnEveryN(1000, "max so far = %d", maximum);
        for (ValveSequence elephantSeq : sequences) {
          if (selfSeq == elephantSeq) {
            continue;
          }
          if (selfSeq.opened().size() > valves.size() - elephantSeq.opened().size()) {
            continue;
          }
          if (!selfSeq.disjoint(elephantSeq)) {
            continue;
          }
          maximum = Math.max(maximum, selfSeq.pressureReleased() + elephantSeq.pressureReleased());
        }
      }
      return maximum;
    }

    Set<List<ValveSequence>> findAllNonOverlappingSequencePairs(Set<ValveSequence> sequences) {
      int totalValves = valves.size();
      return sequences.stream()
          .map(s -> {
            Debug.printlnEveryN(1000, "computing...");
            return s;
          })
          .flatMap(s1 -> sequences.stream()
              .filter(s2 -> s1 != s2)
              .filter(s2 -> s2.opened().size() <= totalValves - s1.opened().size())
              .filter(s2 -> s2.disjoint(s1))
              .map(s2 -> List.of(s1, s2)))
          .collect(Collectors.toSet());
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

    State subtractTime(int timeDelta) {
      return new State(current, opened, timeRemaining - timeDelta);
    }
  }

  static record ValveSequence(TreeSet<String> opened, int pressureReleased) {
    static ValveSequence of(String valve, int pressureReleased) {
      TreeSet<String> opened = new TreeSet<>();
      opened.add(valve);
      return new ValveSequence(opened, pressureReleased);
    }

    ValveSequence appendFront(String valve, int pressureReleasedFromValve) {
      return new ValveSequence(
          Stream.concat(Stream.of(valve), opened.stream()).collect(Collectors.toCollection(TreeSet::new)),
          pressureReleased + pressureReleasedFromValve);
    }

    boolean disjoint(ValveSequence other) {
      return Collections.disjoint(opened, other.opened);
    }
  }
}
