package aoccommon;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Subsets {

  public static <T> Stream<List<T>> getAllSubsets(List<T> list, int size) {
    return getAllSubsets(list).filter(s -> s.size() == size);
  }

  public static <T> Stream<List<T>> getAllSubsets(List<T> list) {
    return IntStream.range(0, (int) Math.pow(2, list.size()))
        .mapToObj(i -> IntStream.range(0, list.size())
            .mapToObj(pos -> ((i >> pos) & 1) == 1 ? Optional.of(list.get(pos)) : Optional.<T>empty())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList()));
  }
}