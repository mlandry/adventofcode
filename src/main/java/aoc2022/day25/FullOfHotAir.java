package aoc2022.day25;

import java.util.List;
import java.util.stream.Collectors;

import aoccommon.Debug;
import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/25}. */
public class FullOfHotAir {

  private static final String INPUT = "aoc2022/day25/input.txt";

  static record Snafu(String value) {
    static Snafu zero() {
      return new Snafu("0");
    }

    long toDecimal() {
      long multiplier = 1;
      long result = 0;
      for (int i = value.length() - 1; i >= 0; i--) {
        int digit = getDigit(value.charAt(i));
        result += (multiplier * digit);
        multiplier *= 5;
      }
      return result;
    }

    Snafu add(Snafu other) {
      int thisIndex = value.length() - 1;
      int otherIndex = other.value.length() - 1;
      int carryOver = 0;
      String result = "";
      while (thisIndex >= 0 || otherIndex >= 0 || carryOver > 0) {
        int added = carryOver
            + (thisIndex < 0 ? 0 : getDigit(value.charAt(thisIndex--)))
            + (otherIndex < 0 ? 0 : getDigit(other.value.charAt(otherIndex--)));

        carryOver = 0;

        if (added < -2) {
          carryOver = -1;
          result = toChar(added + 5) + result;
        } else if (added >= -2 && added <=2) {
          result = toChar(added) + result;
        } else {
          carryOver = 1;
          result = toChar(added - 5) + result;
        }
      }
      return new Snafu(result);
    }
  }

  private static int getDigit(char c) {
    if (Character.isDigit(c)) {
      return c - '0';
    }
    if (c == '-') {
      return -1;
    }
    if (c == '=') {
      return -2;
    }
    throw new IllegalArgumentException();
  }

  private static char toChar(int digit) {
    if (digit < -2 || digit > 2) {
      throw new IllegalArgumentException();
    }
    if (digit == -2) {
      return '=';
    } else if (digit == -1) {
      return '-';
    }
    return Character.forDigit(digit, 10);
  }

  public static void main(String[] args) throws Exception {
    // Debug.enablePrint();
    List<Snafu> input = InputHelper.linesFromResource(INPUT).map(Snafu::new).collect(Collectors.toList());
    Snafu sum = input.stream().reduce(Snafu.zero(), (s1, s2) -> s1.add(s2));
    System.out.println("Part 1: " + sum);
  }
}
