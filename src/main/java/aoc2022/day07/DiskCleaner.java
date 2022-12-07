package aoc2022.day07;

import java.util.regex.Pattern;

/** Solution for {@link https://adventofcode.com/2022/day/7}. */
public class DiskCleaner {

  private static final String INPUT = "aoc2022/day07/input.txt";

  private static final Pattern CD_CMD = Pattern.compile("^\\$\\ cd\\ (.*)$");
  private static final Pattern LS_CMD = Pattern.compile("^\\$\\ ls.*$");

  public static void main(String [] args) throws Exception {
  }
}
