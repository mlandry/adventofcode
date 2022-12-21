package aoc2022.day19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
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
import aoccommon.IntArray;

/** Solution for {@link https://adventofcode.com/2022/day/19}. */
public class NotEnoughMinerals {

  private static final String INPUT = "aoc2022/day19/input.txt";

  private final static State START = new State(
      IntArray.of(1, 0, 0),
      IntArray.of(0, 0, 0),
      24);

  public static void main(String[] args) throws Exception {
    Debug.enablePrint();
    Debug.startTimer("parsing");
    List<Blueprint> blueprints = InputHelper.linesFromResource(INPUT)
        .map(Blueprint::parse)
        .collect(Collectors.toList());
    Debug.endTimer("parsing");

    Debug.startTimer("part1");
    int qualityLevelSum = blueprints.stream()
        .map(Factory::new)
        .mapToInt(factory -> factory.blueprint.id() * factory.produceMaximumGeodes(START))
        .sum();
    System.out.println("Part 1: " + qualityLevelSum);

    Debug.endTimer("part1");
  }

  @SuppressWarnings("unused")
  private static void replay(Blueprint blueprint, Result result) {
    int[] robots = new int[]{1, 0, 0, 0};
    int[] resources = new int[4];
    for (int i = 1; i <= START.timeRemaining(); i++) {
      int timeRemaining = START.timeRemaining() - i + 1;
      Debug.println("== Minute %d ==", i);
      Resource createdRobot = result.robotTimestamps().get(timeRemaining);
      if (createdRobot != null) {
        String cost = blueprint.recipes().get(createdRobot).cost().entrySet().stream()
            .map(e -> String.format("%d %s", e.getValue(), e.getKey()))
            .collect(Collectors.joining(" and "));
        blueprint.recipes().get(createdRobot).cost().entrySet().stream().forEach(e -> resources[e.getKey().ordinal()] -= e.getValue());
        Debug.println("Spend %s to start building a %s-collecting robot.", cost, createdRobot);
      }

      for (Resource r : Resource.values()) {
        int numRobots = robots[r.ordinal()];
        if (numRobots == 0) {
          continue;
        }
        resources[r.ordinal()] += numRobots;
        int amount = resources[r.ordinal()];
        Debug.println("%d %s-collecting %s %d %s; you now have %d %s.", numRobots, r,
            numRobots == 1 ? "robot collects" : "robots collect", numRobots, r, amount, r);
      }

      if (createdRobot != null) {
        int numRobots = ++robots[createdRobot.ordinal()];
        Debug.println("The new %s-collecting robot is ready; you now have %d of them.", createdRobot, numRobots);
      }
      Debug.println("");
    }
  }

  // Temporary data structure for debugging.
  @SuppressWarnings("unused")
  private static record Result(Map<Integer, Resource> robotTimestamps, int geodeProduced, List<State> states) {
    Result update(int timeRemaining, Resource robotCreated, int additionalGeode, State state) {
      Map<Integer, Resource> copy = new HashMap<>(robotTimestamps);
      copy.put(timeRemaining, robotCreated);
      List<State> statesCopy = new ArrayList<>(states);
      statesCopy.add(state);
      return new Result(copy, geodeProduced + additionalGeode, statesCopy);
    }
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
      if (state.timeRemaining() <= 2) {
        result = 0;
      } else {
        // For each type of robot, check a) do we want to keep building this robot and
        // b) when can we build it next.
        Map<Resource, Integer> nextRobots = Arrays.stream(Resource.values())
            .filter(r -> shouldKeepBuildingRobot(state, r))
            .collect(Collectors.toMap(r -> r, r -> minutesUntilRobotCanBeBuilt(state, r)));

        // For each possibile robot, fast forward to the state after it's been built and recurse.
        result = nextRobots.entrySet().stream()
            .filter(e -> e.getValue() < state.timeRemaining() - 1)
            .sorted((e1, e2) -> Integer.compare(e2.getKey().ordinal(), e1.getKey().ordinal()))
            .mapToInt(robotToBuild -> {
              int lifetimeGeodeProducedByNewRobot = robotToBuild.getKey() == Resource.GEODE
                  ? state.timeRemaining() - robotToBuild.getValue() - 1
                  : 0;
              State fastForward = fastForward(state, robotToBuild.getKey(), robotToBuild.getValue());
              return lifetimeGeodeProducedByNewRobot + produceMaximumGeodes(fastForward);
            })
            .max()
            .orElse(0);
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
      int robotsCreatingResource = state.robots().get()[type.ordinal()];
      int currentStockOfResource = state.consumables().get()[type.ordinal()];
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
            double productionRate = state.robots().get()[cost.getKey().ordinal()];
            double currentStock = state.consumables().get()[cost.getKey().ordinal()];
            double required = cost.getValue();
            // if (currentStock >= required) {
            //   return 0;
            // }
            if (productionRate == 0) {
              return Integer.MAX_VALUE;
            }
            return (int) Math.ceil((required - currentStock) / productionRate);
          })
          .max()
          .orElse(Integer.MAX_VALUE);
    }

    State fastForward(State state, Resource robotToBuild, int minutes) {
      IntArray wrappedResources = state.consumables().copy();
      int[] resources = wrappedResources.get();

      // First - produce resources from existing robots for N+1 (enough to start creating the robot and then one more minute) minutes.
      for (int i = 0; i < 3; i++) {
        resources[i] = resources[i] + ((minutes + 1) * state.robots().get()[i]);
      }

      // Second - consume resources to start building a new robot.
      blueprint.recipes().get(robotToBuild).cost().entrySet()
          .forEach(cost -> resources[cost.getKey().ordinal()] = resources[cost.getKey().ordinal()] - cost.getValue());

      // Finally - add the newly constructed robot to the set of robots.
      IntArray robots = state.robots();
      // Exclude GEODE robots as they are accounted outside of the state.
      if (robotToBuild != Resource.GEODE) {
        robots = robots.copy();
        robots.get()[robotToBuild.ordinal()]++;
      }

      return new State(robots, wrappedResources, state.timeRemaining() - minutes - 1);
    }
  }

  // State for memoization. Note robot and consumable totals don't not include
  // geodes to cut down on the number of states.
  // A geode robot is forward-counted as soon as it is created.
  private static record State(IntArray robots, IntArray consumables, int timeRemaining) {
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
