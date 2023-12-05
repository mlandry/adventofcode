package aoc2023.day05;

import aoccommon.InputHelper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Solution for {@link https://adventofcode.com/2023/day/5}.
 */
public class SeedFertilizer {

  private static final String INPUT = "aoc2023/day05/input.txt";
  // private static final String EXAMPLE = "aoc2023/day05/example.txt";

  private static final Pattern MAP_HEADER =
      Pattern.compile("^([a-z]+)\\-to\\-([a-z]+)\\ map:$");

  private record SeedRange(long start, long length) {
    LongStream stream() {
      return LongStream.range(start, start + length);
    }
  }

  private record Mapping(long destStart, long srcStart, long length) {
    static Mapping parse(String line) {
      String[] split = line.split("\\s+");
      return new Mapping(Long.parseLong(split[0]), Long.parseLong(split[1]), Long.parseLong(split[2]));
    }

    boolean contains(long src) {
      return src >= srcStart && src <= (srcStart + length - 1);
    }

    long map(long src) {
      return destStart + (src - srcStart);
    }
  }

  private record Converter(String input, String output, List<Mapping> mappings) {
    long convert(long src) {
        for (Mapping m : this.mappings) {
          if (m.contains(src)) {
            return m.map(src);
          }
        }
        return src;
      }
    }

  /**
   * @param converters Assumed to be in order: seed -> soil -> fertilizer -> water -> light -> temperature -> humidity -> location.
   */
  private record Almanac(List<SeedRange> seeds, List<Converter> converters) {

    static Almanac parse(List<String> input, boolean treatAsRanges) {
        if (!input.get(0).startsWith("seeds:")) {
          throw new IllegalArgumentException("expected seeds on first line");
        }
        List<SeedRange> seeds = null;
        if (treatAsRanges) {
          seeds = new ArrayList<>();
          String [] split = input.get(0).split(":\\s+")[1].split("\\s+");
          for (int i = 0; i < split.length; i += 2) {
            seeds.add(new SeedRange(Long.parseLong(split[i]), Long.parseLong(split[i + 1])));
          }
        } else {
          seeds = Arrays.stream(input.get(0).split(":\\s+")[1].split("\\s+"))
              .map(Long::parseLong)
              .map(s -> new SeedRange(s, 1))
              .toList();
        }

        List<Converter> converters = new ArrayList<>();
        Converter current = null;
        for (int i = 1; i < input.size(); i++) {
          String line = input.get(i);
          if (line.isBlank()) {
            continue;
          }
          Matcher m = MAP_HEADER.matcher(line);
          if (m.matches()) {
            String src = m.group(1);
            String dest = m.group(2);
            // Sanity check.
            if (converters.isEmpty()) {
              if (!src.equals("seed")) {
                throw new IllegalStateException("expected first converter to map seeds");
              }
            } else if (!src.equals(converters.get(converters.size() - 1).output)) {
              throw new IllegalStateException(
                  String.format(
                      "expected converters to be in order. expected next=%s, got=%s",
                      converters.get(converters.size() - 1),
                      src));
            }
            current = new Converter(src, dest, new ArrayList<>());
            converters.add(current);
            continue;
          }
          current.mappings.add(Mapping.parse(line));
        }

        return new Almanac(seeds, converters);
      }

      long mapSeedToLocation(long seed) {
        long out = seed;
        for (Converter converter : this.converters) {
          out = converter.convert(out);
        }
        return out;
      }
    }

  public static void main(String[] args) throws Exception {
    // Part 1.
    final Almanac almanac = Almanac.parse(InputHelper.linesFromResource(INPUT).toList(), false);
    long lowestLocation = almanac.seeds.stream()
        .flatMapToLong(SeedRange::stream)
        .map(almanac::mapSeedToLocation)
        .min()
        .getAsLong();
    System.out.println("Part 1: " + lowestLocation);

    // Part 2.
    final Almanac revisedAlmanac = Almanac.parse(InputHelper.linesFromResource(INPUT).toList(), true);
    lowestLocation = revisedAlmanac.seeds.stream()
        .flatMapToLong(SeedRange::stream)
        .map(almanac::mapSeedToLocation)
        .min()
        .getAsLong();
    System.out.println("Part 2: " + lowestLocation);
  }
}
