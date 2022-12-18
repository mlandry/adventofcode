package aoccommon;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class Debug {
  private static boolean ENABLED = false;
  private static int COUNTER = 0;

  private static final Map<String, Long> TIMERS = new HashMap<>();

  public static void enablePrint() {
    ENABLED = true;
  }

  public static void println(String fmt, Object... args) {
    if (!ENABLED) {
      return;
    }
    System.out.println(String.format(fmt, args));
  }

  public static void printlnAndWaitForInput(String fmt, Object... args) {
    if (!ENABLED) {
      return;
    }
    println(fmt, args);
    try {
      System.in.read();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void printlnEveryN(int n, String fmt, Object ... args) {
    if (!ENABLED) {
      return;
    }
    if (COUNTER++ % n == 0) {
      println(fmt, args);
    }
  }

  public static void startTimer(String name) {
    TIMERS.put(name, System.currentTimeMillis());
  }

  public static void endTimer(String name) {
    long elapsed = System.currentTimeMillis() - TIMERS.remove(name);
    println("[%s]: %d", name, elapsed);
  }
}