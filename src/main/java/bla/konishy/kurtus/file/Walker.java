package bla.konishy.kurtus.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Walker {

    public static void main(String[] args) {
        try (Stream<Path> paths = Files.walk(Paths.get("src/main/java"))) {
            paths.filter(p -> p.toFile().getName().endsWith(".java")).forEach(System.out::println);

            Stream.iterate(1, n -> n + 1).limit(10).forEach(System.out::println);

            Instant t0 = Instant.now();
            IntStream.range(1, 10).forEach(System.out::println);
            System.out.println(Duration.between(t0, Instant.now()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
