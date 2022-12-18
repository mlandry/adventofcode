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

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/16}. */
public class ProboscideaVolcanium {

  private static final String INPUT = "aoc2022/day16/example.txt";

  private static final Pattern REGEX = Pattern
      .compile("^Valve\\ (.+)\\ has\\ flow\\ rate=(\\d+);\\ tunnels?\\ leads?\\ to\\ valves?\\ (.+)$");

  public static void main(String[] args) throws Exception {
    Volcano volcano = Volcano.build(InputHelper.linesFromResource(INPUT));
    System.out.println("Part 1: " + volcano.findMaximumPressureReleased(State.start("AA", 30)));
    System.out.println("Part 2: " + volcano.findMaximumPressureReleased(State.startWithElephant("AA", 30)));
  }

  private static record State(
      String current,
      Optional<String> elephant,
      Set<String> opened,
      int remainingTime) {
    static State start(String start, int timeLimit) {
      return new State(start, Optional.empty(), Set.of(), timeLimit);
    }

    static State startWithElephant(String start, int timeLimit) {
      return new State(start, Optional.of(start), Set.of(), timeLimit - 4 /* teachingElephant */);
    }
  }

  private static record Action(String endPosition, Optional<String> valveToOpen) {
    static Action move(String to) {
      return new Action(to, Optional.empty());
    }

    static Action openValve(String valve) {
      return new Action(valve, Optional.of(valve));
    }
  }

  private static record Result(State state, int pressureReleased) {
  }

  private static class Volcano {
    private final Map<String, Integer> valves;
    private final Map<String, List<String>> tunnels;

    private final Map<String, Map<String, Integer>> shortestPaths;

    // Memoized cache of {currentNode + openedValves + remainingTime} => result.
    private final Map<State, Integer> cache = new HashMap<>();

    private Volcano(Map<String, Integer> valves, Map<String, List<String>> tunnels,
        Map<String, Map<String, Integer>> shortestPaths) {
      this.valves = valves;
      this.tunnels = tunnels;
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
          Collections.unmodifiableMap(tunnels),
          Collections.unmodifiableMap(shortestPaths));
    }

    int findMaximumPressureReleased(State state) {
      Integer cached = cache.get(state);
      if (cached != null) {
        return cached;
      }

      Stream<Result> possibilities = actionPossibilities(state.current, state.remainingTime, state.opened)
          .flatMap(action -> {
            Set<String> opened = state.opened;
            int pressureReleased = 0;
            int remainingTime = state.remainingTime - 1;
            if (action.valveToOpen.isPresent()) {
              opened = new HashSet<>(opened);
              opened.add(action.valveToOpen.get());
              pressureReleased = remainingTime * valves.get(action.valveToOpen.get());
            }

            if (state.elephant.isEmpty()) {
              State child = new State(action.endPosition, Optional.empty(), opened, remainingTime);
              return Stream.of(new Result(child, pressureReleased + findMaximumPressureReleased(child)));
            }
            Stream<Action> elephantPossibilities = actionPossibilities(state.elephant().get(), state.remainingTime,
                opened);

            final Set<String> selfOpened = opened;
            final int selfPressureReleased = pressureReleased;
            return elephantPossibilities.map(elephantAction -> {
              Set<String> elephantOpened = selfOpened;
              int elephantPressureReleased = selfPressureReleased;
              if (elephantAction.valveToOpen.isPresent()) {
                elephantOpened = new HashSet<>(elephantOpened);
                elephantOpened.add(elephantAction.valveToOpen.get());
                elephantPressureReleased += remainingTime * valves.get(elephantAction.valveToOpen.get());
              }
              State child = new State(action.endPosition, Optional.of(elephantAction.endPosition), elephantOpened,
                  remainingTime);
              return new Result(child, elephantPressureReleased + findMaximumPressureReleased(child));
            });
          });

      cached = possibilities.mapToInt(Result::pressureReleased).max().orElse(0);
      cache.put(state, cached);
      return cached;
    }

    Stream<Action> actionPossibilities(String position, int remainingTime, Set<String> opened) {
      Stream.Builder<Action> actions = Stream.builder();
      if (remainingTime >= 1 && valves.get(position) > 0 && !opened.contains(position)) {
        actions.add(Action.openValve(position));
      }

      if (remainingTime > 2) {
        for (String next : tunnels.get(position)) {
          actions.add(Action.move(next));
        }
      }
      return actions.build();
    }
  }
}
