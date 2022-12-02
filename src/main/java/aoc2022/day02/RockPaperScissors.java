package aoc2022.day02;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class RockPaperScissors {

  private static enum Shape {
    ROCK,
    PAPER,
    SCISSORS,
  }

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
    List<String[]> strategyGuide = inputReader().lines()
        .map(line -> line.split(" "))
        .collect(Collectors.toList());

    int score = strategyGuide.stream()
        .map(split -> Arrays.stream(split).map(PLAYS::get).toArray(Shape[]::new))
        .mapToInt(RockPaperScissors::getScore)
        .sum();

    System.out.println("Part 1: " + score);
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
        return -1;
    }
  }

  private static BufferedReader inputReader() throws IOException {
    return new BufferedReader(
        new InputStreamReader(
            RockPaperScissors.class.getClassLoader().getResourceAsStream("aoc2022/day02/input.txt")));
  }
}
