package aoc2022.day13;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/13}. */
public class DistressSignal {

  private static final String INPUT = "aoc2022/day13/input.txt";

  private static final boolean DEBUG = false;

  private static void debug(String fmt, Object... args) {
    if (DEBUG) {
      System.out.println(String.format(fmt, args));
    }
  }

  public static void main(String[] args) throws Exception {
    final List<PacketPair> pairs = new ArrayList<>();

    Iterator<String> iter = InputHelper.linesFromResource(INPUT).iterator();
    while (iter.hasNext()) {
      pairs.add(new PacketPair(parsePacket(iter.next()), parsePacket(iter.next())));
      if (iter.hasNext()) {
        iter.next();
      }
    }

    int indexSum = IntStream.range(0, pairs.size())
        .map(i -> pairs.get(i).compare() < 0 ? i + 1 : 0)
        .sum();
    System.out.println("Part 1: " + indexSum);

    Stream<Packet> allPackets = pairs.stream().flatMap(pair -> Stream.of(pair.left, pair.right));
    List<Packet> dividers = List.of(parsePacket("[[2]]"), parsePacket("[[6]]"));
    List<Packet> sorted = Stream.concat(allPackets, dividers.stream()).sorted().collect(Collectors.toList());
    long decoderKey = dividers.stream().mapToLong(d -> sorted.indexOf(d) + 1).reduce(1, (a, b) -> a * b);
    System.out.println("Part 2: " + decoderKey);
  }

  private static Packet parsePacket(String line) {
    Deque<List<PacketData>> stack = new ArrayDeque<>();
    stack.push(new ArrayList<>());

    int i = 0;
    char c = line.charAt(i++);
    if (c != '[') {
      throw new IllegalStateException();
    }

    while (i < line.length() - 1 /* exclude closing ] */) {
      c = line.charAt(i);
      debug("Parsing char %s at index %d, stack=%s", c, i, stack);
      if (c == '[') {
        stack.push(new ArrayList<>());
        i++;
      } else if (c == ']') {
        List<PacketData> list = stack.pop();
        // stack.peek().add(list.size() == 1 ? list.get(0) : PacketData.list(list));
        stack.peek().add(PacketData.list(list));
        i++;
      } else if (Character.isDigit(c)) {
        int value = 0;
        while (Character.isDigit(c)) {
          value *= 10;
          value += (c - '0');
          c = line.charAt(++i);
        }
        stack.peek().add(PacketData.value(value));
      } else if (c == ',') {
        i++;
      } else {
        throw new IllegalStateException();
      }
    }
    Packet packet = new Packet(stack.pop());
    debug("Parsed: " + packet);
    return packet;
  }

  private static record PacketData(OptionalInt value, List<PacketData> list) implements Comparable<PacketData> {
    private static PacketData value(int value) {
      return new PacketData(OptionalInt.of(value), new ArrayList<>());
    }

    private static PacketData list(List<PacketData> list) {
      return new PacketData(OptionalInt.empty(), list);
    }

    private List<PacketData> asList() {
      return value.isEmpty() ? list : List.of(this);
    }

    @Override
    public int compareTo(PacketData other) {
      if (value.isPresent() && other.value.isPresent()) {
        return Integer.compare(value.getAsInt(), other.value.getAsInt());
      }
      return compareLists(asList(), other.asList());
    }

    @Override
    public String toString() {
      if (value.isPresent()) {
        return Integer.toString(value.getAsInt());
      }
      return "[" + list.stream().map(PacketData::toString).collect(Collectors.joining(",")) + "]";
    }
  }

  private static record Packet(List<PacketData> data) implements Comparable<Packet> {
    @Override
    public int compareTo(Packet other) {
      return compareLists(data, other.data);
    }

    @Override
    public String toString() {
      return "[" + data.stream().map(PacketData::toString).collect(Collectors.joining(",")) + "]";
    }
  }

  private static record PacketPair(Packet left, Packet right) {
    private int compare() {
      debug("- Compare %s vs %s", left, right);
      return left.compareTo(right);
    }
  }

  private static int compareLists(List<PacketData> leftList, List<PacketData> rightList) {
    Iterator<PacketData> leftIter = leftList.iterator();
    Iterator<PacketData> rightIter = rightList.iterator();
    while (leftIter.hasNext() || rightIter.hasNext()) {
      if (!leftIter.hasNext()) {
        debug("  - Left side ran out of items, so inputs are in the right order");
        return -1;
      } else if (!rightIter.hasNext()) {
        debug("  - Right side ran out of items, so inputs are not in the right order");
        return 1;
      }
      PacketData left = leftIter.next();
      PacketData right = rightIter.next();
      debug("  - Compare %s vs %s", left, right);
      int compare = left.compareTo(right);
      if (compare != 0) {
        debug("    - %s side is smaller, so inputs are %sin the right order", compare < 0 ? "Left" : "Right",
            compare < 0 ? "" : "not ");
        return compare;
      }
    }
    return 0;
  }
}
