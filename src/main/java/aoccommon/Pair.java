package aoccommon;

public final record Pair<A extends Object, B extends Object>(A first, B second) {

  public static <A extends Object, B extends Object> Pair<A, B> of(A first, B second) {
    return new Pair<>(first, second);
  }
}
