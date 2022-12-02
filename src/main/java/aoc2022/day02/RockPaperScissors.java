package aoc2022.day02;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class RockPaperScissors {

  public static void main(String [] args) throws Exception {
  }

  private static BufferedReader inputReader() throws IOException {
    return new BufferedReader(
        new InputStreamReader(
            RockPaperScissors.class.getClassLoader().getResourceAsStream("aoc2022/day02/input.txt")));
  }
}
