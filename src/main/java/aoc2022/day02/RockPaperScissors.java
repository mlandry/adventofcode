package aoc2022.day02;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/2}. */
class RockPaperScissors {

  private static final String INPUT = "aoc2022/day02/input.txt";

  private static enum Shape {
    ROCK,
    PAPER,
    SCISSORS,
  }

  private static final Map<Shape, Shape> MOVE_TO_WIN = Map.of(
      Shape.ROCK, Shape.PAPER,
      Shape.PAPER, Shape.SCISSORS,
      Shape.SCISSORS, Shape.ROCK);

  private static final Map<Shape, Shape> MOVE_TO_LOSE = MOVE_TO_WIN.entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

  private static final Map<String, Shape> PLAYS = Map.of(
      // Opponent plays.
      "A", Shape.ROCK,
      "B", Shape.PAPER,
      "C", Shape.SCISSORS,
      // Response plays.
      "X", Shape.ROCK,
      "Y", Shape.PAPER,
      "Z", Shape.SCISSORS);

  private static final Map<Shape, Integer> SHAPE_SCORES = Map.of(
      Shape.ROCK, 1,
      Shape.PAPER, 2,
      Shape.SCISSORS, 3);

  public static void main(String[] args) throws Exception {
    List<String[]> strategyGuide = InputHelper.linesFromResource(INPUT)
        .map(line -> line.split(" "))
        .collect(Collectors.toList());

    int part1Score = strategyGuide.stream()
        .map(split -> Arrays.stream(split).map(PLAYS::get).toArray(Shape[]::new))
        .mapToInt(RockPaperScissors::getScore)
        .sum();
    System.out.println("Part 1: " + part1Score);

    int part2Score = strategyGuide.stream()
        .map(RockPaperScissors::selectPlays)
        .mapToInt(RockPaperScissors::getScore)
        .sum();
    System.out.println("Part 2: " + part2Score);
  }

  private static Shape[] selectPlays(String[] strategy) {
    Shape opponentPlay = PLAYS.get(strategy[0]);
    Shape responsePlay = null;
    switch (strategy[1]) {
      case "X":
        // Lose.
        responsePlay = MOVE_TO_LOSE.get(opponentPlay);
        break;
      case "Y":
        // Draw.
        responsePlay = opponentPlay;
        break;
      case "Z":
        // Win.
        responsePlay = MOVE_TO_WIN.get(opponentPlay);
        break;
      default:
        throw new IllegalArgumentException();
    }
    return new Shape[] { opponentPlay, responsePlay };
  }

  private static int getScore(Shape[] plays) {
    return getOutcome(plays[0], plays[1]) + SHAPE_SCORES.get(plays[1]);
  }

  private static int getOutcome(Shape opponentPlay, Shape responsePlay) {
    if (opponentPlay == responsePlay) {
      return 3; // Draw.
    }
    switch (opponentPlay) {
      case ROCK:
        return responsePlay == Shape.PAPER ? 6 : 0;
      case PAPER:
        return responsePlay == Shape.SCISSORS ? 6 : 0;
      case SCISSORS:
        return responsePlay == Shape.ROCK ? 6 : 0;
      default:
        throw new IllegalArgumentException();
    }
  }
}
