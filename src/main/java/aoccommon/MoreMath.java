package aoccommon;

public class MoreMath {

  public static long gcd(long a, long b) {
    // Euclid's algorithm.
    while (b > 0) {
      long t = b;
      b = a % b;
      a = t;
    }
    return a;
  }

  public static long gcd(long [] input) {
    if (input.length == 0) {
      return 0;
    }
    long gcd = input[0];
    for (int i = 1; i < input.length; i++) {
      gcd = gcd(gcd, input[i]);
    }
    return gcd;
  }

  public static long lcm(long a, long b) {
    return (a * b) / gcd(a, b);
  }

  public static long lcm(long [] input) {
    if (input.length == 0) {
      return 0;
    }
    long lcm = input[0];
    for (int i = 1; i < input.length; i++) {
      lcm = lcm(lcm, input[i]);
    }
    return lcm;
  }
}
