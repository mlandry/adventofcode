package aoccommon;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class Debug {
  private static boolean LOGGING_ENABLED = false;
  private static boolean TIMERS_ENABLED = false;
  private static int COUNTER = 0;

  private static final Map<String, Long> TIMERS = new HashMap<>();

  public static void enablePrint() {
    LOGGING_ENABLED = true;
  }

  public static void enableTimers() {
    TIMERS_ENABLED = true;
  }

  public static void println(String fmt, Object... args) {
    if (!LOGGING_ENABLED) {
      return;
    }
    System.out.println(String.format(fmt, args));
  }

  public static void printlnAndWaitForInput(String fmt, Object... args) {
    if (!LOGGING_ENABLED) {
      return;
    }
    println(fmt, args);
    try {
      System.in.read();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void waitForInput() {
    try {
      System.in.read();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }    
  }

  public static void printlnEveryN(int n, String fmt, Object... args) {
    if (!LOGGING_ENABLED) {
      return;
    }
    if (COUNTER++ % n == 0) {
      println("<%d> %s", (COUNTER - 1), String.format(fmt, args));
    }
  }

  public static void startTimer(String name) {
    if (!TIMERS_ENABLED) {
      return;
    }
    TIMERS.put(name, System.currentTimeMillis());
  }

  public static void endTimer(String name) {
    if (!TIMERS_ENABLED) {
      return;
    }
    long elapsed = System.currentTimeMillis() - TIMERS.remove(name);
    System.out.println(String.format("[%s]: %d", name, elapsed));
  }
}