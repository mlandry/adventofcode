package aoc2022.day03;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/3}. */
class RucksackReorganizer {

  private static final String INPUT = "aoc2022/day03/input.txt";

  private static final Set<Character> ALL_ITEM_TYPES = Stream
      .concat(
          IntStream.range('a', 'z' + 1).mapToObj(c -> (char) c),
          IntStream.range('A', 'Z' + 1).mapToObj(c -> (char) c))
      .collect(Collectors.toSet());

  public static void main(String[] args) throws Exception {
    List<String> rucksacks = InputHelper.linesFromResource(INPUT).collect(Collectors.toList());

    // Find common letters in first half and second half of the string (item types
    // shared between the two "compartments").
    int prioritySum = rucksacks.stream()
        .map(RucksackReorganizer::findCommonItemTypeInCompartments)
        .mapToInt(RucksackReorganizer::getPriority)
        .sum();
    System.out.println("Part 1: " + prioritySum);

    List<List<String>> elfGroups = IntStream.range(0, rucksacks.size())
        .filter(n -> n % 3 == 0)
        .mapToObj(n -> rucksacks.subList(n, n + 3))
        .collect(Collectors.toList());

    prioritySum = elfGroups.stream()
        .map(RucksackReorganizer::findCommonItemTypeInRucksacks)
        .mapToInt(RucksackReorganizer::getPriority)
        .sum();
    System.out.println("Part 2: " + prioritySum);
  }

  private static int getPriority(char c) {
    return Character.isLowerCase(c) ? (c - 'a' + 1) : (Character.toLowerCase(c) - 'a' + 27);
  }

  private static char findCommonItemTypeInCompartments(String rucksack) {
    Set<Character> compartment1 = stringToCharacterSet(rucksack.substring(0, rucksack.length() / 2));
    Set<Character> compartment2 = stringToCharacterSet(rucksack.substring(rucksack.length() / 2));
    compartment1.retainAll(compartment2);
    if (compartment1.size() != 1) {
      throw new IllegalStateException();
    }
    return compartment1.iterator().next();
  }

  private static char findCommonItemTypeInRucksacks(List<String> rucksacks) {
    Set<Character> commonItemTypes = rucksacks.stream()
        .map(RucksackReorganizer::stringToCharacterSet)
        .reduce(new HashSet<>(ALL_ITEM_TYPES), (set, chars) -> {
          set.retainAll(chars);
          return set;
        });
    if (commonItemTypes.size() != 1) {
      throw new IllegalStateException("unexpected size: " + commonItemTypes.size());
    }
    return commonItemTypes.iterator().next();
  }

  private static Set<Character> stringToCharacterSet(String str) {
    return str.chars().mapToObj(i -> (char) i).collect(Collectors.toSet());
  }
}
