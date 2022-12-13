package aoc2022.day13;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/13}. */
public class DistressSignal {

  private static final String INPUT = "aoc2022/day13/input.txt";

  public static void main(String[] args) throws Exception {
    List<PacketPair> pairs = new ArrayList<>();

    Iterator<String> iter = InputHelper.linesFromResource(INPUT).iterator();
    while (iter.hasNext()) {
      pairs.add(new PacketPair(parsePacket(iter.next()), parsePacket(iter.next())));
      iter.next();
    }
  }

  private static Packet parsePacket(String line) {
    Deque<List<PacketData>> stack = new ArrayDeque<>();
    stack.add(new ArrayList<>());

    int i = 0;
    char c = line.charAt(i++);
    if (c != '[') {
      throw new IllegalStateException();
    }

    while(i < line.length() - 1) {
      c = line.charAt(i);
      if (c == '[') {
        stack.add(new ArrayList<>());
        i++;
      } else if (c == ']') {
        List<PacketData> list = stack.pop();
        stack.peek().add(PacketData.list(list));
        i++;
      } else if (Character.isDigit(c)) {
        
      }
    }

    return new Packet(stack.pop());
  }

  private static record PacketData(OptionalInt value, List<PacketData> list) {
    private static PacketData value(int value) {
      return new PacketData(OptionalInt.of(value), new ArrayList<>());
    }

    private static PacketData list(List<PacketData> list) {
      return new PacketData(OptionalInt.empty(), list);
    }
  }

  private static record Packet(List<PacketData> data) {
  }

  private static record PacketPair(Packet left, Packet right) {
  }
}
