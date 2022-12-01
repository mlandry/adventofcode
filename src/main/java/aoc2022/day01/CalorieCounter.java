package aoc2022.day01;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class CalorieCounter {
  public static void main(String [] args) throws Exception {
  }

  private static BufferedReader inputReader() throws IOException {
    return new BufferedReader(
        new InputStreamReader(
            CalorieCounter.class.getClassLoader().getResourceAsStream("aoc2022/day01/input.txt")));
  }
}
