package aoc2022.day10;

import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/10}. */
public class CathodeRayTube {

  private static final String INPUT = "aoc2022/day10/input.txt";

  public static void main(String[] args) throws Exception {
    Queue<Instruction> queue = InputHelper.linesFromResource(INPUT)
        .map(Instruction::parse)
        .collect(Collectors.toCollection(LinkedList::new));

    long cycle = 0;
    long register = 1;
    long signalSum = 0;
    Instruction instruction = null;

    while ((instruction = queue.poll()) != null) {
      switch (instruction.type) {
        case NOOP:
          if ((++cycle - 20) % 40 == 0) {
            signalSum += (cycle * register);
          }
          break;
        case ADDX:
          if ((++cycle - 20) % 40 == 0) {
            signalSum += (cycle * register);
          }
          if ((++cycle - 20) % 40 == 0) {
            signalSum += (cycle * register);
          }
          register += instruction.value();
          break;
      }
    }

    System.out.println("Part 1: " + signalSum);
  }

  private enum Type {
    NOOP(1),
    ADDX(2);

    private final int cycles;

    private Type(int cycles) {
      this.cycles = cycles;
    }
  }

  private record Instruction(Type type, int value) {
    static Instruction parse(String line) {
      String[] split = line.split(" ");
      return new Instruction(
        Type.valueOf(split[0].toUpperCase()),
        split.length == 2 ? Integer.parseInt(split[1]) : -1);
    }
  }
}
