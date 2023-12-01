package aoc2023.day01;

import aoccommon.InputHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Solution for {@link https://adventofcode.com/2023/day/1}.
 */
public class Trebuchet {

  private static final String INPUT = "aoc2023/day01/input.txt";

  private static final Map<String, Integer> DIGIT_WORDS = Map.of(
      "zero", 0,
      "one", 1,
      "two", 2,
      "three", 3,
      "four", 4,
      "five", 5,
      "six", 6,
      "seven", 7,
      "eight", 8,
      "nine", 9
  );

  private static class Node<T> {
    private final char ch;
    private final Map<Character, Node<T>> children;
    private String value = null;

    Node(char ch) {
      this.ch = ch;
      this.children = new HashMap<>();
    }
  }

  private static Node buildTrie() {
    Node<String> head = new Node('*');
    DIGIT_WORDS.keySet().stream().forEach(word -> {
      Node<String> curr = head;
      for (char ch : word.toCharArray()) {
        curr = curr.children.computeIfAbsent(ch, k -> new Node(ch));
      }
      curr.value = word;
    });
    return head;
  }

  public static void main(String[] args) throws Exception {
    List<String> lines = InputHelper.linesFromResource(INPUT).collect(Collectors.toList());
    // Part 1
    int sum = lines.stream()
        .mapToInt(line -> {
          int first = -1;
          int last = -1;
          for (char ch : line.toCharArray()) {
            if (!Character.isDigit(ch)) {
              continue;
            }
            if (first == -1) {
              first = ch - '0';
            }
            last = ch - '0';
          }
          return (first * 10) + last;
        })
        .sum();
    System.out.println("Part 1: " + sum);

    // Part 2
    Node<String> trie = buildTrie();
    sum = lines.stream()
        .mapToInt(line -> {
          List<Node<String>> candidates = new ArrayList<>();
          int first = -1;
          int firstIndex = Integer.MAX_VALUE;
          int last = -1;
          int lastIndex = -1;

          for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (Character.isDigit(ch)) {
              if (first == -1) {
                first = ch - '0';
                firstIndex = i;
              }
              last = ch - '0';
              lastIndex = i;
              continue;
            }

            candidates = candidates.stream()
                .filter(c -> c.children.containsKey(ch))
                .map(c -> c.children.get(ch))
                .collect(Collectors.toList());
            if (trie.children.containsKey(ch)) {
              candidates.add(trie.children.get(ch));
            }

            Iterator<Node<String>> iter = candidates.iterator();
            while (iter.hasNext()) {
              Node<String> c = iter.next();
              if (c.value == null) {
                continue;
              }
              iter.remove();
              int digit = DIGIT_WORDS.get(c.value);
              int index = i - (c.value.length() - 1);
              if (index < firstIndex) {
                first = digit;
                firstIndex = index;
              }
              if (index > lastIndex) {
                last = digit;
                lastIndex = index;
              }
            }
          }
          return (first * 10) + last;
        })
        .sum();
    System.out.println("Part 2: " + sum);
  }
}
