package aoc2022.day19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  private static final String INPUT = "aoc2022/day19/example.txt";

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
        .mapToInt(factory -> {
          Result result = factory.produceMaximumGeodes(State.start(24));
          // replay(factory.blueprint, result, State.start(24));
          return factory.blueprint.id() * result.geodeProduced();
        })
        .sum();
    System.out.println("Part 1: " + qualityLevelSum);
    Debug.endTimer("part1");

    Debug.startTimer("part2");
    long blueprintSubsetGeodeProduct = blueprints.stream()
        .limit(3)
        .map(Factory::new)
        .mapToLong(factory -> {
          Result result = factory.produceMaximumGeodes(State.start(32));
          System.out.println(result);
          replay(factory.blueprint, result, State.start(32));
          return result.geodeProduced();
        })
        .reduce(1, (a, b) -> a * b);
    System.out.println("Part 2: " + blueprintSubsetGeodeProduct);
    Debug.endTimer("part2");
  }

  @SuppressWarnings("unused")
  private static void replay(Blueprint blueprint, Result result, State start) {
    int[] robots = new int[] { 1, 0, 0, 0 };
    int[] resources = new int[4];
    for (int i = 1; i <= start.timeRemaining(); i++) {
      int timeRemaining = start.timeRemaining() - i + 1;
      Debug.println("== Minute %d ==", i);
      Resource createdRobot = result.robotTimestamps().get(timeRemaining);
      if (createdRobot != null) {
        String cost = blueprint.recipes().get(createdRobot).cost().entrySet().stream()
            .map(e -> String.format("%d %s", e.getValue(), e.getKey()))
            .collect(Collectors.joining(" and "));
        blueprint.recipes().get(createdRobot).cost().entrySet().stream()
            .forEach(e -> resources[e.getKey().ordinal()] -= e.getValue());
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
    private final Map<State, Result> cache = new HashMap<>();

    Factory(Blueprint blueprint) {
      this.blueprint = blueprint;
    }

    Result produceMaximumGeodes(State state) {
      Result result = cache.get(state);
      if (result != null) {
        return result;
      }
      if (state.timeRemaining() <= 2) {
        result = new Result(Map.of(), 0, List.of(state));
      } else {
        // For each type of robot, check a) do we want to keep building this robot and
        // b) when can we build it next.
        Map<Resource, Integer> nextRobots = Arrays.stream(Resource.values())
            .filter(r -> shouldKeepBuildingRobot(state, r))
            .collect(Collectors.toMap(r -> r, r -> minutesUntilRobotCanBeBuilt(state, r)));

        // For each possibile robot, fast forward to the state after it's been built and
        // recurse.
        result = nextRobots.entrySet().stream()
            .filter(e -> e.getValue() < state.timeRemaining() - 1)
            .map(robotToBuild -> {
              
              State fastForward = fastForward(state, robotToBuild.getKey(), robotToBuild.getValue());
              int lifetimeGeodeProducedByNewRobot = robotToBuild.getKey() == Resource.GEODE
                  ? fastForward.timeRemaining()
                  : 0;
              if (fastForward.timeRemaining() == state.timeRemaining()) {
                // This is the case where we created a robot and there is just one minute left
                // for it to produce.
                throw new IllegalStateException("Wtf");
                // return new Result(Map.of(fastForward.timeRemaining(), robotToBuild.getKey()),
                //     lifetimeGeodeProducedByNewRobot, List.of());
              }
              Result subResult = produceMaximumGeodes(fastForward);
              return subResult.update(fastForward.timeRemaining() + 1, robotToBuild.getKey(),
                  lifetimeGeodeProducedByNewRobot, fastForward);
            })
            .sorted((r1, r2) -> Integer.compare(r2.geodeProduced(), r1.geodeProduced()))
            .findFirst()
            .orElse(new Result(Map.of(), 0, List.of(state)));
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
            if (currentStock >= required) {
            return 0;
            }
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

      // First - produce resources from existing robots for N+1 (enough to start
      // creating the robot and then one more minute) minutes.
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
    static State start(int timeRemaining) {
      return new State(
          IntArray.of(1, 0, 0),
          IntArray.of(0, 0, 0),
          timeRemaining);
    }
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
