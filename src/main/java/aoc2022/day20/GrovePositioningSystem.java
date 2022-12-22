package aoc2022.day20;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import aoccommon.Debug;
import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/20}. */
public class GrovePositioningSystem {

  private static final String INPUT = "aoc2022/day20/input.txt";

  public static void main(String [] args) throws Exception {
    // Debug.enablePrint();
    List<String> lines = InputHelper.linesFromResource(INPUT).collect(Collectors.toList());

    Node prev = null;
    Node head = null;
    Iterator<String> input = lines.iterator();
    while (input.hasNext()) {
      Node node = Node.parse(input.next());
      if (head == null) {
        head = node;
      }
      node.prev = prev;
      if (prev != null) {
        prev.next = node;
      }
      prev = node;
    }
    // Connect tail to head to make it a circular linked list.
    head.prev = prev;
    prev.next = head;
    Debug.println("Initial arrangement:");
    print(head);

    Iterator<Node> nodeIter = nodeStream(head).iterator();
    while (nodeIter.hasNext()) {
      Node current = nodeIter.next();
      if (current.value == 0) {
        continue;
      }

      current.prev.next = current.next;
      current.next.prev = current.prev;

      if (current == head) {
        head = current.next;
      }

      Node spot = current;
      if (current.value > 0) {
        for (int j = 0; j < current.value; j++) {
          spot = spot.next;
        }
        Debug.println("%d moves between %d and %d:", current.value, spot.value, spot.next.value);
        spot.next.prev = current;
        current.next = spot.next;
        spot.next = current;
        current.prev = spot;
        print(head);
        continue;
      }

      for (int j = 0; j > current.value; j--) {
        spot = spot.prev;
      }
      Debug.println("%d moves between %d and %d:", current.value, spot.prev.value, spot.value);
      spot.prev.next = current;
      current.prev = spot.prev;
      spot.prev = current;
      current.next = spot;
      print(head);
    }

    int length = length(head);
    int zeroIndex = indexOf(head, 0);

    int value1000 = get(head, zeroIndex + 1000 % length).value();
    int value2000 = get(head, zeroIndex + 2000 % length).value();
    int value3000 = get(head, zeroIndex + 3000 % length).value();

    System.out.println("Part 1: " + (value1000 + value2000 + value3000));
  }

  private static class Node {
    private final int value;

    private Node prev;
    private Node next;

    Node(int value) {
      this.value = value;
    }

    static Node parse(String line) {
      return new Node(Integer.parseInt(line));
    }

    int value() {
      return value;
    }

    @Override
    public String toString() {
      return "{" + value + "}";
    }
  }

  private static Stream<Node> nodeStream(Node head) {
    Node current = head;
    Stream.Builder<Node> builder = Stream.builder();
    do {
      builder.add(current);
      current = current.next;
    } while (current != head);
    return builder.build();
  }

  private static IntStream valueStream(Node head) {
    return nodeStream(head).mapToInt(Node::value);
  }

  private static void print(Node head) {
    Debug.println(valueStream(head).mapToObj(Integer::toString).collect(Collectors.joining(", ")));
  }

  private static int length(Node head) {
    Node current = head;
    int len = 0;
    do {
      len++;
      current = current.next;
    } while (current != head);
    return len;
  }

  private static int indexOf(Node head, int value) {
    Node current = head;
    int i = 0;
    do {
      if (current.value == value) {
        return i;
      }
      i++;
      current = current.next;
    } while (current != head);
    return -1;
  }

  private static Node get(Node head, int index) {
    Node current = head;
    for (int i = 0; i < index; i++) {
      current = current.next;
    }
    return current;
  }
}
