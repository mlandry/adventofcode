package aoc2023.day13;

import aoccommon.InputHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Solution for {@link https://adventofcode.com/2023/day/13}.
 */
public class PointOfIncidence {

  private static final String INPUT = "aoc2023/day13/input.txt";
  private static final String EXAMPLE = "aoc2023/day13/example.txt";

  private record Pattern(List<String> rows) {
    IntStream row(int r) {
      return rows.get(r).chars();
    }

    IntStream col(int c) {
      return rows.stream().mapToInt(row -> row.charAt(c));
    }

    int diffs(IntStream a, IntStream b) {
      PrimitiveIterator.OfInt ai = a.iterator();
      PrimitiveIterator.OfInt bi = b.iterator();
      int diffs = 0;
      while (ai.hasNext()) {
        if (!bi.hasNext()) {
          throw new IllegalArgumentException("b had fewer elements than a");
        }
        if (!ai.next().equals(bi.next())) {
          diffs++;
        }
      }
      if (bi.hasNext()) {
        throw new IllegalArgumentException("b had more elements than a");
      }
      return diffs;
    }

    OptionalInt findMirrorRow(boolean smudged) {
      return smudged
          ? findMirrorWithSmudge(i -> row(i), rows.size())
          : findMirror(i -> row(i), rows.size());
    }

    OptionalInt findMirrorCol(boolean smudged) {
      return smudged
          ? findMirrorWithSmudge(i -> col(i), rows.get(0).length())
          : findMirror(i -> col(i), rows.get(0).length());
    }

    OptionalInt findMirror(Function<Integer, IntStream> getRowOrCol, int length) {
      outer:
      for (int a = 0; a < length - 1; a++) {
        int b = a + 1;
        if (diffs(getRowOrCol.apply(a), getRowOrCol.apply(b)) != 0) {
          continue;
        }
        for (int i = 1; a - i >= 0 && b + i < length; i++) {
          if (diffs(getRowOrCol.apply(a - i), getRowOrCol.apply(b + i)) != 0) {
            continue outer;
          }
        }
        return OptionalInt.of(a);
      }
      return OptionalInt.empty();
    }

    OptionalInt findMirrorWithSmudge(Function<Integer, IntStream> getRowOrCol, int length) {
      outer:
      for (int a = 0; a < length - 1; a++) {
        int b = a + 1;
        int diffs = diffs(getRowOrCol.apply(a), getRowOrCol.apply(b));
        if (diffs > 1) {
          continue;
        }
        for (int i = 1; a - i >= 0 && b + i < length; i++) {
          diffs += diffs(getRowOrCol.apply(a - i), getRowOrCol.apply(b + i));
          if (diffs > 1) {
            continue outer;
          }
        }
        if (diffs == 1) {
          return OptionalInt.of(a);
        }
      }
      return OptionalInt.empty();
    }

    long summarize(boolean smudged) {
        OptionalInt col = findMirrorCol(smudged);
        if (col.isPresent()) {
          return col.getAsInt() + 1;
        }
        OptionalInt row = findMirrorRow(smudged);
        if (!row.isPresent()) {
          throw new IllegalStateException();
        }
        return (100L * (1L + row.getAsInt()));

    }
  }

  public static void main(String[] args) throws Exception {
    List<String> lines = InputHelper.linesFromResource(INPUT).toList();

    List<Pattern> patterns = new ArrayList<>();
    List<String> rows = new ArrayList<>();
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (line.isBlank()) {
        if (!rows.isEmpty()) {
          patterns.add(new Pattern(rows));
        }
        rows = new ArrayList<>();
      } else {
        rows.add(line);
      }
    }
    if (!rows.isEmpty()) {
      patterns.add(new Pattern(rows));
    }

    // Part 1.
    long sum = patterns.stream().mapToLong(p -> p.summarize(false)).sum();
    System.out.println("Part 1: " + sum);

    // Part 2.
    sum = patterns.stream().mapToLong(p -> p.summarize(true)).sum();
    System.out.println("Part 2: " + sum);
  }
}
