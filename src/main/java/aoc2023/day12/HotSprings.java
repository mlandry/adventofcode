package aoc2023.day12;

import aoccommon.InputHelper;
import aoccommon.IntArray;
import aoccommon.Pair;

import java.util.*;
import java.util.stream.Stream;

/**
 * Solution for {@link https://adventofcode.com/2023/day/12}.
 */
public class HotSprings {

  private static final String INPUT = "aoc2023/day12/input.txt";
  private static final String EXAMPLE = "aoc2023/day12/example.txt";

  private static final char OPERATIONAL = '.';
  private static final char DAMAGED = '#';
  private static final char UNKNOWN = '?';

  private record Record(String row, IntArray groups, Map<Pair<Integer, IntArray>, Long> cache) {

    static Record parse(String line) {
      String[] split = line.split("\\s+");
      return new Record(
          split[0],
          Arrays.stream(split[1].split(","))
              .mapToInt(Integer::parseInt)
              .collect(
                  IntArray::builder,
                  (builder, t) -> builder.add(t),
                  (b1, b2) -> b1.addAll(b2)
              )
              .build(),
          new HashMap<>());
    }

    Record unfold(int times) {
       StringBuilder sb = new StringBuilder();
       int[] groups = new int[this.groups.get().length * times];
       for (int i = 0; i < times; i++) {
         if (i > 0) {
           sb.append(UNKNOWN);
         }
         sb.append(row);
         for (int g = 0; g < this.groups.get().length; g++) {
           groups[(i * this.groups.get().length) + g] = this.groups.get()[g];
         }
       }
       return new Record(sb.toString(), IntArray.wrap(groups), new HashMap<>());
    }

    long countArrangements() {
      return countArrangement(0, groups);
    }

    private long countArrangement(int index, IntArray remaining) {
      Long cached = cache.get(Pair.of(index, remaining));
      if (cached != null) {
        return cached;
      }
      int target = remaining.get()[0];
      // Find all the ways to make the first group, then recurse into the remaining.
      long arrangements = 0;
      for (int start = index; start <= row.length() - target; start++) {
        boolean cantSkip = row.charAt(start) == DAMAGED;
        boolean group = true;
        for (int i = start; i < start + target; i++) {
          char c = row.charAt(i);
          if (c == OPERATIONAL) {
            group = false;
          }
        }
        if (!group) {
          if (cantSkip) {
            break;
          } else {
            continue;
          }
        }
        int next = start + target;
        if (next < row.length() && row.charAt(next) == DAMAGED) {
          if (cantSkip) {
            break;
          } else {
            continue;
          }
        }
        // If there are too many remaining damaged springs past our last group we can't count this.
        long remainingDamaged = row.chars().skip(next).filter(c -> ((char) c) == DAMAGED).count();
        long remainingGroupSum = remaining.stream().skip(1).sum();
        if (remainingDamaged > remainingGroupSum) {
          if (cantSkip) {
            break;
          } else {
            continue;
          }
        }
        if (remaining.get().length > 1) {
          long sub = countArrangement(next + 1, IntArray.wrap(remaining.stream().skip(1).toArray()));
          if (sub > 0) {
            arrangements += sub;
          }
        } else {
          arrangements += 1;
        }
        if (cantSkip) {
          // Can't skip past a damaged spring.
          break;
        }
      }

      cache.put(Pair.of(index, remaining), arrangements);
      return arrangements;
    }

//    void printArrangement(List<Integer> arrangement) {
//      int currentGroup = 0;
//      StringBuilder sb = new StringBuilder();
//      for (int i = 0; i < row.length(); ) {
//        if (currentGroup < arrangement.size() && i == arrangement.get(currentGroup)) {
//          for (int j = 0; j < groups.get()[currentGroup]; j++) {
//            sb.append('#');
//            i++;
//          }
//          currentGroup++;
//        } else {
//          sb.append('.');
//          i++;
//        }
//      }
//      System.out.println(sb.toString());
//    }
  }

  public static void main(String[] args) throws Exception {
    List<Record> records = InputHelper.linesFromResource(INPUT).map(Record::parse).toList();

    // Part 1.
    long sum = records.stream().mapToLong(Record::countArrangements).sum();
    System.out.println("Part 1: " + sum);

    // Part 2.
    records = records.stream().map(r -> r.unfold(5)).toList();
    sum = records.stream().mapToLong(Record::countArrangements).sum();
    System.out.println("Part 2: " + sum);
  }
}
