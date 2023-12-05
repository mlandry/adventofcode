package aoc2023.day04;

import aoccommon.InputHelper;
import aoccommon.Stats;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Solution for {@link https://adventofcode.com/2023/day/4}.
 */
public class Scratchcards {

  private static final String INPUT = "aoc2023/day04/input.txt";

  private static class Card {
    private final int id;
    private final Set<Integer> winningNumbers;
    private final Set<Integer> pickedNumbers;

    private Card(int id, Set<Integer> winningNumbers, Set<Integer> pickedNumbers) {
      this.id = id;
      this.winningNumbers = winningNumbers;
      this.pickedNumbers = pickedNumbers;
    }

    static Card parse(String line) {
      String[] split = line.split(":\\s+");
      int id = Integer.parseInt(split[0].split("\\s+")[1]);
      split = split[1].split("\\s+\\|\\s+");
      Set<Integer> winningNumbers = Arrays.stream(split[0].split("\\s+")).map(Integer::parseInt).collect(Collectors.toSet());
      Set<Integer> pickedNumbers = Arrays.stream(split[1].split("\\s+")).map(Integer::parseInt).collect(Collectors.toSet());
      return new Card(id, winningNumbers, pickedNumbers);
    }

    long countWinners() {
      return pickedNumbers.stream().filter(winningNumbers::contains).count();
    }
  }

  private static class ScratchcardProcessor {
    private final List<Card> scratchcards;

    // Maps a card index to the number of won cards at each index (recursive).
    private final Map<Integer, Map<Integer, Long>> cache = new HashMap<>();

    ScratchcardProcessor(List<Card> scratchcards) {
      this.scratchcards = scratchcards;
    }

    Map<Integer, Long> process() {
      Map<Integer, Long> cardCounts = new HashMap<>();
      for (int i = 0; i < scratchcards.size(); i++) {
        processCard(i).forEach((card, count) -> {
          Long prev = cardCounts.get(card);
          cardCounts.put(card, count + ((prev == null) ? 0 : prev));
        });
      }
      return cardCounts;
    }

    private Map<Integer, Long> processCard(int index) {
      if (index >= scratchcards.size()) {
        return Map.of();
      }
      Map<Integer, Long> cached = cache.get(index);
      if (cached != null) {
        return cached;
      }
      Map<Integer, Long> cardCounts = new HashMap<>();
      cardCounts.put(index, 1L);
      for (int i = index + 1; i <= index + scratchcards.get(index).countWinners(); i++) {
        processCard(i).forEach((card, count) -> {
          Long prev = cardCounts.get(card);
          cardCounts.put(card, count + ((prev == null) ? 0 : prev));
        });
      }
      cache.put(index, cardCounts);
      return cardCounts;
    }
  }

  public static void main(String[] args) throws Exception {
    List<Card> cards = InputHelper.linesFromResource(INPUT).map(Card::parse).toList();
    // Part 1.
    long sum = cards.stream().mapToLong(Card::countWinners).map(c -> (long) Math.pow(2, (c - 1))).sum();
    System.out.println("Part 1: " + sum);

    // Part 2.
    Stats.enablePrintOnExit();
    Stats.startTimer("part2");
    Map<Integer, Long> processedCards = new ScratchcardProcessor(cards).process();
    long totalCards = processedCards.values().stream().mapToLong(c -> c).sum();
    System.out.println("Part 2: " + totalCards);
    Stats.endTimer("part2");
  }


}
