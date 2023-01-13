package aoccommon;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Stats {
  private static final Map<String, Long> ACTIVE = new ConcurrentHashMap<>();
  private static final Map<String, List<Long>> COMPLETED = new ConcurrentHashMap<>();

  private static final Map<String, AtomicLong> COUNTERS = new ConcurrentHashMap<>();
  private static final Map<String, Map<Object, AtomicLong>> HISTOGRAMS = new ConcurrentHashMap<>();

  private static AtomicBoolean printOnExit = new AtomicBoolean(false);

  public static void enablePrintOnExit() {
    printOnExit.set(true);
  }

  static {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        if (printOnExit.get()) {
          print(System.out);
        }
      }
    });
  }

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
          out.println(String.format("[%s]: %dms", entry.getKey(), entry.getValue().get(0)));
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
          out.println(String.format("[%s]: %dms total, %dms avg, %dms min, %dms max", entry.getKey(), sum, avg, min, max));
        }
      }
    }
    if (!COUNTERS.isEmpty()) {
      out.println("=== COUNTERS ===");
      COUNTERS.entrySet().forEach(e -> out.println(String.format("[%s]: %d", e.getKey(), e.getValue().get())));
    }
    if (!HISTOGRAMS.isEmpty()) {
      out.println("=== HISTOGRAMS ===");
      for (Map.Entry<String, Map<Object, AtomicLong>> entry : HISTOGRAMS.entrySet()) {
        out.print(String.format("[%s]: ", entry.getKey()));
        out.println(entry.getValue().entrySet().stream().map(e -> String.format("%s-%d", e.getKey(), e.getValue().get())).collect(Collectors.joining(", ")));
      }
    }
  }

  public static void incrementCounter(String name) {
    COUNTERS.computeIfAbsent(name, k -> new AtomicLong()).incrementAndGet();
  }

  public static void incrementHistogramValue(String name, Object value) {
    HISTOGRAMS.computeIfAbsent(name, k -> new ConcurrentHashMap<>()).computeIfAbsent(value, v -> new AtomicLong()).incrementAndGet();
  }
}
