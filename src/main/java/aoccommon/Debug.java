package aoccommon;

import java.io.IOException;

public final class Debug {
  private static boolean LOGGING_ENABLED = false;
  private static int COUNTER = 0;

  public static void enablePrint() {
    LOGGING_ENABLED = true;
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
}