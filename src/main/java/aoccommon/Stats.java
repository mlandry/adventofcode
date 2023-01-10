package aoccommon;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stats {
  private static final Map<String, Long> ACTIVE = new HashMap<>();
  private static final Map<String, List<Long>> COMPLETED = new HashMap<>();

  private static final Map<String, Long> COUNTERS = new HashMap<>();

  public static void startTimer(String name) {
    ACTIVE.put(name, System.currentTimeMillis());
  }

  public static void endTimer(String name) {
    long elapsed = System.currentTimeMillis() - ACTIVE.remove(name);
    COMPLETED.computeIfAbsent(name, k -> new ArrayList<>()).add(elapsed);
  }

  public static void print(PrintStream out) {
    if (!COMPLETED.isEmpty()) {
      out.println("=== TIMERS ===");
      for (Map.Entry<String, List<Long>> entry : COMPLETED.entrySet()) {
        if (entry.getValue().size() == 1) {
          out.println(String.format("[%s]: %d", entry.getKey(), entry.getValue().get(0)));
        } else {
          long min = Long.MAX_VALUE;
          long max = 0;
          long sum = 0;
          for (long value : entry.getValue()) {
            min = Math.min(value, min);
            max = Math.max(value, max);
            sum += value;
          }
          long avg = sum / entry.getValue().size();
          out.println(String.format("[%s]: %d total, %d avg, %d min, %d max", entry.getKey(), sum, avg, min, max));
        }
      }
    }
    if (!COUNTERS.isEmpty()) {
      out.println("=== COUNTERS ===");
      COUNTERS.entrySet().forEach(e -> out.println(String.format("[%s]: %d", e.getKey(), e.getValue())));
    }
  }

  public static void incrementCounter(String name) {
    COUNTERS.compute(name, (k, v) -> v == null ? 1 : v + 1);
  }
}
