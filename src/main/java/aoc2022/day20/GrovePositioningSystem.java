package aoc2022.day20;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import aoccommon.Debug;
import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/20}. */
public class GrovePositioningSystem {

  private static final String INPUT = "aoc2022/day20/input.txt";

  public static void main(String [] args) throws Exception {
    // Debug.enablePrint();
    // Debug.enableTimers();

    Debug.startTimer("parse");
    List<String> lines = InputHelper.linesFromResource(INPUT).collect(Collectors.toList());

    Node head = parseToCircularlyLinkedList(lines);
    Debug.println("Initial arrangement:");
    print(head);
    Debug.endTimer("parse");

    Debug.startTimer("mix");
    int length = length(head);
    List<Node> originalNodeOrder = nodeStream(head).collect(Collectors.toList());
    head = mix(head, originalNodeOrder, length);
    Debug.endTimer("mix");

    System.out.println("Part 1: " + computeKeyValueSum(head, length));

    // Reparse the unmixed list.
    head = parseToCircularlyLinkedList(lines);
    originalNodeOrder = nodeStream(head).collect(Collectors.toList());
    // Multiply all values by the decryption key.
    nodeStream(head).forEach(node -> node.updateValue(v -> v * 811589153L));
    Debug.println("Initial arrangement:");
    print(head);

    // Mix ten times.
    for (int i = 0; i < 10; i++) {
      Debug.startTimer("mix-" + (i + 1));
      head = mix(head, originalNodeOrder, length);
      Debug.println("After %d rounds of mixing:", i + 1);
      print(head);
      Debug.endTimer("mix-" + (i + 1));
    }

    System.out.println("Part 2: " + computeKeyValueSum(head, length));
  }

  private static Node parseToCircularlyLinkedList(List<String> lines) {
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
    return head;
  }

  private static Node mix(Node head, List<Node> originalNodeOrder, int length) {
    Iterator<Node> nodeIter = originalNodeOrder.iterator();
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

      // Current node is disconnected, so calculate the shift mod length - 1.
      int shift = (int) (Math.abs(current.value) % (long) (length - 1));
      if (current.value < 0) {
        shift = 0 - shift;
      }
      Node spot = get(current, shift);
      if (current.value > 0) {
        // Debug.println("%d moves between %d and %d:", current.value, spot.value, spot.next.value);
        spot.next.prev = current;
        current.next = spot.next;
        spot.next = current;
        current.prev = spot;
        // print(head);
        continue;
      }
      // Debug.println("%d moves between %d and %d:", current.value, spot.prev.value, spot.value);
      spot.prev.next = current;
      current.prev = spot.prev;
      spot.prev = current;
      current.next = spot;
      // print(head);
    }
    return head;
  }

  private static long computeKeyValueSum(Node head, int length) {
    int zeroIndex = indexOf(head, 0);
    return IntStream.of(1000, 2000, 3000)
        .map(i -> zeroIndex + i % length)
        .mapToObj(i -> get(head, i))
        .mapToLong(Node::value)
        .sum();
  }

  private static class Node {
    private long value;

    private Node prev;
    private Node next;

    Node(long value) {
      this.value = value;
    }

    static Node parse(String line) {
      return new Node(Long.parseLong(line));
    }

    long value() {
      return value;
    }

    void updateValue(Function<Long, Long> func) {
      this.value = func.apply(value);
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

  private static LongStream valueStream(Node head) {
    return nodeStream(head).mapToLong(Node::value);
  }

  private static void print(Node head) {
    Debug.println(valueStream(head).mapToObj(Long::toString).collect(Collectors.joining(", ")));
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

  private static int indexOf(Node head, long value) {
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
    for (int i = 0; i < Math.abs(index); i++) {
      current = index > 0 ? current.next : current.prev;
    }

    return current;
  }
}
