package aoc2022.day16;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import aoccommon.Debug;
import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/16}. */
public class ProboscideaVolcanium {

  private static final String INPUT = "aoc2022/day16/input.txt";

  private static final Pattern REGEX = Pattern
      .compile("^Valve\\ (.+)\\ has\\ flow\\ rate=(\\d+);\\ tunnels?\\ leads?\\ to\\ valves?\\ (.+)$");

  public static void main(String[] args) throws Exception {
    Debug.enable();

    Volcano volcano = Volcano.build(InputHelper.linesFromResource(INPUT));
    System.out.println("Part 1: " + volcano.findMaximumPressureReleased(new State("AA", new HashSet<>(), 30)));
  }

  private static record Valve(String name, int flowRate) {
  }

  private static record State(String current, Set<String> opened, int remainingTime) {
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
    private final Map<String, Valve> valves;
    private final Map<String, List<String>> tunnels;

    // Memoized cache of {currentNode + openedValves + remainingTime} => maximum pressure.
    private final Map<State, Integer> cache = new HashMap<>();

    private Volcano(Map<String, Valve> valves, Map<String, List<String>> tunnels) {
      this.valves = valves;
      this.tunnels = tunnels;
    }

    static Volcano build(Stream<String> input) {
      final Map<String, Valve> valves = new HashMap<>();
      final Map<String, List<String>> tunnels = new HashMap<>();
      input.map(REGEX::matcher)
          .forEach(m -> {
            if (!m.matches()) {
              throw new IllegalArgumentException();
            }
            String name = m.group(1);
            int flowRate = Integer.parseInt(m.group(2));
            valves.put(name, new Valve(name, flowRate));

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
      
      Valve valve = valves.get(state.current);

      IntStream.Builder possibilities = IntStream.builder();
      if (state.remainingTime >= 1 && valve.flowRate > 0 && !state.opened.contains(state.current)) {
        State child = state.openCurrentValve();
        int pressureReleased = child.remainingTime * valve.flowRate;
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

    int maximizePressureReleased(String start, int timeLimit) {
      Queue<Path> queue = new LinkedList<>();
      Set<Path> cache = new HashSet<>();
      queue.offer(Path.start(start, timeLimit));
      cache.add(Path.start(start, timeLimit));
      int maxPressure = 0;

      while (!queue.isEmpty()) {
        Path path = queue.poll();
        Debug.printlnEveryN(10000, "path=%s, max=%d, queue=%d", path, maxPressure, queue.size());

        // First, check if we can/should open the current valve.
        Valve valve = valves.get(path.current);
        if (path.remainingTime >= 1 && valve.flowRate > 0 && !path.openedValves.contains(valve.name)) {
          Path toQueue = path.openValve(valve);
          if (cache.add(toQueue)) {
            queue.offer(toQueue);
          }
        }

        // Enqueue visits to the adjacent valves if it makes sense with remaining time.
        for (String next : tunnels.get(valve.name)) {
          Integer pressureReleasedAtLastVisit = path.visited.get(next);
          if (path.remainingTime > 1
              && (pressureReleasedAtLastVisit == null || path.pressureReleased > pressureReleasedAtLastVisit)) {
            Path toQueue = path.move(next);
            if (cache.add(toQueue)) {
              queue.offer(toQueue);
            }
          }
        }
        maxPressure = Math.max(maxPressure, path.pressureReleased);
      }
      return maxPressure;
    }
  }

  private static record Path(
      String current,
      Map<String, Integer> visited,
      Set<String> openedValves,
      int remainingTime,
      int pressureReleased) {
    static Path start(String start, int remainingTime) {
      return new Path(start, Map.of(start, 0), Set.of(), remainingTime, 0);
    }

    Path move(String next) {
      Map<String, Integer> visitedCopy = new HashMap<>(visited);
      visitedCopy.put(next, pressureReleased);
      return new Path(next, visitedCopy, new HashSet<>(openedValves), remainingTime - 1, pressureReleased);
    }

    Path openValve(Valve valve) {
      Set<String> openedCopy = new HashSet<>(openedValves);
      openedCopy.add(valve.name);
      int newRemainingTime = remainingTime - 1;
      int newPressureReleased = pressureReleased + (valve.flowRate * newRemainingTime);
      return new Path(valve.name, new HashMap<>(visited), openedCopy, newRemainingTime, newPressureReleased);
    }
  }
}
