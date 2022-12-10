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
    System.out.println("Part 2:\n" + computer.crt.toString());
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
    private static final int CRT_WIDTH = 40;

    private long cycle = 0;
    private long register = 1;
    private long signalStrengthSum = 0;

    StringBuilder crt = new StringBuilder();

    private void execute(Instruction instruction) {
      for (int c = 0; c < instruction.type.cycles; c++) {
        runCycle();
      }
      register += instruction.value;
    }

    private void runCycle() {
      // CRT position is zero-indexed, 40 characters wide.
      int crtx = (int) cycle % CRT_WIDTH;

      // Signal strength is updated at 20, 60, 100, 140, etc.
      if ((++cycle - 20) % 40 == 0) {
        signalStrengthSum += (cycle * register);
      }

      // Sprite is 3 characters wide with register pointing to the middle.
      if (Math.abs(crtx - register) <= 1) {
        crt.append('#');
      } else {
        crt.append('.');
      }
      if (crtx == CRT_WIDTH - 1) {
        crt.append('\n');
      }
    }
  }
}
