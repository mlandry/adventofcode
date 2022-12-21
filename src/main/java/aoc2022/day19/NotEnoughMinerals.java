package aoc2022.day19;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/19}. */
public class NotEnoughMinerals {

  private static final String INPUT = "aoc2022/day19/input.txt";
  private final static State START = new State(
      Arrays.stream(Resource.values()).collect(Collectors.toMap(r -> r, r -> r == Resource.ORE ? 1 : 0)),
      Arrays.stream(Resource.values()).collect(Collectors.toMap(r -> r, r -> 0)),
      24);

  public static void main(String[] args) throws Exception {
    List<Blueprint> blueprints = InputHelper.linesFromResource(INPUT)
        .map(Blueprint::parse)
        .collect(Collectors.toList());

    System.out.println(new Factory(blueprints.get(0)).produceMaximumGeodes(START));
  }

  private static class Factory {
    private final Blueprint blueprint;

    // Memoized cache of state -> produced geodes.
    private final Map<State, Integer> cache = new HashMap<>();

    Factory(Blueprint blueprint) {
      this.blueprint = blueprint;
    }

    int produceMaximumGeodes(State state) {
      Integer result = cache.get(state);
      if (result != null) {
        return result;
      }

      if (state.timeRemaining() < 1) {
        return state.resources().get(Resource.GEODE);
      } else {
        // Check if we can build any robots and also consider the state where we don't
        // build any robots (save up resources).
        Stream<Optional<Resource>> possibleRobotStates = Stream.concat(
            Stream.of(Optional.empty()),
            Arrays.stream(Resource.values()).filter(r -> canBuildRobot(state, r))
                .map(Optional::of));

        Stream<State> nextStates = possibleRobotStates.map(robotToBuild -> tick(state, robotToBuild));

        result = nextStates.mapToInt(this::produceMaximumGeodes).max().orElse(0);
      }

      cache.put(state, result);
      return result;
    }

    boolean canBuildRobot(State state, Resource type) {
      if (state.timeRemaining() <= 1) {
        return false;
      }
      RobotRecipe recipe = blueprint.recipes().get(type);
      return recipe.cost().entrySet().stream()
          .allMatch(e -> state.resources().get(e.getKey()) >= e.getValue());
    }

    State tick(State state, Optional<Resource> robotToBuild) {
      Map<Resource, Integer> resources = new HashMap<>(state.resources());

      // First - consume resources to start building a new robot.
      if (robotToBuild.isPresent()) {
        blueprint.recipes().get(robotToBuild.get()).cost().entrySet()
            .forEach(cost -> resources.compute(cost.getKey(), (k, v) -> v - cost.getValue()));
      }

      // Second - produce resources from existing robots.
      state.robots().entrySet().forEach(robots -> resources.compute(robots.getKey(), (k, v) -> v + robots.getValue()));

      // Finally - add the newly constructed robot to the set of robots.
      Map<Resource, Integer> robots = state.robots().entrySet().stream()
          .collect(Collectors.toMap(
              Map.Entry::getKey,
              e -> robotToBuild.filter(r -> r == e.getKey())
                  .map(r -> e.getValue() + 1)
                  .orElse(e.getValue())));

      return new State(robots, resources, state.timeRemaining() - 1);
    }
  }

  private static record State(Map<Resource, Integer> robots, Map<Resource, Integer> resources, int timeRemaining) {
  }

  private static enum Resource {
    ORE,
    CLAY,
    OBSIDIAN,
    GEODE,
  }

  private static record RobotRecipe(Resource type, Map<Resource, Integer> cost) {
  }

  private static record Blueprint(int id, Map<Resource, RobotRecipe> recipes) {

    private static final Pattern REGEX = Pattern.compile(
        "^Blueprint\\ (\\d+):\\ " +
            "Each\\ ore\\ robot\\ costs\\ (\\d+)\\ ore.\\ " +
            "Each\\ clay\\ robot\\ costs\\ (\\d+)\\ ore.\\ " +
            "Each\\ obsidian\\ robot\\ costs\\ (\\d+)\\ ore\\ and (\\d+)\\ clay.\\ " +
            "Each\\ geode\\ robot\\ costs\\ (\\d+)\\ ore\\ and\\ (\\d+)\\ obsidian.$");

    static Blueprint parse(String line) {
      Matcher m = REGEX.matcher(line);
      if (!m.matches()) {
        throw new IllegalArgumentException("Could not parse: " + line);
      }
      int id = Integer.parseInt(m.group(1));
      Map<Resource, RobotRecipe> recipes = Map.of(
          Resource.ORE,
          new RobotRecipe(Resource.ORE, Map.of(Resource.ORE, Integer.parseInt(m.group(2)))),
          Resource.CLAY,
          new RobotRecipe(Resource.CLAY, Map.of(Resource.ORE, Integer.parseInt(m.group(3)))),
          Resource.OBSIDIAN,
          new RobotRecipe(
              Resource.OBSIDIAN,
              Map.of(
                  Resource.ORE, Integer.parseInt(m.group(4)),
                  Resource.CLAY, Integer.parseInt(m.group(5)))),
          Resource.GEODE,
          new RobotRecipe(
              Resource.GEODE,
              Map.of(
                  Resource.ORE, Integer.parseInt(m.group(6)),
                  Resource.OBSIDIAN, Integer.parseInt(m.group(7)))));
      return new Blueprint(id, recipes);
    }
  }
}
