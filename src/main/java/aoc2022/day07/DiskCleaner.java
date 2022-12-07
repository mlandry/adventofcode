package aoc2022.day07;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/7}. */
public class DiskCleaner {

  private static final String INPUT = "aoc2022/day07/input.txt";

  private static final Pattern CD_CMD = Pattern.compile("^\\$\\ cd\\ (.+)$");
  private static final Pattern LS_CMD = Pattern.compile("^\\$\\ ls.*$");

  private static final Pattern DIR_OUTPUT = Pattern.compile("^dir\\ (.+)$");
  private static final Pattern FILE_OUTPUT = Pattern.compile("^(\\d+)\\ (.+)$");

  public static void main(String [] args) throws Exception {
    List<String> output = InputHelper.linesFromResource(INPUT).collect(Collectors.toList());

    Deque<String> stack = new ArrayDeque<>();
    Map<String, Long> dirSizes = new HashMap<>();

    for (String line : output) {
      String curDir = stack.peek();
      Matcher m = CD_CMD.matcher(line);
      if (m.matches()) {
        String newDir = m.group(1);
        if (newDir.equals("..")) {
          String oldDir = stack.pop();
          // Roll up calculated size into parent directory.
          long oldDirSize = dirSizes.get(oldDir);
          dirSizes.compute(stack.peek(), (k, v) -> v == null ? oldDirSize : oldDirSize + v);
        } else {
          stack.push(getPath(curDir, newDir));
        }
        continue;
      }

      m = LS_CMD.matcher(line);
      if (m.matches()) {
        continue;
      }

      m = DIR_OUTPUT.matcher(line);
      if (m.matches()) {
        continue;
      }

      m = FILE_OUTPUT.matcher(line);
      if (m.matches()) {
        long fileSize = Long.parseLong(m.group(1));
        dirSizes.compute(stack.peek(), (k, v) -> v == null ? fileSize : fileSize + v);
      }
    }
    while (stack.size() > 1) {
      String childDir = stack.pop();
      // Roll up calculated size into parent directory.
      long childDirSize = dirSizes.get(childDir);
      dirSizes.compute(stack.peek(), (k, v) -> v == null ? childDirSize : childDirSize + v);
    }

    long sumBelowThreshold = dirSizes.values().stream()
        .filter(v -> v <= 100000)
        .mapToLong(v -> v)
        .sum();
    System.out.println("Part 1: " + sumBelowThreshold);

    long totalDiskSpace = 70000000;
    long requiredSpace = 30000000;
    long unusedSpace = totalDiskSpace - dirSizes.get(stack.peek());
    long spaceToDelete = requiredSpace - unusedSpace;

    long dirSizeToDelete = dirSizes.values().stream()
        .filter(v -> v >= spaceToDelete)
        .sorted()
        .findFirst()
        .get();
    System.out.println("Part 2: " + dirSizeToDelete);
  }

  private static String getPath(String curDir, String newDir) {
    if (curDir == null) {
      return newDir;
    }
    return curDir + (curDir.endsWith("/") ? "" : "/") + newDir;
  }
}
