package aoc2015.day04;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2015/day/4}. */
public class StockingStuffer {

  private static final String INPUT = "aoc2015/day04/input.txt";

  public static void main(String [] args) throws Exception {
    String key = InputHelper.linesFromResource(INPUT).findFirst().get();

    for (int i = 1; i < Integer.MAX_VALUE; i++) {
      String hex = md5(key, i);
      if (hex.startsWith("00000")) {
        System.out.println("Part 1: " + i);
        break;
      }
    }

    for (int i = 1; i < Integer.MAX_VALUE; i++) {
      String hex = md5(key, i);
      if (hex.startsWith("000000")) {
        System.out.println("Part 2: " + i);
        break;
      }
    }
  }

  private static String md5(String key, int i) throws NoSuchAlgorithmException {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      md5.update((key + i).getBytes());
      return HexFormat.of().formatHex(md5.digest());
  }
}
