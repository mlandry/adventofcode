package aoc2022.day10;

import java.util.List;
import java.util.stream.Collectors;

import aoccommon.InputHelper;

/** Solution for {@link https://adventofcode.com/2022/day/10}. */
public class CathodeRayTube {

  private static final String INPUT = "aoc2022/day10/input.txt";

  public static void main(String[] args) throws Exception {
    List<Instruction> instructions = InputHelper.linesFromResource(INPUT)
        .map(Instruction::parse)
        .collect(Collectors.toList());

    Computer computer = new Computer();
    instructions.forEach(computer::execute);

    System.out.println("Part 1: " + computer.signalStrengthSum);
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
        split.length == 2 ? Integer.parseInt(split[1]) : 0);
    }
  }

  private static class Computer {
    private long cycle = 0;
    private long register = 1;
    private long signalStrengthSum = 0;

    private void execute(Instruction instruction) {
      for (int c = 0; c < instruction.type.cycles; c++) {
        incrementCycle();
      }
      register += instruction.value;
    }

    private void incrementCycle() {
      if ((++cycle - 20) % 40 == 0) {
        signalStrengthSum += (cycle * register);
      }
    }
  }
}
