package aoccommon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class InputHelper {

  public static BufferedReader newBufferedFileReader(String filepath) throws IOException {
    return Files.newBufferedReader(Paths.get(filepath));
  }

  public static Stream<String> lines(String filepath) throws IOException {
    return newBufferedFileReader(filepath).lines();
  }

  public static BufferedReader newBufferedResourceReader(String resourceName) throws IOException {
    return new BufferedReader(
        new InputStreamReader(InputHelper.class.getClassLoader().getResourceAsStream(resourceName)));
  }

  public static Stream<String> linesFromResource(String resourceName) throws IOException {
    return newBufferedResourceReader(resourceName).lines();
  }
}
