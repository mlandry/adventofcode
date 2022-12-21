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

  private static final String INPUT = "aoc2022/day19/example.txt";
  private final static State START = new State(
      Arrays.stream(Resource.values()).filter(r -> r != Resource.GEODE)
          .collect(Collectors.toMap(r -> r, r -> r == Resource.ORE ? 1 : 0)),
      Arrays.stream(Resource.values()).filter(r -> r != Resource.GEODE).collect(Collectors.toMap(r -> r, r -> 0)),
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
        return 0;
      } else {
        // Check if we can build any robots and also consider the state where we don't
        // build any robots (save up resources).
        Stream<Optional<Resource>> robotPossibilities = Stream.concat(
            Stream.of(Optional.empty()),
            Arrays.stream(Resource.values()).filter(r -> shouldBuildRobot(state, r))
                .map(Optional::of));

        result = robotPossibilities.mapToInt(robotToBuild -> {
          int lifetimeGeodeProducedByNewRobot = robotToBuild.filter(r -> r == Resource.GEODE)
              .map(r -> state.timeRemaining() - 1).orElse(0);
          return lifetimeGeodeProducedByNewRobot + produceMaximumGeodes(tick(state, robotToBuild));
        }).max().orElse(0);
      }

      cache.put(state, result);
      return result;
    }

    boolean shouldBuildRobot(State state, Resource type) {
      if (state.timeRemaining() <= 1) {
        return false;
      }

      if (type != Resource.GEODE) {
        // Optimization - if we're already producing enough of this resource to build
        // any possible robot, don't bother building more.
        int robotsCreatingResource = state.robots().get(type);
        int currentStockOfResource = state.consumables().get(type);
        int totalPossibleProduction = robotsCreatingResource * state.timeRemaining();

        int maximumResourceRequiredForBuild = blueprint.recipes().values().stream()
            .map(RobotRecipe::cost)
            .map(Map::entrySet)
            .flatMap(Set::stream)
            .filter(e -> e.getKey() == type)
            .mapToInt(Map.Entry::getValue)
            .max()
            .orElse(0);
        int totalMaxRequired = maximumResourceRequiredForBuild * state.timeRemaining();

        if (robotsCreatingResource >= maximumResourceRequiredForBuild) {
          return false;
        }
        if (totalPossibleProduction + currentStockOfResource > totalMaxRequired) {
          return false;
        }
      }

      RobotRecipe recipe = blueprint.recipes().get(type);
      return recipe.cost().entrySet().stream().allMatch(e -> state.consumables().get(e.getKey()) >= e.getValue());
    }

    State tick(State state, Optional<Resource> robotToBuild) {
      Map<Resource, Integer> resources = new HashMap<>(state.consumables());

      // First - consume resources to start building a new robot.
      if (robotToBuild.isPresent()) {
        blueprint.recipes().get(robotToBuild.get()).cost().entrySet()
            .forEach(cost -> resources.compute(cost.getKey(), (k, v) -> v - cost.getValue()));
      }

      // Second - produce resources from existing robots.
      state.robots().entrySet().forEach(robots -> resources.compute(robots.getKey(), (k, v) -> v + robots.getValue()));

      // Finally - add the newly constructed robot to the set of robots.
      Map<Resource, Integer> robots = state.robots();
      // Exclude GEODE robots as they are accounted outside of the state.
      if (robotToBuild.filter(r -> r != Resource.GEODE).isPresent()) {
        robots = state.robots().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> robotToBuild.filter(r -> r == e.getKey())
                    .map(r -> e.getValue() + 1)
                    .orElse(e.getValue())));
      }

      return new State(robots, resources, state.timeRemaining() - 1);
    }
  }

  // State for memoization. Note robot and consumable totals don't not include
  // geodes to cut down on the number of states.
  // A geode robot is forward-counted as soon as it is created.
  private static record State(Map<Resource, Integer> robots, Map<Resource, Integer> consumables, int timeRemaining) {
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
