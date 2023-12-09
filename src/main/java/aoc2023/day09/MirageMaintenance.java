package aoc2023.day09;

import aoccommon.InputHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Solution for {@link https://adventofcode.com/2023/day/9}.
 */
public class MirageMaintenance {

  private static final String INPUT = "aoc2023/day09/input.txt";
  private static final String EXAMPLE = "aoc2023/day09/example.txt";

  private record SequenceCalculator(List<int[]> sequences) {
    static SequenceCalculator create(int[] input) {
      List<int[]> sequences = new ArrayList<>();
      sequences.add(input);
      while (!Arrays.stream(input).allMatch(i -> i == 0)) {
        int[] deltas = new int[input.length - 1];
        for (int i = 0; i < deltas.length; i++) {
          deltas[i] = input[i + 1] - input[i];
        }
        sequences.add(deltas);
        input = deltas;
      }
      return new SequenceCalculator(sequences);
    }

    int predictNextValue() {
      int add = 0;
      for (int s = sequences.size() - 1; s >= 0; s--) {
        int[] seq = sequences.get(s);
        add = add + seq[seq.length - 1];
      }
      return add;
    }

    int predictPreviousValue() {
      int sub = 0;
      for (int s = sequences.size() - 1; s >= 0; s--) {
        int[] seq = sequences.get(s);
        sub = seq[0] - sub;
      }
      return sub;
    }
  }

  public static void main(String[] args) throws Exception {
    List<SequenceCalculator> calculators = InputHelper.linesFromResource(INPUT)
        .map(l -> l.split("\\s+"))
        .map(split -> Arrays.stream(split).mapToInt(Integer::parseInt).toArray())
        .map(SequenceCalculator::create)
        .toList();

    // Part 1.
    long sum = calculators.stream()
        .mapToLong(SequenceCalculator::predictNextValue)
        .sum();
    System.out.println("Part 1: " + sum);

    // Part 2.
    sum = calculators.stream()
        .mapToLong(SequenceCalculator::predictPreviousValue)
        .sum();
    System.out.println("Part 2: " + sum);

  }
}
