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

import aoccommon.Debug;
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
    //Debug.enablePrint();
    List<Blueprint> blueprints = InputHelper.linesFromResource(INPUT)
        .map(Blueprint::parse)
        .collect(Collectors.toList());

        Factory factory = new Factory(blueprints.get(0));
    System.out.println(factory.produceMaximumGeodes(START));
    factory.cache.entrySet().stream().filter(e -> e.getKey().timeRemaining() == 24).forEach(System.out::println);
    System.out.println(new Factory(blueprints.get(1)).produceMaximumGeodes(START));
  }

  private static class Factory {
    private final Blueprint blueprint;

    // Memoized cache of state -> produced geodes.
    private final Map<State, Integer> cache = new HashMap<>();

    Factory(Blueprint blueprint) {
      this.blueprint = blueprint;
    }

    int produceMaximumGeodes(State state) {
      Debug.printlnAndWaitForInput("== Minute %d ==\n%s", 24 - state.timeRemaining() + 1, state);
      Integer result = cache.get(state);
      if (result != null) {
        return result;
      }
      if (state.timeRemaining() <= 2) {
        result = 0;
      } else {
        // For each type of robot, check a) do we want to keep building this robot and
        // b) when can we build it next.
        Map<Resource, Integer> nextRobots = Arrays.stream(Resource.values())
            .filter(r -> shouldKeepBuildingRobot(state, r))
            .collect(Collectors.toMap(r -> r, r -> minutesUntilRobotCanBeBuilt(state, r)));

        result = nextRobots.entrySet().stream()
            .filter(e -> e.getValue() < state.timeRemaining() - 1)
            .sorted((e1, e2) -> Integer.compare(e2.getKey().ordinal(), e1.getKey().ordinal()))
            .mapToInt(robotToBuild -> {
              int lifetimeGeodeProducedByNewRobot = robotToBuild.getKey() == Resource.GEODE
                  ? state.timeRemaining() - robotToBuild.getValue() - 1
                  : 0;
              if (lifetimeGeodeProducedByNewRobot > 0) {
                Debug.printlnAndWaitForInput("Building a geode robot that will produce %d geodes", lifetimeGeodeProducedByNewRobot);
              }
              State fastForward = fastForward(state, robotToBuild.getKey(), robotToBuild.getValue() + 1);
              return lifetimeGeodeProducedByNewRobot + produceMaximumGeodes(fastForward);
            }).max().orElse(0);
      }

      cache.put(state, result);
      return result;
    }

    boolean shouldKeepBuildingRobot(State state, Resource type) {
      if (state.timeRemaining() <= 1) {
        return false;
      }
      if (type == Resource.GEODE) {
        return true;
      }
      // Optimization - if we're already producing enough of this resource to build
      // any possible robot, don't bother building more. Consider both on any
      // individual turn as well as the total possible production with current stock.
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
      return true;
    }

    int minutesUntilRobotCanBeBuilt(State state, Resource type) {
      return blueprint.recipes().get(type).cost().entrySet().stream()
          .mapToInt(cost -> {
            int productionRate = state.robots().get(cost.getKey());
            int currentStock = state.consumables().get(cost.getKey());
            int required = cost.getValue();
            if (currentStock >= required) {
              return 0;
            }
            if (productionRate == 0) {
              return Integer.MAX_VALUE;
            }
            return (required - currentStock) / productionRate;
          })
          .max()
          .orElse(Integer.MAX_VALUE);
    }

    State fastForward(State state, Resource robotToBuild, int minutes) {
      Map<Resource, Integer> resources = new HashMap<>(state.consumables());

      // First - produce resources from existing robots for N minutes.
      state.robots().entrySet()
          .forEach(robots -> resources.compute(robots.getKey(), (k, v) -> v + minutes * robots.getValue()));

      // Second - consume resources to start building a new robot.
      blueprint.recipes().get(robotToBuild).cost().entrySet()
          .forEach(cost -> resources.compute(cost.getKey(), (k, v) -> v - cost.getValue()));

      // Finally - add the newly constructed robot to the set of robots.
      Map<Resource, Integer> robots = state.robots();
      // Exclude GEODE robots as they are accounted outside of the state.
      if (robotToBuild != Resource.GEODE) {
        robots = state.robots().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> robotToBuild == e.getKey() ? e.getValue() + 1 : e.getValue()));
      }

      return new State(robots, resources, state.timeRemaining() - minutes);
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
