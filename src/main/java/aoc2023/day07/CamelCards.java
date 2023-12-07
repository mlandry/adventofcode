package aoc2023.day07;

import aoccommon.InputHelper;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Solution for {@link https://adventofcode.com/2023/day/7}.
 */
public class CamelCards {

  private static final String INPUT = "aoc2023/day07/input.txt";
  private static final String EXAMPLE = "aoc2023/day07/example.txt";

  private enum Type {
    HIGH_CARD,
    ONE_PAIR,
    TWO_PAIR,
    THREE_OF_A_KIND,
    FULL_HOUSE,
    FOUR_OF_A_KIND,
    FIVE_OF_A_KIND,
  }

  private record Cards(String cards, Type type) {
    static Cards parse(String cards, boolean withJokers) {
      return new Cards(cards, withJokers ? classifyWithJokers(cards) : classify(cards));
    }

    static Type classify(String cards) {
      Map<Character, Integer> sets = new HashMap<>();
      for (char card : cards.toCharArray()) {
        sets.compute(card, (c, count) -> count == null ? 1 : count + 1);
      }
      return classify(sets);
    }

    static Type classify(Map<Character, Integer> sets) {
      if (sets.size() == 1) {
        return Type.FIVE_OF_A_KIND;
      }
      if (sets.size() == 2) {
        return sets.values().stream().mapToInt(Integer::intValue).max().getAsInt() == 4
            ? Type.FOUR_OF_A_KIND
            : Type.FULL_HOUSE;
      }
      if (sets.size() == 3) {
        return sets.values().stream().mapToInt(Integer::intValue).max().getAsInt() == 3
            ? Type.THREE_OF_A_KIND
            : Type.TWO_PAIR;
      }
      return sets.size() == 4
          ? Type.ONE_PAIR
          : Type.HIGH_CARD;
    }

    static Type classifyWithJokers(String cards) {
      Map<Character, Integer> sets = new HashMap<>();
      for (char card : cards.toCharArray()) {
        sets.compute(card, (c, count) -> count == null ? 1 : count + 1);
      }

      Integer jokers = sets.remove('J');
      if (jokers == null) {
        return classify(sets);
      }

      if (jokers == 5 || sets.size() == 1) {
        return Type.FIVE_OF_A_KIND;
      }
      if (jokers == 3) {
        return Type.FOUR_OF_A_KIND;
      }
      if (jokers == 2) {
        if (sets.size() == 2) {
          return Type.FOUR_OF_A_KIND;
        }
        return Type.THREE_OF_A_KIND;
      }

      // 1 joker

      if (sets.size() == 4) {
        return Type.ONE_PAIR;
      }
      if (sets.size() == 3) {
        return Type.THREE_OF_A_KIND;
      }
      return sets.values().stream().mapToInt(Integer::intValue).max().getAsInt() == 3
          ? Type.FOUR_OF_A_KIND
          : Type.FULL_HOUSE;
    }
  }

  private record Hand(Cards cards, int bid) {
    static Hand parse(String line, boolean withJokers) {
      String[] split = line.split("\\s+");
      return new Hand(Cards.parse(split[0], withJokers), Integer.parseInt(split[1]));
    }

    @Override
    public String toString() {
      return String.format("%s (%s) %d", cards.cards, cards.type, bid);
    }
  }

  private static int getCardRank(char c, boolean withJokers) {
    if (Character.isDigit(c)) {
      return c - '0';
    }
    switch (c) {
      case 'T':
        return 10;
      case 'J':
        return withJokers ? 1 : 11;
      case 'Q':
        return 12;
      case 'K':
        return 13;
      case 'A':
        return 14;
      default:
        throw new IllegalArgumentException();
    }
  }

  private static Function<Boolean, Comparator<Hand>> HAND_COMPARATOR = (withJokers) -> (left, right) -> {
    int result = left.cards.type.ordinal() - right.cards.type.ordinal();
    if (result != 0) {
      return result;
    }

    // Same type, compare cards.
    for (int i = 0; i < 5; i++) {
      result = getCardRank(left.cards.cards.charAt(i), withJokers) - getCardRank(right.cards.cards.charAt(i), withJokers);
      if (result != 0) {
        return result;
      }
    }
    return result;
  };

  private static long calculateScore(List<Hand> sorted) {
    long result = 0;
    for (int i = 0; i < sorted.size(); i++) {
      long rank = i + 1L;
      result += rank * ((long) sorted.get(i).bid);
    }
    return result;
  }

  public static void main(String[] args) throws Exception {
    List<String> input = InputHelper.linesFromResource(INPUT).toList();

    // Part 1.
    List<Hand> hands = input.stream()
        .map(line -> Hand.parse(line, false))
        .sorted(HAND_COMPARATOR.apply(false))
        .toList();
    System.out.println("Part 1: " + calculateScore(hands));

    // Part 2.
    hands = input.stream()
        .map(line -> Hand.parse(line, true))
        .sorted(HAND_COMPARATOR.apply(true))
        .toList();
    System.out.println("Part 2: " + calculateScore(hands));
  }
}
